package atomicstryker.multimine.client;

import atomicstryker.multimine.common.ISidedProxy;
import atomicstryker.multimine.common.MultiMine;
import atomicstryker.multimine.common.PartiallyMinedBlock;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.File;
import java.lang.reflect.Field;

@EventBusSubscriber(value = Dist.CLIENT, modid = MultiMine.MOD_ID)
public class MultiMineClient implements ISidedProxy {
    private static MultiMineClient instance = null;
    private static Minecraft mc;
    private static Player thePlayer;
    private final PartiallyMinedBlock[] partiallyMinedBlocksArray = new PartiallyMinedBlock[30];

    // reflects into: float MultiPlayerGameMode.destroyProgress
    private Field vanillaDestroyProgressField = null;

    private int arrayOverWriteIndex;
    private BlockPos lastClickedBlock;
    private float lastBlockCompletion;

    /**
     * Client instance of Multi Mine Mod. Keeps track of whether or not the current Server has the Mod,
     * the current Block being mined, and hacks into the vanilla "partially Destroyed Blocks" RenderMap.
     * Also handles Packets sent from server to announce other people's damaged Blocks.
     */
    @Override
    public void commonSetup() {
        MultiMine.LOGGER.info("MultiMineClient initializing");
        arrayOverWriteIndex = 0;
        lastClickedBlock = BlockPos.ZERO;
        lastBlockCompletion = 0F;
        instance = this;
    }

    @Override
    public void handlePartialBlockPacket(PartialBlockPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                onServerSentPartialBlockData(packet.x(), packet.y(), packet.z(), packet.value(), packet.regenerating());
            } else {
                // client is also receiving singleplayer/localhost server packets, reroute these
                MultiMine.instance().getServer().onClientSentPartialBlockPacket((ServerPlayer) context.player(), packet.x(), packet.y(), packet.z(), packet.value());
            }
        });
    }

    @Override
    public void handlePartialBlockRemovalPacket(PartialBlockRemovalPacket payload, IPayloadContext context) {
        context.enqueueWork(() ->
                onServerSentPartialBlockDeleteCommand(new BlockPos(payload.x(), payload.y(), payload.z())));
    }

    public static MultiMineClient instance() {
        return instance;
    }

    @SubscribeEvent
    public static void playerLoginToServer(ClientPlayerNetworkEvent.LoggingIn evt) {
        mc = Minecraft.getInstance();
        MultiMine.LOGGER.info("MultiMineClient playerLoginToServer: " + evt.getPlayer());
        MultiMine.instance().initIfNeeded(evt.getPlayer().level());
    }

    public static File getMcFolder() {
        return Minecraft.getInstance().gameDirectory;
    }

    @SubscribeEvent
    public static void onClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        instance().onClickBlockInstance(event);
    }

    private void onClickBlockInstance(PlayerInteractEvent.LeftClickBlock event) {

        if (!event.getEntity().level().isClientSide) {
            // only clientside pls
            return;
        }

        thePlayer = event.getEntity();
        BlockPos pos = event.getPos();

        if (!destroyProgressFieldFound()) {
            // on the very first blockbreak tick, we cant tell which is the target field
            MultiMine.instance().debugPrint("reflection into destroyProgress not ready, aborting");
            return;
        }

        BlockEntity blockentity = thePlayer.level().getBlockEntity(pos);
        if (blockentity instanceof Container) {
            // if its any kind of chest or container, just nope out
            MultiMine.instance().debugPrint("aborting because its a container block");
            return;
        }

        // use reflection to read MultiPlayerGameMode.destroyProgress
        float destroyProgressVanilla = getVanillaDestroyProgressValue();
        MultiMine.instance().debugPrint("client {} clicked block {}, destroyProgress {}, lastBlockCompletion {}", thePlayer, pos, destroyProgressVanilla, lastBlockCompletion);

        boolean cachedProgressWasAhead = false;
        // see if we have multimine completion cached somewhere
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++) {
            if (partiallyMinedBlocksArray[i] != null && partiallyMinedBlocksArray[i].getPos().equals(pos)) {
                float savedProgress = partiallyMinedBlocksArray[i].getProgress();
                MultiMine.instance().debugPrint("found cached destroyProgress at index {}, cached: {}, mc: {}", i, savedProgress, destroyProgressVanilla);
                if (savedProgress > destroyProgressVanilla) {
                    lastBlockCompletion = savedProgress;
                    cachedProgressWasAhead = true;
                }
                break;
            }
        }

        if (!cachedProgressWasAhead) {
            if (!lastClickedBlock.equals(pos)) {
                // setup new block values
                MultiMine.instance().debugPrint("client is destroying new block, was {} and now {}", lastClickedBlock, pos);
                lastClickedBlock = pos;
                lastBlockCompletion = 0;
            } else if (destroyProgressVanilla > lastBlockCompletion) {
                MultiMine.instance().debugPrint("client has block progress for: [{}], actual completion: {}, lastCompletion: {}", pos, destroyProgressVanilla, lastBlockCompletion);
                PartialBlockPacket partialBlockPacket = new PartialBlockPacket(thePlayer.getScoreboardName(), lastClickedBlock.getX(), lastClickedBlock.getY(), lastClickedBlock.getZ(), destroyProgressVanilla, false);
                PacketDistributor.sendToServer(partialBlockPacket);
                MultiMine.instance().debugPrint("sent block progress packet to server: {}", destroyProgressVanilla);
                lastBlockCompletion = destroyProgressVanilla;
                updateLocalPartialBlock(lastClickedBlock.getX(), lastClickedBlock.getY(), lastClickedBlock.getZ(), destroyProgressVanilla, false);
            }
        } else {
            // use reflection to overwrite MultiPlayerGameMode.destroyProgress with lastBlockCompletion
            MultiMine.instance().debugPrint("overriding client destroyProgress {} with multi mine cache {}", destroyProgressVanilla, lastBlockCompletion);
            setDestroyProgressValue(lastBlockCompletion);
        }
    }

    private boolean destroyProgressFieldFound() {

        if (mc.gameMode == null) {
            return false;
        }

        if (vanillaDestroyProgressField != null) {
            return true;
        }

        for (Field f : MultiPlayerGameMode.class.getDeclaredFields()) {
            if (f.getType().equals(float.class)) {
                try {
                    f.setAccessible(true);
                    float value = (float) f.get(mc.gameMode);
                    // on second break tick, the class field will have a uniquely small float value
                    if (value > 0 && value < 1.0F) {
                        vanillaDestroyProgressField = f;
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("vanillaDestroyProgressField read failure", e);
                }
            }
        }

        return false;
    }

    private float getVanillaDestroyProgressValue() {
        try {
            return (float) vanillaDestroyProgressField.get(mc.gameMode);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("vanillaDestroyProgressField read failure", e);
        }
    }

    private void setDestroyProgressValue(float lastBlockCompletion) {
        try {
            vanillaDestroyProgressField.set(mc.gameMode, lastBlockCompletion);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("vanillaDestroyProgressField write failure", e);
        }
    }

    /**
     * Helper method to emulate the Digging Particles created when a player mines a Block. This usually runs every tick while mining
     *
     * @param x coordinate of Block being mined
     * @param y coordinate of Block being mined
     * @param z coordinate of Block being mined
     */
    private void renderBlockDigParticles(int x, int y, int z) {
        Level world = thePlayer.level();
        BlockPos bp = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(bp);
        Block block = state.getBlock();
        if (block != Blocks.AIR) {
            SoundType soundtype = block.getSoundType(state, world, bp, thePlayer);
            mc.getSoundManager().play(new SimpleSoundInstance(soundtype.getHitSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, thePlayer.getRandom(), bp));
        }
        world.addDestroyBlockEffect(bp, state);
    }

    /**
     * Called when a server informs the client about new Block progress. See if it exists locally and update, else add it.
     *
     * @param x            coordinate of Block
     * @param y            coordinate of Block
     * @param z            coordinate of Block
     * @param progress     of Block Mining, float 0 to 1
     * @param regenerating true when the block mining progress is reversing
     */
    public void onServerSentPartialBlockData(int x, int y, int z, float progress, boolean regenerating) {
        if (thePlayer == null) {
            return;
        }

        MultiMine.instance().debugPrint("Client received partial Block packet for: [{}|{}|{}], progress now: {}, regen: {}", x, y, z, progress, regenerating);
        updateLocalPartialBlock(x, y, z, progress, regenerating);
    }

    private void updateLocalPartialBlock(int x, int y, int z, float progress, boolean regenerating) {

        BlockPos pos = new BlockPos(x, y, z);

        final PartiallyMinedBlock newBlock = new PartiallyMinedBlock(x, y, z, thePlayer.level().dimension(), progress);
        PartiallyMinedBlock iterBlock;
        int freeIndex = -1;

        if (regenerating && pos.equals(lastClickedBlock) && progress >= 0) {
            lastBlockCompletion = progress;
        }

        for (int arrayIndex = 0; arrayIndex < partiallyMinedBlocksArray.length; arrayIndex++) {
            iterBlock = partiallyMinedBlocksArray[arrayIndex];
            if (iterBlock == null && freeIndex == -1) {
                freeIndex = arrayIndex;
            } else if (newBlock.equals(iterBlock)) {

                if (progress < 0) {
                    MultiMine.instance().debugPrint("Client was told to forget progress for partial block [{}|{}|{}], at index {}, it is blacklisted", x, y, z, arrayIndex);
                    partiallyMinedBlocksArray[arrayIndex] = null;
                    return;
                }

                boolean notClientsBlock = false;
                // if other guy's progress advances, render digging
                if (iterBlock.getProgress() < progress && !iterBlock.getPos().equals(pos)) {
                    renderBlockDigParticles(x, y, z);
                    notClientsBlock = true;
                }
                MultiMine.instance().debugPrint("Client updating local partial block [{}|{}|{}], at index {}, notClientsBlock: {}, setting progress from {} to {}", x, y, z, arrayIndex, notClientsBlock,
                        iterBlock.getProgress(), progress);

                iterBlock.setProgress(progress);

                // last method called in MultiPlayerGameMode.startDestroyBlock
                mc.level.destroyBlockProgress(arrayIndex, iterBlock.getPos(), Math.min(9, Math.round(10f * iterBlock.getProgress())));

                if (iterBlock.isFinished()) {

                    // method called in MultiPlayerGameMode.startDestroyBlock, inside the >= 1.0F check if-branch
                    mc.gameMode.destroyBlock(pos);
                    // calling this vanilla method with parameter 10 (or -1) will wipe the visible damage cracks
                    mc.level.destroyBlockProgress(arrayIndex, iterBlock.getPos(), 10);
                    partiallyMinedBlocksArray[arrayIndex] = null;
                    if (lastClickedBlock.getX() == x && lastClickedBlock.getY() == y && lastClickedBlock.getZ() == z) {
                        lastClickedBlock = BlockPos.ZERO;
                    }
                    MultiMine.instance().debugPrint("Client wiped local finished block [{}|{}|{}], at index {}", x, y, z, arrayIndex);
                }
                return;
            }
        }

        if (progress > 0.99) {
            MultiMine.instance().debugPrint("Client ignoring late arrival packet [{}|{}|{}]", x, y, z);
            return;
        }

        if (freeIndex != -1) {
            partiallyMinedBlocksArray[freeIndex] = newBlock;
        } else {
            partiallyMinedBlocksArray[arrayOverWriteIndex++] = newBlock;
            if (arrayOverWriteIndex == partiallyMinedBlocksArray.length) {
                arrayOverWriteIndex = 0;
            }
        }
    }

    /**
     * Helper method to emulate vanilla behaviour of damaging your Item as you finish mining a Block.
     *
     * @param player Player doing the mining
     * @param x      Coordinates of the Block
     * @param y      Coordinates of the Block
     * @param z      Coordinates of the Block
     */
    private void onBlockMineFinishedDamagePlayerItem(Player player, int x, int y, int z) {
        if (x != this.lastClickedBlock.getX() || y != lastClickedBlock.getY() || z != lastClickedBlock.getZ()) {
            return;
        }

        ItemStack itemStack = player.getMainHandItem();
        BlockPos pos = new BlockPos(x, y, z);
        itemStack.mineBlock(player.level(), player.level().getBlockState(pos), pos, player);
        if (itemStack.getCount() == 0) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    /**
     * Called by the server via packet if there is more than the allowed amount of concurrent partial Blocks
     * in play. Causes the client to delete the corresponding local Block.
     */
    public void onServerSentPartialBlockDeleteCommand(BlockPos p) {
        if (mc.level == null) {
            return;
        }
        MultiMine.instance().debugPrint("Server sent partial delete command for [{}|{}|{}]", p.getX(), p.getY(), p.getZ());
        if (lastClickedBlock.equals(p)) {
            MultiMine.instance().debugPrint("was current block, wiping that!");
            lastClickedBlock = BlockPos.ZERO;
            lastBlockCompletion = 0F;
        }
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++) {
            if (partiallyMinedBlocksArray[i] != null && partiallyMinedBlocksArray[i].getPos().equals(p)) {
                // calling this vanilla method with parameter 10 (or -1) will wipe the visible damage cracks
                mc.level.destroyBlockProgress(i, partiallyMinedBlocksArray[i].getPos(), 10);
                partiallyMinedBlocksArray[i] = null;
                MultiMine.instance().debugPrint("Server sent partial delete matched at index {}, deleted!", i);
                break;
            }
        }
    }
}
