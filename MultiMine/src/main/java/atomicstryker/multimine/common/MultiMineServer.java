package atomicstryker.multimine.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.BlockEvent;

public class MultiMineServer
{
    private static MultiMineServer instance;
    private static MinecraftServer serverInstance;
    private final HashMap<Integer, List<PartiallyMinedBlock>> partiallyMinedBlocksListByDimension;
    private final BlockRegenQueue blockRegenQueue;
    private final HashMap<String, Boolean> blacklistedBlocksAndTools;

    /**
     * Server instance of Multi Mine Mod. Keeps track of Players having the Mod
     * installed, the Blocks they damage, and the Block regeneration Queue,
     * which is watched by the integrated Tick Handler.
     */
    public MultiMineServer()
    {
        partiallyMinedBlocksListByDimension = Maps.newHashMap();
        instance = this;
        blockRegenQueue = new BlockRegenQueue(30, new BlockAgeComparator());
        blacklistedBlocksAndTools = Maps.newHashMap();

        final Set<Entry<String, Property>> setblocks = MultiMine.instance().config.getCategory("bannedblocks").entrySet();
        for (Entry<String, Property> entry : setblocks)
        {
            blacklistedBlocksAndTools.put(entry.getKey(), entry.getValue().getBoolean(false));
        }
        final Set<Entry<String, Property>> setitems = MultiMine.instance().config.getCategory("banneditems").entrySet();
        for (Entry<String, Property> entry : setitems)
        {
            blacklistedBlocksAndTools.put(entry.getKey(), entry.getValue().getBoolean(false));
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static MultiMineServer instance()
    {
        return instance;
    }

    /**
     * Called when a client has a block progression update. Update the
     * already existing partial Block or create one if not present,
     * and send that information back to all interested players.
     *
     * @param player player doing the digging
     * @param x      coordinate of Block
     * @param y      coordinate of Block
     * @param z      coordinate of Block
     * @param value  block progression the client reported
     */
    public void onClientSentPartialBlockPacket(EntityPlayerMP player, BlockPos pos, float value)
    {
        serverInstance = FMLCommonHandler.instance().getMinecraftServerInstance();
        int dimension = player.dimension;
        MultiMine.instance().debugPrint("multi mine client " + player + " sent progress packet: " + value);

        int x = pos.x;
        int y = pos.y;
        int z = pos.z;
        final Block block = player.worldObj.getBlock(x, y, z);
        final int meta = player.worldObj.getBlockMetadata(x, y, z);
        if (isUsingBannedItem(player) || isBlockBanned(block, meta))
        {
            return;
        }

        List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);

        if (partiallyMinedBlocks == null)
        {
            partiallyMinedBlocks = Lists.newArrayList();
            partiallyMinedBlocksListByDimension.put(dimension, partiallyMinedBlocks);
        }

        final PartiallyMinedBlock newblock = new PartiallyMinedBlock(x, y, z, dimension, 0f);
        newblock.setLastTimeMined(System.currentTimeMillis() + MultiMine.instance().getInitialBlockRegenDelay());
        for (PartiallyMinedBlock iterBlock : partiallyMinedBlocks)
        {
            if (iterBlock.equals(newblock))
            {
                iterBlock.setProgress(Math.max(iterBlock.getProgress(), value));
                iterBlock.setLastTimeMined(System.currentTimeMillis() + MultiMine.instance().getInitialBlockRegenDelay());
                MultiMine.instance().debugPrint("Server updating partial block at: [" + x + "|" + y + "|" + z + "], progress now: " + iterBlock.getProgress());

                // send the newly advanced partialblock to all relevant players
                sendPartiallyMinedBlockUpdateToAllPlayers(iterBlock);

                if (iterBlock.isFinished() && block != Blocks.air)
                {
                    MultiMine.instance().debugPrint("Server finishing partial block at: [" + x + "|" + y + "|" + z + "]");
                    // and if its done, destroy the world block
                    player.worldObj.destroyBlockInWorldPartially(player.getEntityId(), x, y, z, -1);

                    BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(player.worldObj, MinecraftServer.getServer().getGameType(), player, x, y, z);
                    if (!event.isCanceled())
                    {
                        TileEntity tileentity = player.worldObj.getTileEntity(x, y, z);

                        ItemStack stack = player.getCurrentEquippedItem();
                        if (stack != null && stack.getItem().onBlockStartBreak(stack, x, y, z, player))
                        {
                            return;
                        }

                        player.worldObj.playAuxSFXAtEntity(player, 2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));

                        ItemStack itemstack = player.getCurrentEquippedItem();
                        boolean canHarvest = block.canHarvestBlock(player, meta);

                        if (itemstack != null)
                        {
                            itemstack.func_150999_a(player.worldObj, block, x, y, z, player);
                            if (itemstack.stackSize == 0)
                            {
                                player.destroyCurrentEquippedItem();
                            }
                        }

                        block.onBlockHarvested(player.worldObj, x, y, z, meta, player);
                        boolean removed = block.removedByPlayer(player.worldObj, player, x, y, z, canHarvest);
                        if (removed)
                        {
                            block.onBlockDestroyedByPlayer(player.worldObj, x, y, z, meta);
                        }

                        if (removed && canHarvest)
                        {
                            block.harvestBlock(player.worldObj, player, x, y, z, meta);
                        }

                        // Drop experience
                        if (removed && !event.isCanceled())
                        {
                            block.dropXpOnBlockBreak(player.worldObj, x, y, z, meta);
                        }

                        partiallyMinedBlocks.remove(iterBlock);
                        blockRegenQueue.remove(iterBlock);
                    }
                }
                else
                {
                    blockRegenQueue.offer(iterBlock);
                }

                return;
            }
        }

        // else send the new partialblock to all relevant players
        // System.out.println("Server creating new partial block at: ["+x+"|"+y+"|"+z+"]");

        if (partiallyMinedBlocks.size() > 29)
        {
            PartiallyMinedBlock old = partiallyMinedBlocks.get(0);
            sendPartiallyMinedBlockDeleteCommandToAllPlayers(old);
            partiallyMinedBlocks.remove(old);
            blockRegenQueue.remove(old);
        }

        partiallyMinedBlocks.add(newblock);
        blockRegenQueue.offer(newblock);
        sendPartiallyMinedBlockUpdateToAllPlayers(newblock);
    }

    private boolean isBlockBanned(Block block, int meta)
    {
        final String ident = Block.blockRegistry.getNameForObject(block).toString() + "-" + meta;
        Boolean result = blacklistedBlocksAndTools.get(ident);
        if (result != null)
        {
            return result;
        }

        final Configuration config = MultiMine.instance().config;
        config.load();
        result = config.get("bannedblocks", ident, false).getBoolean(false);
        config.save();
        blacklistedBlocksAndTools.put(ident, result);

        return result;
    }

    private boolean isUsingBannedItem(EntityPlayer player)
    {
        ItemStack item = player.getCurrentEquippedItem();
        if (item == null || item.getItem() == null)
        {
            return false;
        }

        final String ident = Item.itemRegistry.getNameForObject(item.getItem()).toString() + "-" + item.getItemDamage();
        Boolean result = blacklistedBlocksAndTools.get(ident);
        if (result != null && result)
        {
            return true;
        }

        final Configuration config = MultiMine.instance().config;
        config.load();
        result = config.get("banneditems", ident, false).getBoolean(false);
        config.save();
        blacklistedBlocksAndTools.put(ident, result);

        if (result)
        {
            return true;
        }
        return false;
    }

    /**
     * Tells all clients to delete this partially mined Block off their local
     * storage, the server exceeds the max amount of concurrent partial Blocks.
     *
     * @param block partial Block to be deleted
     */
    private void sendPartiallyMinedBlockDeleteCommandToAllPlayers(PartiallyMinedBlock block)
    {
        MultiMine.instance().networkHelper.sendPacketToAllAroundPoint(new PartialBlockRemovalPacket(block.getPos().x, block.getPos().y, block.getPos().z),
                new TargetPoint(block.getDimension(), block.getPos().x, block.getPos().y, block.getPos().z, 30D));
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        final EntityPlayerMP player = (EntityPlayerMP) event.player;
        int dimension = player.worldObj.provider.dimensionId;
        final List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);
        if (partiallyMinedBlocks != null)
        {
            for (PartiallyMinedBlock block : partiallyMinedBlocks)
            {
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
    private List<PartiallyMinedBlock> getPartiallyMinedBlocksForDimension(int dim)
    {
        return partiallyMinedBlocksListByDimension.get(dim);
    }

    /**
     * Sends a partial Block Packet to all players in the matching dimension and
     * a certain area. Overwrites their local partial Block instances with
     * whatever you send.
     *
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockUpdateToAllPlayers(PartiallyMinedBlock block)
    {
        MultiMine.instance().networkHelper.sendPacketToAllAroundPoint(new PartialBlockPacket("server", block.getPos().x, block.getPos().y, block.getPos().z, block.getProgress()),
                new TargetPoint(block.getDimension(), block.getPos().x, block.getPos().y, block.getPos().z, 32D));
    }

    /**
     * Sends a partial Block Packet to a particular player.
     *
     * @param p     Player targeted
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockToPlayer(EntityPlayerMP p, PartiallyMinedBlock block)
    {
        MultiMine.instance().networkHelper.sendPacketToPlayer(new PartialBlockPacket("server", block.getPos().x, block.getPos().y, block.getPos().z, block.getProgress()), p);
    }

    /**
     * Tick Handler to achieve Block Regeneration. We keep track of our Block
     * age using a PriorityQueue and start repairing Blocks if they get too old.
     */
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            if (blockRegenQueue.isEmpty())
            {
                return;
            }

            PartiallyMinedBlock block;
            for (Iterator<PartiallyMinedBlock> iter = blockRegenQueue.iterator(); iter.hasNext(); )
            {
                block = iter.next();
                if (isBlockGone(block))
                {
                    sendPartiallyMinedBlockDeleteCommandToAllPlayers(block);
                    getPartiallyMinedBlocksForDimension(block.getDimension()).remove(block);
                    iter.remove();
                }
            }

            if (blockRegenQueue.isEmpty() || !MultiMine.instance().getBlockRegenEnabled())
            {
                return;
            }

            long curTime = System.currentTimeMillis();
            if (blockRegenQueue.peek().getLastTimeMined() + MultiMine.instance().getBlockRegenInterval() < curTime)
            {
                block = blockRegenQueue.poll();

                block.setProgress(block.getProgress() - 0.1f);
                block.setLastTimeMined(curTime);
                if (block.getProgress() < 0f)
                {
                    // tell everyone to stop tracking this one
                    sendPartiallyMinedBlockDeleteCommandToAllPlayers(block);
                    getPartiallyMinedBlocksForDimension(block.getDimension()).remove(block);
                }
                else
                {
                    // send update about this one to all
                    sendPartiallyMinedBlockUpdateToAllPlayers(block);
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
    private boolean isBlockGone(PartiallyMinedBlock block)
    {
        return serverInstance.worldServerForDimension(block.getDimension()).isAirBlock(block.getPos().x, block.getPos().y, block.getPos().z);
    }

    /**
     * PriorityQueue sorting Blocks by age, in order to only check one each tick
     */
    private class BlockRegenQueue extends PriorityQueue<PartiallyMinedBlock>
    {
        private static final long serialVersionUID = 1L;

        BlockRegenQueue(int initialSize, Comparator<PartiallyMinedBlock> comparator)
        {
            super(initialSize, comparator);
        }

        /**
         * Overriding the offer method in order to delete older instances in
         * favor of more recently mined ones.
         */
        @Override
        public boolean offer(PartiallyMinedBlock block)
        {
            if (contains(block))
            {
                this.remove(block);
            }
            return super.offer(block);
        }
    }

    /**
     * Comparator to help sort PartiallyMinedBlock instances by their age
     */
    private class BlockAgeComparator implements Comparator<PartiallyMinedBlock>
    {
        @Override
        public int compare(PartiallyMinedBlock b1, PartiallyMinedBlock b2)
        {
            if (b1.getLastTimeMined() < b2.getLastTimeMined())
            {
                return -1;
            }
            return 1;
        }
    }
}
