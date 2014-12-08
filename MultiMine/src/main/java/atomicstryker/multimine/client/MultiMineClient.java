package atomicstryker.multimine.client;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import atomicstryker.multimine.common.MultiMine;
import atomicstryker.multimine.common.PartiallyMinedBlock;
import atomicstryker.multimine.common.network.PartialBlockPacket;

public class MultiMineClient
{
    private static MultiMineClient instance;
    private static Minecraft mc;
    private static EntityPlayer thePlayer;
    private final PartiallyMinedBlock[] partiallyMinedBlocksArray;
    private Map<Integer, DestroyBlockProgress> vanillaDestroyBlockProgressMap;
    private int arrayOverWriteIndex;
    private int curBlockX;
    private int curBlockY;
    private int curBlockZ;
    private float lastBlockCompletion;
    private int lastCloudTickReading;
    
    /**
     * Client instance of Multi Mine Mod. Keeps track of whether or not the current Server has the Mod,
     * the current Block being mined, and hacks into the vanilla "partially Destroyed Blocks" RenderMap.
     * Also handles Packets sent from server to announce other people's damaged Blocks.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MultiMineClient()
    {
        instance = this;
        mc = FMLClientHandler.instance().getClient();
        curBlockX = curBlockY = curBlockZ = 0;
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
                    MultiMine.instance().debugPrint("Multi Mine vanilla RenderMap invasion successful, field: "+f.getName());
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
     * Called by the transformed PlayerControllerMP, has the ability to override the internal block mine completion with another value
     * @param pos BlockPos instance being hit
     * @param blockCompletion mine completion value as currently held by the controller. Values >= 1.0f trigger block breaking
     * @return value to override blockCompletion in PlayerControllerMP with
     */
    public float eventPlayerDamageBlock(BlockPos pos, float blockCompletion)
    {
        thePlayer = FMLClientHandler.instance().getClient().thePlayer;
        boolean partiallyMined = false;
        // see if we have multimine completion cached somewhere
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++)
        {
            if (partiallyMinedBlocksArray[i] != null
            && partiallyMinedBlocksArray[i].getPos().equals(pos))
            {
                float savedProgress = partiallyMinedBlocksArray[i].getProgress() * 0.1F;
                MultiMine.instance().debugPrint("found cached block, cached: "+savedProgress+", completion: "+blockCompletion);
                if (savedProgress > blockCompletion)
                {
                    blockCompletion = savedProgress;
                    lastBlockCompletion = savedProgress;
                    partiallyMined = true;
                }
                break;
            }
        }
        
        if (!partiallyMined)
        {
            // edge state optimization
            if (blockCompletion > 0.99f)
            {
                blockCompletion = 1.0f;
            }
            
            if (curBlockX != pos.getX() || curBlockY != pos.getY() || curBlockZ != pos.getZ())
            {
                // case block change, check one last time for partial mining
                while (blockCompletion >= lastBlockCompletion+0.1f)
                {
                    MultiMine.instance().networkHelper.sendPacketToServer(new PartialBlockPacket(thePlayer.getName(), curBlockX, curBlockY, curBlockZ, thePlayer.dimension));
                    lastBlockCompletion += 0.1f;
                }
                
                // setup new block values
                curBlockX = pos.getX();
                curBlockY = pos.getY();
                curBlockZ = pos.getZ();
                lastBlockCompletion = 0f;
            }
            else if (blockCompletion+0.1f >= lastBlockCompletion)
            {
                MultiMine.instance().debugPrint("Client has block progress for: ["+pos+"], actual completion: "+blockCompletion+", lastCompletion: "+lastBlockCompletion);
                // case same block, and mining has progressed
                while (blockCompletion >= lastBlockCompletion+0.1f)
                {
                    MultiMine.instance().networkHelper.sendPacketToServer(new PartialBlockPacket(thePlayer.getName(), curBlockX, curBlockY, curBlockZ, thePlayer.dimension));
                    MultiMine.instance().debugPrint("Sent one 10% block progress packet to server...");
                    lastBlockCompletion += 0.1f;
                }
            }
        }
        
        return blockCompletion;
    }
    
    /**
     * Helper method to emulate the Digging Particles created when a player mines a Block. This usually runs every tick while mining
     * @param x coordinate of Block being mined
     * @param y coordinate of Block being mined
     * @param z coordinate of Block being mined
     */
    public void renderBlockDigParticles(int x, int y, int z)
    {
        World world = thePlayer.worldObj;
        IBlockState state = world.getBlockState(new BlockPos(x, y, z));
        Block block = state.getBlock();
        if (block != Blocks.air)
        {
            mc.getSoundHandler().playSound(
                    new PositionedSoundRecord(
                            new ResourceLocation(block.stepSound.getBreakSound()), 
                            (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, x+0.5f, y+0.5f, z+0.5f));
            //mc.effectRenderer.addBlockDestroyEffects(x, y, z, block, blockMeta);
            mc.effectRenderer.func_180533_a(new BlockPos(x, y, z), state);
        }
    }

    /**
     * Called when a server informs the client about new Block progress. See if it exists locally and update, else add it.
     * @param x coordinate of Block
     * @param y coordinate of Block
     * @param z coordinate of Block
     * @param progress of Block Mining, range 1 to 10
     */
    public void onServerSentPartialBlockData(int x, int y, int z, int progress)
    {
        if (thePlayer == null)
        {
            return;
        }
        
        MultiMine.instance().debugPrint("Client received partial Block packet for: ["+x+"|"+y+"|"+z+"], progress now: "+progress);
        updateCloudTickReading();
        
        EntityPlayer player = thePlayer;
        World w = player.worldObj;
        BlockPos pos = new BlockPos(x, y, z);
        final IBlockState block = w.getBlockState(pos);
        
        int dimension = thePlayer.dimension;
        final PartiallyMinedBlock newBlock = new PartiallyMinedBlock(x, y, z, dimension, progress);
        PartiallyMinedBlock iterBlock;
        int freeIndex = -1;
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++)
        {
            iterBlock = partiallyMinedBlocksArray[i];
            if (iterBlock == null
            && freeIndex == -1)
            {
                freeIndex = i;
            }
            else if (newBlock.equals(iterBlock))
            {
                boolean notClientsBlock = false;
                // if other guy's progress advances, render digging
                if (iterBlock.getProgress() < progress
                && !iterBlock.getPos().equals(pos))
                {
                    renderBlockDigParticles(x, y, z);
                    notClientsBlock = true;
                }
                
                iterBlock.setProgress(progress);
                final DestroyBlockProgress newDestroyBP = new DestroyBlockProgress(0, iterBlock.getPos());
                newDestroyBP.setPartialBlockDamage(iterBlock.getProgress());
                newDestroyBP.setCloudUpdateTick(lastCloudTickReading);
                vanillaDestroyBlockProgressMap.put(i, newDestroyBP);
                
                if (iterBlock.isFinished())
                {
                    w.sendBlockBreakProgress(player.getEntityId(), pos, -1);
                    
                    if (block.getBlock() != Blocks.air)
                    {
                        if (!notClientsBlock && block.getBlock().removedByPlayer(w, pos, player, true))
                        {
                            block.getBlock().onBlockDestroyedByPlayer(w, pos, block);
                            block.getBlock().harvestBlock(w, player, pos, block, w.getTileEntity(pos));
                        }
						
                        //w.playAuxSFX(2001, x, y, z, blockID + meta << 12);
						w.playSound(x+0.5D, y+0.5D, z+0.5D, block.getBlock().stepSound.getBreakSound(), 
						        (block.getBlock().stepSound.getVolume() + 1.0F) / 2.0F, block.getBlock().stepSound.getFrequency() * 0.8F, false);
                    }
                    onBlockMineFinishedDamagePlayerItem(player, block.getBlock(), x, y, z);

                    vanillaDestroyBlockProgressMap.remove(i);
                    partiallyMinedBlocksArray[i] = null;
                }
                return;
            }
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
     * Helper method to emulate vanilla behaviour of damaging your Item as you finish mining a Block.
     * @param player Player doing the mining
     * @param blockID id of the Block which was destroyed
     * @param x Coordinates of the Block
     * @param y Coordinates of the Block
     * @param z Coordinates of the Block
     */
    private void onBlockMineFinishedDamagePlayerItem(EntityPlayer player, Block blockID, int x, int y, int z)
    {
        if (x != this.curBlockX
        || y != curBlockY
        || z != curBlockZ)
        {
            return;
        }
        
        ItemStack itemStack = player.getCurrentEquippedItem();
        if (itemStack != null)
        {
            itemStack.onBlockDestroyed(player.worldObj, blockID, new BlockPos(x, y, z), player);
            if (itemStack.stackSize == 0)
            {
                player.destroyCurrentEquippedItem();
            }
        }
    }

    /**
     * Called by the server via packet if there is more than the allowed amount of concurrent partial Blocks
     * in play. Causes the client to delete the corresponding local Block.
     * @param x coordinate of Block
     * @param y coordinate of Block
     * @param z coordinate of Block
     */
    public void onServerSentPartialBlockDeleteCommand(BlockPos p)
    {
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++)
        {
            if (partiallyMinedBlocksArray[i] != null
            && partiallyMinedBlocksArray[i].getPos().equals(p))
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
        
        // execute code which gets the object assigned the private cloud tick value we want
        mc.renderGlobal.sendBlockBreakProgress(0, new BlockPos((int)thePlayer.posX, (int)thePlayer.posY, (int)thePlayer.posZ), 1);
        
        // read the needed value
        lastCloudTickReading = vanillaDestroyBlockProgressMap.get(0).getCreationCloudUpdateTick();
        
        // execute code which destroys the helper object
        mc.renderGlobal.sendBlockBreakProgress(0, new BlockPos((int)thePlayer.posX, (int)thePlayer.posY, (int)thePlayer.posZ), 10);
        
        // if necessary restore previous object
        if (dbp != null)
        {
            vanillaDestroyBlockProgressMap.put(0, dbp);
        }
        // System.out.println("lastCloudTickReading is now "+lastCloudTickReading);
    }
}
