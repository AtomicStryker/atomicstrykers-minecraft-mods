package atomicstryker.multimine.client;

import java.lang.reflect.Field;
import java.util.Map;

import atomicstryker.multimine.common.BlockPos;
import atomicstryker.multimine.common.MultiMine;
import atomicstryker.multimine.common.PartiallyMinedBlock;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MultiMineClient
{
    private static MultiMineClient instance;
    private static Minecraft mc;
    private static EntityPlayer thePlayer;
    private final PartiallyMinedBlock[] partiallyMinedBlocksArray;
    private Map<Integer, DestroyBlockProgress> vanillaDestroyBlockProgressMap;
    private int arrayOverWriteIndex;
    private float lastBlockCompletion;
    private int lastCloudTickReading;
    private BlockPos curBlock = new BlockPos();

    /**
     * Client instance of Multi Mine Mod. Keeps track of whether or not the
     * current Server has the Mod, the current Block being mined, and hacks into
     * the vanilla "partially Destroyed Blocks" RenderMap. Also handles Packets
     * sent from server to announce other people's damaged Blocks.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MultiMineClient()
    {
        instance = this;
        mc = FMLClientHandler.instance().getClient();
        partiallyMinedBlocksArray = new PartiallyMinedBlock[30];
        arrayOverWriteIndex = 0;
        lastBlockCompletion = 0F;
        lastCloudTickReading = 0;

        MultiMine.instance().debugPrint("Multi Mine about to hack vanilla RenderMap");
        for (Field f : RenderGlobal.class.getDeclaredFields())
        {
            if (f.getType().equals(Map.class))
            {
                f.setAccessible(true);
                try
                {
                    vanillaDestroyBlockProgressMap = (Map) f.get(mc.renderGlobal);
                    MultiMine.instance().debugPrint("Multi Mine vanilla RenderMap invasion successful, field: " + f.getName());
                    break;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static MultiMineClient instance()
    {
        return instance;
    }

    /**
     * Called by the transformed PlayerControllerMP, has the ability to override
     * the internal block mine completion with another value
     *
     * @param pos
     *            BlockPos instance being hit
     * @param blockCompletion
     *            mine completion value as currently held by the controller.
     *            Values >= 1.0f trigger block breaking
     * @return value to override blockCompletion in PlayerControllerMP with
     */
    @SuppressWarnings("unused")
    public float eventPlayerDamageBlock(int x, int y, int z, float blockCompletion)
    {
        BlockPos pos = new BlockPos(x, y, z);
        thePlayer = FMLClientHandler.instance().getClient().thePlayer;
        boolean cachedProgressWasAhead = false;
        // see if we have multimine completion cached somewhere
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++)
        {
            if (partiallyMinedBlocksArray[i] != null && partiallyMinedBlocksArray[i].getPos().equals(pos))
            {
                float savedProgress = partiallyMinedBlocksArray[i].getProgress();
                MultiMine.instance().debugPrint("found cached block at index " + i + ", cached: " + savedProgress + ", completion: " + blockCompletion);
                if (savedProgress > blockCompletion)
                {
                    lastBlockCompletion = savedProgress;
                    cachedProgressWasAhead = true;
                }
                break;
            }
        }

        if (!cachedProgressWasAhead)
        {
            if (!curBlock.equals(pos))
            {
                // setup new block values
                curBlock = pos;
                lastBlockCompletion = blockCompletion;
            }
            else if (blockCompletion > lastBlockCompletion)
            {
                MultiMine.instance().debugPrint("Client has block progress for: [" + pos + "], actual completion: " + blockCompletion + ", lastCompletion: " + lastBlockCompletion);
                MultiMine.instance().networkHelper.sendPacketToServer(new PartialBlockPacket(thePlayer.getCommandSenderName(), curBlock.x, curBlock.y, curBlock.z, blockCompletion));
                MultiMine.instance().debugPrint("Sent block progress packet to server: " + blockCompletion);
                lastBlockCompletion = blockCompletion;
                updateLocalPartialBlock(curBlock.x, curBlock.y, curBlock.z, blockCompletion);
            }
        }

        return lastBlockCompletion;
    }

    /**
     * Helper method to emulate the Digging Particles created when a player
     * mines a Block. This usually runs every tick while mining
     *
     * @param x
     *            coordinate of Block being mined
     * @param y
     *            coordinate of Block being mined
     * @param z
     *            coordinate of Block being mined
     */
    private void renderBlockDigParticles(int x, int y, int z)
    {
        World world = thePlayer.worldObj;
        Block block = world.getBlock(x, y, z);
        if (block != Blocks.air)
        {
            int blockMeta = world.getBlockMetadata(x, y, z);
            mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F,
                    block.stepSound.getPitch() * 0.8F, x + 0.5f, y + 0.5f, z + 0.5f));
            mc.effectRenderer.addBlockDestroyEffects(x, y, z, block, blockMeta);
        }
    }

    /**
     * Called when a server informs the client about new Block progress. See if
     * it exists locally and update, else add it.
     *
     * @param x
     *            coordinate of Block
     * @param y
     *            coordinate of Block
     * @param z
     *            coordinate of Block
     * @param progress
     *            of Block Mining, float 0 to 1
     */
    public void onServerSentPartialBlockData(int x, int y, int z, float progress)
    {
        if (thePlayer == null)
        {
            return;
        }

        MultiMine.instance().debugPrint("Client received partial Block packet for: [" + x + "|" + y + "|" + z + "], progress now: " + progress);
        updateLocalPartialBlock(x, y, z, progress);
    }

    private void updateLocalPartialBlock(int x, int y, int z, float progress)
    {
        updateCloudTickReading();

        EntityPlayer player = thePlayer;
        World w = player.worldObj;
        BlockPos pos = new BlockPos(x, y, z);
        final Block block = w.getBlock(x, y, z);
        final int meta = w.getBlockMetadata(x, y, z);

        final PartiallyMinedBlock newBlock = new PartiallyMinedBlock(x, y, z, thePlayer.dimension, progress);
        PartiallyMinedBlock iterBlock;
        int freeIndex = -1;
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++)
        {
            iterBlock = partiallyMinedBlocksArray[i];
            if (iterBlock == null && freeIndex == -1)
            {
                freeIndex = i;
            }
            else if (newBlock.equals(iterBlock))
            {
                boolean notClientsBlock = false;
                // if other guy's progress advances, render digging
                if (iterBlock.getProgress() < progress && !iterBlock.getPos().equals(pos))
                {
                    renderBlockDigParticles(x, y, z);
                    notClientsBlock = true;
                }
                MultiMine.instance().debugPrint("Client updating local partial block [" + x + "|" + y + "|" + z + "], at index " + i + ", notClientsBlock: " + notClientsBlock
                        + ", setting progres from " + iterBlock.getProgress() + " to " + progress);

                iterBlock.setProgress(progress);
                final DestroyBlockProgress newDestroyBP = new DestroyBlockProgress(0, iterBlock.getPos().x, iterBlock.getPos().y, iterBlock.getPos().z);
                newDestroyBP.setPartialBlockDamage(Math.min(9, Math.round(10f * iterBlock.getProgress())));
                newDestroyBP.setCloudUpdateTick(lastCloudTickReading);
                vanillaDestroyBlockProgressMap.put(i, newDestroyBP);

                if (iterBlock.isFinished())
                {
                    w.destroyBlockInWorldPartially(player.getEntityId(), x, y, z, -1);

                    if (block != Blocks.air)
                    {
                        if (!notClientsBlock && block.removedByPlayer(w, player, x, y, z, true))
                        {
                            block.onBlockDestroyedByPlayer(w, x, y, z, meta);
                            block.harvestBlock(w, player, x, y, z, meta);
                        }
                        
                        w.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + meta << 12);
                        w.playSound(x+0.5D, y+0.5D, z+0.5D, block.stepSound.getBreakSound(), 
                                (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F, false);
                    }
                    onBlockMineFinishedDamagePlayerItem(player, x, y, z);

                    vanillaDestroyBlockProgressMap.remove(i);
                    partiallyMinedBlocksArray[i] = null;
                    if (curBlock.x == x && curBlock.y == y && curBlock.z == z)
                    {
                        curBlock.x = 0;
                        curBlock.y = 0;
                        curBlock.z = 0;
                    }
                    MultiMine.instance().debugPrint("Client wiped local finished block [" + x + "|" + y + "|" + z + "], at index " + i);
                }
                return;
            }
        }

        if (progress > 0.99)
        {
            MultiMine.instance().debugPrint("Client ignoring late arrival packet [" + x + "|" + y + "|" + z + "]");
            return;
        }

        if (freeIndex != -1)
        {
            partiallyMinedBlocksArray[freeIndex] = newBlock;
        }
        else
        {
            partiallyMinedBlocksArray[arrayOverWriteIndex++] = newBlock;
            if (arrayOverWriteIndex == partiallyMinedBlocksArray.length)
            {
                arrayOverWriteIndex = 0;
            }
        }
    }

    /**
     * Helper method to emulate vanilla behaviour of damaging your Item as you
     * finish mining a Block.
     *
     * @param player
     *            Player doing the mining
     * @param x
     *            Coordinates of the Block
     * @param y
     *            Coordinates of the Block
     * @param z
     *            Coordinates of the Block
     */
    private void onBlockMineFinishedDamagePlayerItem(EntityPlayer player, int x, int y, int z)
    {
        if (x != this.curBlock.x || y != curBlock.y || z != curBlock.z)
        {
            return;
        }

        ItemStack itemStack = player.getCurrentEquippedItem();
        if (itemStack != null)
        {
            itemStack.func_150999_a(player.worldObj, player.worldObj.getBlock(x, y, z), x, y, z, player);
            if (itemStack.stackSize == 0)
            {
                player.destroyCurrentEquippedItem();
            }
        }
    }

    /**
     * Called by the server via packet if there is more than the allowed amount
     * of concurrent partial Blocks in play. Causes the client to delete the
     * corresponding local Block.
     */
    public void onServerSentPartialBlockDeleteCommand(int x, int y, int z)
    {
        BlockPos p = new BlockPos(x, y, z);
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++)
        {
            if (partiallyMinedBlocksArray[i] != null && partiallyMinedBlocksArray[i].getPos().equals(p))
            {
                partiallyMinedBlocksArray[i] = null;
                vanillaDestroyBlockProgressMap.remove(i);
                break;
            }
        }
    }

    private void updateCloudTickReading()
    {
        // cache previous object
        DestroyBlockProgress dbp = vanillaDestroyBlockProgressMap.get(0);

        // execute code which gets the object assigned the private cloud tick
        // value we want
        mc.renderGlobal.destroyBlockPartially(0, (int) thePlayer.posX, (int) thePlayer.posY, (int) thePlayer.posZ, 1);

        // read the needed value
        lastCloudTickReading = vanillaDestroyBlockProgressMap.get(0).getCreationCloudUpdateTick();

        // execute code which destroys the helper object
        mc.renderGlobal.destroyBlockPartially(0, (int) thePlayer.posX, (int) thePlayer.posY, (int) thePlayer.posZ, 10);

        // if necessary restore previous object
        if (dbp != null)
        {
            vanillaDestroyBlockProgressMap.put(0, dbp);
        }
        // System.out.println("lastCloudTickReading is now
        // "+lastCloudTickReading);
    }
}
