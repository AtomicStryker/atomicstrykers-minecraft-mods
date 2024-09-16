package atomicstryker.multimine.common;

import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class MultiMineServer implements ISidedProxy {
    private static MultiMineServer instance;
    private static MinecraftServer serverInstance;
    private final HashMap<ResourceKey<Level>, List<PartiallyMinedBlock>> partiallyMinedBlocksListByDimension;
    private final HashMap<ResourceKey<Level>, BlockRegenQueue> blockRegenQueuesByDimension;

    /**
     * any block destroyed by multi mine has its coordinates entered into a blacklist for 10 ticks,
     * during which Multi Mine will block any further block destruction.
     * this is to prevent race conditions with vanilla and other mod interactions
     */
    private final HashMap<ResourceKey<Level>, HashMap<BlockPos, Integer>> blocksRecentlyDestroyedByWorld;

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
        blocksRecentlyDestroyedByWorld = Maps.newHashMap();
    }

    @Override
    public void commonSetup() {
        // no proxy action needed
    }

    @Override
    public void handlePartialBlockPacket(PartialBlockPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(packet.user());
            if (p != null) {
                onClientSentPartialBlockPacket(p, packet.x(), packet.y(), packet.z(), packet.value());
            }
        });
    }

    @Override
    public void handlePartialBlockRemovalPacket(PartialBlockRemovalPacket payload, IPayloadContext context) {
        // never called
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
        serverInstance = ServerLifecycleHooks.getCurrentServer();
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
                sendPartiallyMinedBlockUpdateToAllPlayers(iterBlock, false, (ServerLevel) player.level());

                if (iterBlock.isFinished() && !player.level().getBlockState(pos).isAir()) {
                    MultiMine.instance().debugPrint("Server popping, then forgetting block at: [{}|{}|{}]", x, y, z);

                    // see net.minecraft.server.level.ServerPlayerGameMode.handleBlockBreakAction
                    // popping the block serverside is necessary as MC now keeps destroyProgress on serverside
                    player.gameMode.destroyBlock(pos);

                    HashMap<BlockPos, Integer> blocksRecentlyDestroyed = blocksRecentlyDestroyedByWorld
                            .computeIfAbsent(dimension, k -> Maps.newHashMap());
                    // multi mine bans the block coordinates from further destruction for a short time
                    blocksRecentlyDestroyed.put(pos, 10);

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
        sendPartiallyMinedBlockUpdateToAllPlayers(newblock, false, (ServerLevel) player.level());
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        HashMap<BlockPos, Integer> blocksRecentlyDestroyed = blocksRecentlyDestroyedByWorld
                .computeIfAbsent(event.getPlayer().level().dimension(), k -> Maps.newHashMap());
        /*
         * any block destroyed by multi mine has its coordinates entered into a blacklist for 10 ticks,
         * during which Multi Mine will block any further block destruction.
         * this is to prevent race conditions with vanilla and other mod interactions
         */
        if (blocksRecentlyDestroyed.containsKey(event.getPos())) {
            event.setCanceled(true);
        }
    }

    private boolean isBlockBanned(BlockState blockState) {
        String blockIdentifier = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
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

        String ident = BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
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
        PartialBlockRemovalPacket packet = new PartialBlockRemovalPacket(block.getPos().getX(), block.getPos().getY(), block.getPos().getZ());
        PacketDistributor.sendToAllPlayers(packet);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getEntity();
        ResourceKey<Level> dimensionKey = player.level().dimension();
        final List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimensionKey);
        if (partiallyMinedBlocks != null) {
            for (PartiallyMinedBlock block : partiallyMinedBlocks) {
                instance().sendPartiallyMinedBlockToPlayer((ServerPlayer) player, block);
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
     */
    private void sendPartiallyMinedBlockUpdateToAllPlayers(PartiallyMinedBlock block, boolean regenerating, ServerLevel world) {
        PartialBlockPacket packet = new PartialBlockPacket("server", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), block.getProgress(), regenerating);
        PacketDistributor.sendToPlayersInDimension(world, packet);
    }

    /**
     * Sends a partial Block Packet to a particular player.
     *
     * @param p     Player targeted
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockToPlayer(ServerPlayer p, PartiallyMinedBlock block) {
        PartialBlockPacket packet = new PartialBlockPacket("server", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), block.getProgress(), false);
        PacketDistributor.sendToPlayer(p, packet);
    }

    @SubscribeEvent
    public void commonSetup(ServerStartedEvent evt) {
        // dedicated server starting point
        MultiMine.LOGGER.info("MultiMine ServerStartedEvent");
        MultiMine.instance().initIfNeeded(evt.getServer().getAllLevels().iterator().next());
    }

    /**
     * Tick Handler to achieve Block Regeneration. We keep track of our Block
     * age using a PriorityQueue and start repairing Blocks if they get too old.
     */
    @SubscribeEvent
    public void onTick(LevelTickEvent.Post tick) {
        if (tick.getLevel().isClientSide()) {
            return;
        }

        HashMap<BlockPos, Integer> blocksRecentlyDestroyed = blocksRecentlyDestroyedByWorld
                .computeIfAbsent(tick.getLevel().dimension(), k -> Maps.newHashMap());
        Iterator<Map.Entry<BlockPos, Integer>> iterator = blocksRecentlyDestroyed.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            int remainingTicks = entry.getValue() - 1;
            if (remainingTicks > 0) {
                entry.setValue(remainingTicks);
            } else {
                iterator.remove();
                MultiMine.instance().debugPrint("Server Blacklist timed out: [{}|{}|{}]",
                        entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
            }
        }

        BlockRegenQueue queueForDimension = getBlockRegenQueueForDimension(tick.getLevel().dimension());
        if (queueForDimension.isEmpty()) {
            return;
        }

        PartiallyMinedBlock block;
        for (Iterator<PartiallyMinedBlock> iter = queueForDimension.iterator(); iter.hasNext(); ) {
            block = iter.next();
            if (isBlockGone(block)) {
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
                sendPartiallyMinedBlockUpdateToAllPlayers(block, true, (ServerLevel) tick.getLevel());
                queueForDimension.add(block);
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
        return serverInstance.getLevel(block.getDimension()).isEmptyBlock(block.getPos());
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
