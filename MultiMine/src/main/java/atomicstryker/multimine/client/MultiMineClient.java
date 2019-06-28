package atomicstryker.multimine.client;

import atomicstryker.multimine.common.MultiMine;
import atomicstryker.multimine.common.PartiallyMinedBlock;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.Map;

public class MultiMineClient {
    private static MultiMineClient instance;
    private static Minecraft mc;
    private static EntityPlayer thePlayer;
    private final PartiallyMinedBlock[] partiallyMinedBlocksArray;
    private Map<Integer, DestroyBlockProgress> vanillaDestroyBlockProgressMapLazy;
    private int arrayOverWriteIndex;
    private BlockPos curBlock;
    private float lastBlockCompletion;
    private int lastCloudTickReading;

    /**
     * Client instance of Multi Mine Mod. Keeps track of whether or not the current Server has the Mod,
     * the current Block being mined, and hacks into the vanilla "partially Destroyed Blocks" RenderMap.
     * Also handles Packets sent from server to announce other people's damaged Blocks.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MultiMineClient() {
        instance = this;
        mc = Minecraft.getInstance();
        partiallyMinedBlocksArray = new PartiallyMinedBlock[30];
        arrayOverWriteIndex = 0;
        curBlock = BlockPos.ORIGIN;
        lastBlockCompletion = 0F;
        lastCloudTickReading = 0;
    }

    public static MultiMineClient instance() {
        return instance;
    }

    /**
     * Called by the transformed PlayerControllerMP, has the ability to override the internal block mine completion with another value
     *
     * @param pos             BlockPos instance being hit
     * @param blockCompletion mine completion value as currently held by the controller. Values >= 1.0f trigger block breaking
     * @return value to override blockCompletion in PlayerControllerMP with
     */
    @SuppressWarnings("unused")
    public float eventPlayerDamageBlock(BlockPos pos, float blockCompletion) {
        thePlayer = mc.player;

        /*
         * completely disable multi mine on blocks with custom breaking, such as
         * skulls, chests, signs
         */
        IBlockState state = thePlayer.world.getBlockState(pos);
        if (state.hasCustomBreakingProgress()) {
            return blockCompletion;
        }

        boolean cachedProgressWasAhead = false;
        // see if we have multimine completion cached somewhere
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++) {
            if (partiallyMinedBlocksArray[i] != null && partiallyMinedBlocksArray[i].getPos().equals(pos)) {
                float savedProgress = partiallyMinedBlocksArray[i].getProgress();
                MultiMine.instance().debugPrint("found cached block at index {}, cached: {}, completion: {}", i, savedProgress, blockCompletion);
                if (savedProgress > blockCompletion) {
                    lastBlockCompletion = savedProgress;
                    cachedProgressWasAhead = true;
                }
                break;
            }
        }

        if (!cachedProgressWasAhead) {
            if (!curBlock.equals(pos)) {
                // setup new block values
                curBlock = pos;
                lastBlockCompletion = blockCompletion;
            } else if (blockCompletion > lastBlockCompletion) {
                MultiMine.instance().debugPrint("Client has block progress for: [{}], actual completion: {}, lastCompletion: {}", pos, blockCompletion, lastBlockCompletion);
                MultiMine.instance().networkHelper.sendPacketToServer(new PartialBlockPacket(thePlayer.getName().getString(), curBlock.getX(), curBlock.getY(), curBlock.getZ(), blockCompletion, false));
                MultiMine.instance().debugPrint("Sent block progress packet to server: {}", blockCompletion);
                lastBlockCompletion = blockCompletion;
                updateLocalPartialBlock(curBlock.getX(), curBlock.getY(), curBlock.getZ(), blockCompletion, false);
            }
        }

        return lastBlockCompletion;
    }

    /**
     * Helper method to emulate the Digging Particles created when a player mines a Block. This usually runs every tick while mining
     *
     * @param x coordinate of Block being mined
     * @param y coordinate of Block being mined
     * @param z coordinate of Block being mined
     */
    private void renderBlockDigParticles(int x, int y, int z) {
        World world = thePlayer.world;
        BlockPos bp = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(bp);
        Block block = state.getBlock();
        if (block != Blocks.AIR) {
            SoundType soundtype = block.getSoundType(state, world, bp, thePlayer);
            mc.getSoundHandler().play(new SimpleSound(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, bp));
            mc.particles.addBlockDestroyEffects(new BlockPos(x, y, z), state);
        }
    }

    /**
     * Called when a server informs the client about new Block progress. See if it exists locally and update, else add it.
     *
     * @param x            coordinate of Block
     * @param y            coordinate of Block
     * @param z            coordinate of Block
     * @param progress     of Block Mining, float 0 to 1
     * @param regenerating
     */
    public void onServerSentPartialBlockData(int x, int y, int z, float progress, boolean regenerating) {
        if (thePlayer == null) {
            return;
        }

        MultiMine.instance().debugPrint("Client received partial Block packet for: [{}|{}|{}], progress now: {}, regen: {}", x, y, z, progress, regenerating);
        updateLocalPartialBlock(x, y, z, progress, regenerating);
    }

    private void updateLocalPartialBlock(int x, int y, int z, float progress, boolean regenerating) {
        updateCloudTickReading();

        EntityPlayer player = thePlayer;
        World w = player.world;
        BlockPos pos = new BlockPos(x, y, z);
        final IBlockState block = w.getBlockState(pos);

        final PartiallyMinedBlock newBlock = new PartiallyMinedBlock(x, y, z, thePlayer.dimension.getId(), progress);
        PartiallyMinedBlock iterBlock;
        int freeIndex = -1;

        if (regenerating && pos.equals(curBlock)) {
            lastBlockCompletion = progress;
        }

        for (int i = 0; i < partiallyMinedBlocksArray.length; i++) {
            iterBlock = partiallyMinedBlocksArray[i];
            if (iterBlock == null && freeIndex == -1) {
                freeIndex = i;
            } else if (newBlock.equals(iterBlock)) {
                boolean notClientsBlock = false;
                // if other guy's progress advances, render digging
                if (iterBlock.getProgress() < progress && !iterBlock.getPos().equals(pos)) {
                    renderBlockDigParticles(x, y, z);
                    notClientsBlock = true;
                }
                MultiMine.instance().debugPrint("Client updating local partial block [{}|{}|{}], at index {}, notClientsBlock: {}, setting progres from {} to {}", x, y, z, i, notClientsBlock,
                        iterBlock.getProgress(), progress);

                iterBlock.setProgress(progress);
                final DestroyBlockProgress newDestroyBP = new DestroyBlockProgress(0, iterBlock.getPos());
                newDestroyBP.setPartialBlockDamage(Math.min(9, Math.round(10f * iterBlock.getProgress())));
                newDestroyBP.setCloudUpdateTick(lastCloudTickReading);
                getVanillaRenderMap().put(i, newDestroyBP);

                if (iterBlock.isFinished()) {
                    w.sendBlockBreakProgress(player.getEntityId(), pos, -1);

                    if (block.getBlock() != Blocks.AIR) {
                        IBlockState is = w.getBlockState(pos);
                        if (!notClientsBlock && block.getBlock().removedByPlayer(is, w, pos, player, true, w.getFluidState(pos))) {
                            block.getBlock().onPlayerDestroy(w, pos, block);
                            block.getBlock().harvestBlock(w, player, pos, block, w.getTileEntity(pos), player.getHeldItemMainhand());
                        }

                        SoundType st = block.getBlock().getSoundType(is, w, iterBlock.getPos(), player);
                        if (st != null) {
                            w.playSound(null, pos, st.getBreakSound(), SoundCategory.BLOCKS, st.getVolume() + 1.0F / 2.0F, st.getPitch() * 0.8F);
                        }
                    }
                    onBlockMineFinishedDamagePlayerItem(player, x, y, z);

                    getVanillaRenderMap().remove(i);
                    partiallyMinedBlocksArray[i] = null;
                    if (curBlock.getX() == x && curBlock.getY() == y && curBlock.getZ() == z) {
                        curBlock = BlockPos.ORIGIN;
                    }
                    MultiMine.instance().debugPrint("Client wiped local finished block [{}|{}|{}], at index {}", x, y, z, i);
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
    private void onBlockMineFinishedDamagePlayerItem(EntityPlayer player, int x, int y, int z) {
        if (x != this.curBlock.getX() || y != curBlock.getY() || z != curBlock.getZ()) {
            return;
        }

        ItemStack itemStack = player.getHeldItemMainhand();
        BlockPos pos = new BlockPos(x, y, z);
        itemStack.onBlockDestroyed(player.world, player.world.getBlockState(pos), pos, player);
        if (itemStack.getCount() == 0) {
            player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    /**
     * Called by the server via packet if there is more than the allowed amount of concurrent partial Blocks
     * in play. Causes the client to delete the corresponding local Block.
     */
    public void onServerSentPartialBlockDeleteCommand(BlockPos p) {
        MultiMine.instance().debugPrint("Server sent partial delete command for [{}|{}|{}]", p.getX(), p.getY(), p.getZ());
        if (curBlock.equals(p)) {
            MultiMine.instance().debugPrint("was current block, wiping that!");
            curBlock = BlockPos.ORIGIN;
            lastBlockCompletion = 0F;
        }
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++) {
            if (partiallyMinedBlocksArray[i] != null && partiallyMinedBlocksArray[i].getPos().equals(p)) {
                partiallyMinedBlocksArray[i] = null;
                getVanillaRenderMap().remove(i);
                MultiMine.instance().debugPrint("Server sent partial delete matched at index {}, deleted!", i);
                break;
            }
        }
    }

    private void updateCloudTickReading() {
        // cache previous object
        DestroyBlockProgress dbp = getVanillaRenderMap().get(0);

        // execute code which gets the object assigned the private cloud tick value we want
        mc.worldRenderer.sendBlockBreakProgress(0, new BlockPos((int) thePlayer.posX, (int) thePlayer.posY, (int) thePlayer.posZ), 1);

        // read the needed value
        lastCloudTickReading = getVanillaRenderMap().get(0).getCreationCloudUpdateTick();

        // execute code which destroys the helper object
        mc.worldRenderer.sendBlockBreakProgress(0, new BlockPos((int) thePlayer.posX, (int) thePlayer.posY, (int) thePlayer.posZ), 10);

        // if necessary restore previous object
        if (dbp != null) {
            getVanillaRenderMap().put(0, dbp);
        }
        // System.out.println("lastCloudTickReading is now "+lastCloudTickReading);
    }

    private Map<Integer, DestroyBlockProgress> getVanillaRenderMap() {
        if (vanillaDestroyBlockProgressMapLazy != null) {
            return vanillaDestroyBlockProgressMapLazy;
        }
        MultiMine.instance().debugPrint("Multi Mine about to hack vanilla RenderMap");
        for (Field f : WorldRenderer.class.getDeclaredFields()) {
            if (f.getType().equals(Map.class)) {
                f.setAccessible(true);
                try {
                    vanillaDestroyBlockProgressMapLazy = (Map) f.get(mc.worldRenderer);
                    MultiMine.instance().debugPrint("Multi Mine vanilla RenderMap invasion successful, field: " + f.getName());
                    return vanillaDestroyBlockProgressMapLazy;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        throw new RuntimeException("rendermap hack failed");
    }
}
