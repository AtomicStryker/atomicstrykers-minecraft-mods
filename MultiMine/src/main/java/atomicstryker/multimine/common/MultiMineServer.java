package atomicstryker.multimine.common;

import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class MultiMineServer {
    private static MultiMineServer instance;
    private final HashMap<ResourceKey<Level>, List<PartiallyMinedBlock>> partiallyMinedBlocksListByDimension;
    private final HashMap<ResourceKey<Level>, BlockRegenQueue> blockRegenQueuesByDimension;

    /**
     * Server instance of Multi Mine Mod. Keeps track of Players having the Mod
     * installed, the Blocks they damage, and the Block regeneration Queue,
     * which is watched by the integrated Tick Handler.
     */
    public MultiMineServer() {
        MultiMine.LOGGER.info("MultiMineServer initializing");
        instance = this;
        partiallyMinedBlocksListByDimension = Maps.newHashMap();
        blockRegenQueuesByDimension = Maps.newHashMap();
    }

    public static MultiMineServer instance() {
        return instance;
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
    public void onClientSentPartialBlockPacket(ServerPlayer player, int x, int y, int z, float value) {
        ResourceKey<Level> dimension = player.level().dimension();
        MultiMine.instance().debugPrint("multi mine client {} sent progress packet: {}", player.getName().getContents(), value);

        final BlockPos pos = new BlockPos(x, y, z);
        final BlockState iblockstate = player.level().getBlockState(pos);
        if (isUsingBannedItem(player) || isBlockBanned(iblockstate) || isItemTagBanned(player.getMainHandItem()) || isBlockTagBanned(iblockstate)) {
            // notify client that this combo does not have multi mine support and progress should not be cached
            sendPartiallyMinedBlockToPlayer(player, new PartiallyMinedBlock(x, y, z, dimension, -1F));
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

                if (iterBlock.isFinished() && !player.level().getBlockState(pos).isAir()) {
                    MultiMine.instance().debugPrint("Server popping, then forgetting block at: [{}|{}|{}]", x, y, z);

                    // see net.minecraft.server.level.ServerPlayerGameMode.handleBlockBreakAction
                    // popping the block serverside is necessary as MC now keeps destroyProgress on serverside
                    player.gameMode.destroyBlock(pos);

                    partiallyMinedBlocks.remove(iterBlock);
                    getBlockRegenQueueForDimension(dimension).remove(iterBlock);
                } else {
                    getBlockRegenQueueForDimension(dimension).offer(iterBlock);
                }

                return;
            }
        }

        // else send the new partialblock to all relevant players

        if (partiallyMinedBlocks.size() > 29) {
            PartiallyMinedBlock old = partiallyMinedBlocks.get(0);
            sendPartiallyMinedBlockDeleteCommandToAllPlayers(old);
            partiallyMinedBlocks.remove(old);
            getBlockRegenQueueForDimension(dimension).remove(old);
        }

        partiallyMinedBlocks.add(newblock);
        getBlockRegenQueueForDimension(dimension).offer(newblock);
        sendPartiallyMinedBlockUpdateToAllPlayers(newblock, false);
    }

    private boolean isBlockBanned(BlockState blockState) {
        String blockIdentifier = ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString();
        Boolean result = MultiMine.instance().getConfig().getBannedBlocks().get(blockIdentifier);
        if (result != null) {
            return result;
        }

        result = false;
        if (!MultiMine.instance().getConfig().isDisableAutoRegisterNames()) {
            MultiMine.instance().getConfig().getBannedBlocks().put(blockIdentifier, result);
            MultiMine.instance().saveConfig();
        }
        return result;
    }

    private boolean isBlockTagBanned(BlockState blockState) {
        // check for the tag being banned in the config list
        return blockState.getTags().anyMatch(blockTagKey -> {
            Boolean boolForTag = MultiMine.instance().getConfig().getBannedBlocks().get(blockTagKey.location().toString());
            return Objects.requireNonNullElse(boolForTag, false);
        });
    }

    private boolean isUsingBannedItem(Player player) {
        ItemStack item = player.getMainHandItem();

        String ident = ForgeRegistries.ITEMS.getKey(item.getItem()).toString();
        Boolean result = MultiMine.instance().getConfig().getBannedItems().get(ident);
        if (result != null) {
            return result;
        }

        result = false;
        if (!MultiMine.instance().getConfig().isDisableAutoRegisterNames()) {
            MultiMine.instance().getConfig().getBannedItems().put(ident, result);
            MultiMine.instance().saveConfig();
        }
        return result;
    }

    private boolean isItemTagBanned(ItemStack handItem) {
        return handItem.getTags().anyMatch(itemTagKey -> {
            Boolean boolForTag = MultiMine.instance().getConfig().getBannedItems().get(itemTagKey.location().toString());
            return Objects.requireNonNullElse(boolForTag, false);
        });
    }

    /**
     * Tells all clients to delete this partially mined Block off their local
     * storage, the server exceeds the max amount of concurrent partial Blocks.
     *
     * @param block partial Block to be deleted
     */
    private void sendPartiallyMinedBlockDeleteCommandToAllPlayers(PartiallyMinedBlock block) {
        MultiMine.networkChannel.send(new PartialBlockRemovalPacket(block.getPos()),
                PacketDistributor.NEAR.with(new PacketDistributor.TargetPoint(block.getPos().getX(),
                        block.getPos().getY(), block.getPos().getZ(), 30D, block.getDimension())));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        instance().onPlayerLoginInstance(event);
    }

    private void onPlayerLoginInstance(PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getEntity();
        ResourceKey<Level> dimensionKey = player.level().dimension();
        final List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimensionKey);
        if (partiallyMinedBlocks != null) {
            for (PartiallyMinedBlock block : partiallyMinedBlocks) {
                sendPartiallyMinedBlockToPlayer((ServerPlayer) player, block);
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
    private List<PartiallyMinedBlock> getPartiallyMinedBlocksForDimension(ResourceKey<Level> dim) {
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
        MultiMine.networkChannel.send(new PartialBlockPacket("server", block.getPos().getX(),
                        block.getPos().getY(), block.getPos().getZ(), block.getProgress(), regenerating),
                PacketDistributor.NEAR.with(new PacketDistributor.TargetPoint(block.getPos().getX(),
                        block.getPos().getY(), block.getPos().getZ(), 32D, block.getDimension())));
    }

    /**
     * Sends a partial Block Packet to a particular player.
     *
     * @param p     Player targeted
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockToPlayer(ServerPlayer p, PartiallyMinedBlock block) {
        MultiMine.networkChannel.send(new PartialBlockPacket("server", block.getPos().getX(),
                block.getPos().getY(), block.getPos().getZ(), block.getProgress(), false),
                PacketDistributor.PLAYER.with(p));
    }

    @SubscribeEvent
    public void commonSetup(ServerStartedEvent evt) {
        // dedicated server starting point
        MultiMine.LOGGER.info("MultiMine ServerStartedEvent");
        MultiMine.instance().initIfNeeded(evt.getServer().getAllLevels().iterator().next());
    }

    @SubscribeEvent
    public void stopping(ServerStoppingEvent evt) {
        MultiMine.LOGGER.info("MultiMine ServerStoppingEvent");
        MultiMine.instance().shutDown();
        // prevent any block interactions when reopening worlds because MC will deadlock
        partiallyMinedBlocksListByDimension.clear();
        blockRegenQueuesByDimension.clear();
    }

    /**
     * Tick Handler to achieve Block Regeneration. We keep track of our Block
     * age using a PriorityQueue and start repairing Blocks if they get too old.
     */
    @SubscribeEvent
    public void onTick(TickEvent.LevelTickEvent tick) {
        if (tick.side.isClient() || tick.phase != TickEvent.Phase.END) {
            return;
        }
        BlockRegenQueue queueForDimension = getBlockRegenQueueForDimension(tick.level.dimension());
        if (queueForDimension.isEmpty()) {
            return;
        }

        PartiallyMinedBlock block;
        for (Iterator<PartiallyMinedBlock> iter = queueForDimension.iterator(); iter.hasNext(); ) {
            block = iter.next();
            if (tick.level.isEmptyBlock(block.getPos())) {
                sendPartiallyMinedBlockDeleteCommandToAllPlayers(block);
                getPartiallyMinedBlocksForDimension(block.getDimension()).remove(block);
                iter.remove();
            }
        }

        if (queueForDimension.isEmpty() || !MultiMine.instance().getBlockRegenEnabled()) {
            return;
        }

        long curTime = System.currentTimeMillis();
        if (queueForDimension.peek().getLastTimeMined() + MultiMine.instance().getBlockRegenInterval() < curTime) {
            block = queueForDimension.poll();

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
                queueForDimension.add(block);
            }
        }
    }

    /**
     * PriorityQueue sorting Blocks by age, in order to only check one each tick
     */
    private static class BlockRegenQueue extends PriorityQueue<PartiallyMinedBlock> {
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
    private static class BlockAgeComparator implements Comparator<PartiallyMinedBlock> {
        @Override
        public int compare(PartiallyMinedBlock b1, PartiallyMinedBlock b2) {
            return Long.compare(b1.getLastTimeMined(), b2.getLastTimeMined());
        }
    }

    /**
     * each dimension must have their own block regen queue, else we try accessing blocks that do not exist
     */
    private BlockRegenQueue getBlockRegenQueueForDimension(ResourceKey<Level> dimension) {
        BlockRegenQueue queueForDimension = blockRegenQueuesByDimension.get(dimension);
        if (queueForDimension == null) {
            queueForDimension = new BlockRegenQueue(30, new BlockAgeComparator());
            blockRegenQueuesByDimension.put(dimension, queueForDimension);
        }
        return queueForDimension;
    }
}
