package atomicstryker.multimine.common;

import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiMineServer {
    private static MultiMineServer instance;
    private final HashMap<Integer, List<PartiallyMinedBlock>> partiallyMinedBlocksListByDimension;
    private final BlockRegenQueue blockRegenQueue;
    private final HashMap<String, Boolean> blacklistedBlocksAndTools;

    /**
     * Server instance of Multi Mine Mod. Keeps track of Players having the Mod
     * installed, the Blocks they damage, and the Block regeneration Queue,
     * which is watched by the integrated Tick Handler.
     */
    public MultiMineServer() {
        partiallyMinedBlocksListByDimension = Maps.newHashMap();
        instance = this;
        blockRegenQueue = new BlockRegenQueue(30, new BlockAgeComparator());
        blacklistedBlocksAndTools = Maps.newHashMap();

        for (Map.Entry<String, Boolean> entry : MultiMine.instance().config.getBannedBlocks().entrySet()) {
            blacklistedBlocksAndTools.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Boolean> entry : MultiMine.instance().config.getBannedItems().entrySet()) {
            blacklistedBlocksAndTools.put(entry.getKey(), entry.getValue());
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static MultiMineServer instance() {
        return instance;
    }

    private static String fromItemStack(ItemStack itemStack) {
        itemStack.getOrCreateTag().putString("nameId", ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
        return itemStack.getOrCreateTag().toString();
    }

    /**
     * Called when a client has a block progression update. Update the already
     * existing partial Block or create one if not present, and send that
     * information back to all interested players.
     *
     * @param player player doing the digging
     * @param x      coordinate of Block
     * @param y      coordinate of Block
     * @param z      coordinate of Block
     * @param value  block progression the client reported
     */
    public void onClientSentPartialBlockPacket(EntityPlayerMP player, int x, int y, int z, float value) {

        int dimension = player.dimension.getId();
        MultiMine.instance().debugPrint("multi mine client {} sent progress packet: {}", player.getName(), value);

        final BlockPos pos = new BlockPos(x, y, z);
        final IBlockState iblockstate = player.world.getBlockState(pos);
        if (isUsingBannedItem(player) || isBlockBanned(iblockstate)) {
            return;
        }

        List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);

        if (partiallyMinedBlocks == null) {
            partiallyMinedBlocks = Lists.newArrayList();
            partiallyMinedBlocksListByDimension.put(dimension, partiallyMinedBlocks);
        }

        final PartiallyMinedBlock newblock = new PartiallyMinedBlock(x, y, z, dimension, 0f);
        newblock.setLastTimeMined(System.currentTimeMillis() + MultiMine.instance().getInitialBlockRegenDelay());
        for (PartiallyMinedBlock iterBlock : partiallyMinedBlocks) {
            if (iterBlock.equals(newblock)) {
                iterBlock.setProgress(Math.max(iterBlock.getProgress(), value));
                iterBlock.setLastTimeMined(System.currentTimeMillis() + MultiMine.instance().getInitialBlockRegenDelay());
                MultiMine.instance().debugPrint("Server updating partial block at: [{}|{}|{}], progress now: {}", x, y, z, iterBlock.getProgress());

                // send the newly advanced partialblock to all relevant players
                sendPartiallyMinedBlockUpdateToAllPlayers(iterBlock, false);

                if (iterBlock.isFinished() && !iblockstate.isAir(player.world, pos)) {
                    MultiMine.instance().debugPrint("Server finishing partial block at: [{}|{}|{}]", x, y, z);
                    // and if its done, destroy the world block
                    player.world.sendBlockBreakProgress(player.getEntityId(), pos, -1);

                    final int event = ForgeHooks.onBlockBreakEvent(player.world, player.interactionManager.getGameType(), player, pos);
                    if (event != -1) {
                        TileEntity tileentity = player.world.getTileEntity(pos);

                        ItemStack stack = player.getHeldItemMainhand();
                        if (stack.getItem().onBlockStartBreak(stack, pos, player)) {
                            return;
                        }

                        player.world.playEvent(2001, pos, Block.getStateId(iblockstate));

                        ItemStack itemstack = player.getHeldItemMainhand();
                        boolean canHarvest = iblockstate.getBlock().canHarvestBlock(iblockstate, player.world, pos, player);

                        itemstack.onBlockDestroyed(player.world, iblockstate, pos, player);
                        if (itemstack.getCount() == 0) {
                            player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                        }

                        iblockstate.getBlock().onBlockHarvested(player.world, pos, iblockstate, player);
                        boolean removed;
                        if (iblockstate.getBlock() instanceof BlockDoor) {
                            // onBlockHarvested in BlockDoor actually sets the block to air, wat
                            removed = true;
                        } else {
                            removed = iblockstate.getBlock().removedByPlayer(player.world.getBlockState(pos), player.world, pos, player, canHarvest, player.world.getFluidState(pos));
                        }

                        if (removed) {
                            iblockstate.getBlock().onPlayerDestroy(player.world, pos, iblockstate);
                        }

                        if (removed && canHarvest && !(iblockstate.getBlock() instanceof BlockSkull)) {
                            iblockstate.getBlock().harvestBlock(player.world, player, pos, iblockstate, tileentity, itemstack);
                        }

                        // Drop experience
                        if (removed && event > 0) {
                            iblockstate.getBlock().dropXpOnBlockBreak(player.world, pos, event);
                        }
                        if (removed) {
                            player.world.destroyBlock(pos, false);
                        }

                        partiallyMinedBlocks.remove(iterBlock);
                        blockRegenQueue.remove(iterBlock);
                    }
                } else {
                    blockRegenQueue.offer(iterBlock);
                }

                return;
            }
        }

        // else send the new partialblock to all relevant players
        // System.out.println("Server creating new partial block at:
        // ["+x+"|"+y+"|"+z+"]");

        if (partiallyMinedBlocks.size() > 29) {
            PartiallyMinedBlock old = partiallyMinedBlocks.get(0);
            sendPartiallyMinedBlockDeleteCommandToAllPlayers(old);
            partiallyMinedBlocks.remove(old);
            blockRegenQueue.remove(old);
        }

        partiallyMinedBlocks.add(newblock);
        blockRegenQueue.offer(newblock);
        sendPartiallyMinedBlockUpdateToAllPlayers(newblock, false);
    }

    private boolean isBlockBanned(IBlockState blockState) {
        NBTTagCompound tagCompound = NBTUtil.writeBlockState(blockState);
        String ident = tagCompound.toString();

        Boolean result = findInBlacklistEntries(ident);
        if (result != null) {
            return result;
        }

        MultiMine.instance().config.getBannedBlocks().put(ident, false);
        GsonConfig.saveConfig(MultiMine.instance().config, MultiMine.instance().configFile);
        blacklistedBlocksAndTools.put(ident, result);

        return false;
    }

    private Boolean findInBlacklistEntries(String ident) {
        for (String key : blacklistedBlocksAndTools.keySet()) {
            Pattern pattern = Pattern.compile(key);
            Matcher matcher = pattern.matcher(ident);
            if (matcher.find()) {
                return blacklistedBlocksAndTools.get(key);
            }
        }
        return null;
    }

    private boolean isUsingBannedItem(EntityPlayer player) {
        for (EnumHand hand : EnumHand.values()) {
            ItemStack item = player.getHeldItem(hand);
            if (item == ItemStack.EMPTY) {
                continue;
            }

            final String ident = fromItemStack(item);
            Boolean result = findInBlacklistEntries(ident);
            if (result != null) {
                return result;
            }

            MultiMine.instance().config.getBannedItems().put(ident, false);
            GsonConfig.saveConfig(MultiMine.instance().config, MultiMine.instance().configFile);
            blacklistedBlocksAndTools.put(ident, result);
        }
        return false;
    }

    /**
     * Tells all clients to delete this partially mined Block off their local
     * storage, the server exceeds the max amount of concurrent partial Blocks.
     *
     * @param block partial Block to be deleted
     */
    private void sendPartiallyMinedBlockDeleteCommandToAllPlayers(PartiallyMinedBlock block) {
        MultiMine.instance().networkHelper.sendPacketToAllAroundPoint(new PartialBlockRemovalPacket(block.getPos()),
                new PacketDistributor.TargetPoint(block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), 30D, getServerDimension(block.getDimension())));
    }

    private DimensionType getServerDimension(int dimension) {
        for (WorldServer candidate : ServerLifecycleHooks.getCurrentServer().getWorlds()) {
            if (candidate.getWorldInfo().getDimension() == dimension) {
                return candidate.getDimension().getType();
            }
        }
        throw new RuntimeException("Reference to unknown dimension " + dimension);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        final EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
        int dimension = player.world.getWorldInfo().getDimension();
        final List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);
        if (partiallyMinedBlocks != null) {
            for (PartiallyMinedBlock block : partiallyMinedBlocks) {
                sendPartiallyMinedBlockToPlayer(player, block);
            }
        }
    }

    /**
     * Helper method to get the correct partial Block list for a World Dimension
     *
     * @param dim Dimension of the world
     * @return the List of partial Blocks for the dimension. Can be null, can be
     * empty.
     */
    private List<PartiallyMinedBlock> getPartiallyMinedBlocksForDimension(int dim) {
        return partiallyMinedBlocksListByDimension.get(dim);
    }

    /**
     * Sends a partial Block Packet to all players in the matching dimension and
     * a certain area. Overwrites their local partial Block instances with
     * whatever you send.
     *
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockUpdateToAllPlayers(PartiallyMinedBlock block, boolean regenerating) {
        MultiMine.instance().networkHelper.sendPacketToAllAroundPoint(new PartialBlockPacket("server", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), block.getProgress(), regenerating),
                new PacketDistributor.TargetPoint(block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), 32D, getServerDimension(block.getDimension())));
    }

    /**
     * Sends a partial Block Packet to a particular player.
     *
     * @param p     Player targeted
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockToPlayer(EntityPlayerMP p, PartiallyMinedBlock block) {
        MultiMine.instance().networkHelper.sendPacketToPlayer(new PartialBlockPacket("server", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), block.getProgress(), false), p);
    }

    /**
     * Tick Handler to achieve Block Regeneration. We keep track of our Block
     * age using a PriorityQueue and start repairing Blocks if they get too old.
     */
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick) {
        if (tick.phase == Phase.END) {
            if (blockRegenQueue.isEmpty()) {
                return;
            }

            PartiallyMinedBlock block;
            for (Iterator<PartiallyMinedBlock> iter = blockRegenQueue.iterator(); iter.hasNext(); ) {
                block = iter.next();
                if (isBlockGone(block)) {
                    sendPartiallyMinedBlockDeleteCommandToAllPlayers(block);
                    getPartiallyMinedBlocksForDimension(block.getDimension()).remove(block);
                    iter.remove();
                }
            }

            if (blockRegenQueue.isEmpty() || !MultiMine.instance().getBlockRegenEnabled()) {
                return;
            }

            long curTime = System.currentTimeMillis();
            if (blockRegenQueue.peek().getLastTimeMined() + MultiMine.instance().getBlockRegenInterval() < curTime) {
                block = blockRegenQueue.poll();

                block.setProgress(block.getProgress() - 0.1f);
                block.setLastTimeMined(curTime);
                if (block.getProgress() < 0f) {
                    // tell everyone to stop tracking this one
                    MultiMine.instance().debugPrint("Server sending partial delete command for [{}|{}|{}]", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ());
                    sendPartiallyMinedBlockDeleteCommandToAllPlayers(block);
                    getPartiallyMinedBlocksForDimension(block.getDimension()).remove(block);
                } else {
                    // send update about this one to all
                    MultiMine.instance().debugPrint("Server sending partial regen update for [{}|{}|{}]", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ());
                    sendPartiallyMinedBlockUpdateToAllPlayers(block, true);
                    blockRegenQueue.add(block);
                }
            }
        }
    }

    /**
     * Helper method to determine if a Block was removed by other means
     * (Explosion, Sand/Gravel falling, Pistons...)
     *
     * @param block PartiallyMinedBlock to check
     * @return true if the PartiallyMinedBlock Block coordinates return 0 in a
     * getBlockId check, false otherwise
     */
    private boolean isBlockGone(PartiallyMinedBlock block) {
        for (WorldServer candidate : ServerLifecycleHooks.getCurrentServer().getWorlds()) {
            if (candidate.getWorldInfo().getDimension() == block.getDimension()) {
                return candidate.isAirBlock(block.getPos());
            }
        }
        throw new RuntimeException("Reference to unknown dimension " + block.getDimension());
    }

    /**
     * PriorityQueue sorting Blocks by age, in order to only check one each tick
     */
    private class BlockRegenQueue extends PriorityQueue<PartiallyMinedBlock> {
        private static final long serialVersionUID = 1L;

        BlockRegenQueue(int initialSize, Comparator<PartiallyMinedBlock> comparator) {
            super(initialSize, comparator);
        }

        /**
         * Overriding the offer method in order to delete older instances in
         * favor of more recently mined ones.
         */
        @Override
        public boolean offer(PartiallyMinedBlock block) {
            if (contains(block)) {
                this.remove(block);
            }
            return super.offer(block);
        }
    }

    /**
     * Comparator to help sort PartiallyMinedBlock instances by their age
     */
    private class BlockAgeComparator implements Comparator<PartiallyMinedBlock> {
        @Override
        public int compare(PartiallyMinedBlock b1, PartiallyMinedBlock b2) {
            if (b1.getLastTimeMined() < b2.getLastTimeMined()) {
                return -1;
            }
            return 1;
        }
    }
}
