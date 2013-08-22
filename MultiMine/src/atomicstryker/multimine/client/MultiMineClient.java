package atomicstryker.multimine.client;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.multimine.common.MultiMine;
import atomicstryker.multimine.common.PartiallyMinedBlock;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class MultiMineClient
{
    private static MultiMineClient instance;
    private static Minecraft mc;
    private static EntityPlayer thePlayer;
    private PartiallyMinedBlock[] partiallyMinedBlocksArray;
    private boolean isInstalledOnServer;
    private Map<Integer, DestroyBlockProgress> vanillaDestroyBlockProgressMap;
    private int arrayOverWriteIndex;
    private int curBlockX;
    private int curBlockY;
    private int curBlockZ;
    
    private final long cloudTickReadingIntervalMillis = 5000L;
    private long nextTimeCloudTickReading;
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
        isInstalledOnServer = false;
        arrayOverWriteIndex = 0;
        
        nextTimeCloudTickReading = System.currentTimeMillis();
        lastCloudTickReading = 0;
        
        for (Field f : RenderGlobal.class.getDeclaredFields())
        {
            if (f.getType().equals(Map.class))
            {
                f.setAccessible(true);
                try
                {
                    vanillaDestroyBlockProgressMap = (Map) f.get(FMLClientHandler.instance().getClient().renderGlobal);
                    System.out.println("Multi Mine vanilla RenderMap invasion successful!");
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
     * Called by the transformed Client's PlayerController when it has finished mining a tenth of a Block and aborted internal processing.
     * A packet about this is sent to server, who will do further processing. We also check the supplied block mining completion for being
     * abnormally high (eg shearing) to prevent bugs from happening.
     * @param x coordinate of Block
     * @param y coordinate of Block
     * @param z coordinate of Block
     * @param blockCompletion the actual damage value inflicted on the Block
     */
    public void onClientMinedBlockTenthCompleted(int x, int y, int z, float blockCompletion)
    {
        if (blockCompletion < 1.0f)
        {
            curBlockX = x;
            curBlockY = y;
            curBlockZ = z;
            Object[] toSend = {x, y, z, thePlayer.dimension};
            float f = blockCompletion;
            while (f >= 0.1f)
            {
                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_MM", 1, toSend));
                f -= 0.1f;
            }
        }
        //System.out.println("Client finished a block tenth for: ["+x+"|"+y+"|"+z+"], actual completion: "+blockCompletion);
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
        int blockID = world.getBlockId(x, y, z);
        if (blockID != 0)
        {
            int blockMeta = world.getBlockMetadata(x, y, z);
            Block block = Block.blocksList[blockID];
            mc.sndManager.playSound(block.stepSound.getStepSound(), (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
            mc.effectRenderer.addBlockDestroyEffects(x, y, z, blockID, blockMeta);
        }
    }

    /**
     * Called by Server Login Event clientside, Client sends a packet to server only understood by MultiMine servers
     */
    public void onClientLoggedIn()
    {
        System.out.println("MM sending client login packet to server...");
        isInstalledOnServer = false;
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_MM", 0, null));
    }

    /**
     * Called by Packet Handler clientside, when a server answered the challenge correctly and full functionality shall be enabled
     */
    public void setEnabledOnServer()
    {
        isInstalledOnServer = true;
        thePlayer = FMLClientHandler.instance().getClient().thePlayer;
        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage("Server handshake complete! Multi Mine enabled!");
    }
    
    /**
     * Called by the transformed PlayerControllerMP to determine whether or not to suppress vanilla mining behaviour
     * @param blockId Block ID currently being mined
     * @return true when Multi Mine takes over now, false otherwise
     */
    public boolean getIsEnabledForServerAndBlockId(int blockId)
    {
        return isInstalledOnServer
                && !thePlayer.capabilities.isCreativeMode
                && !MultiMine.instance().getIsExcludedItem(thePlayer.getCurrentEquippedItem())
                && !MultiMine.instance().getIsExcludedBlock(blockId);
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
        
        //System.out.println("Client received partial Block packet for: ["+x+"|"+y+"|"+z+"], progress now: "+progress);
        updateCloudTickReading();
        
        int dimension = thePlayer.dimension;
        PartiallyMinedBlock newBlock = new PartiallyMinedBlock(x, y, z, dimension, progress);
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
                // if other guy's progress advances, render digging
                if (iterBlock.getProgress() < progress
                && (iterBlock.getX() != curBlockX || iterBlock.getY() != curBlockY || iterBlock.getZ() != curBlockZ))
                {
                    renderBlockDigParticles(x, y, z);
                }
                
                iterBlock.setProgress(progress);
                DestroyBlockProgress newDestroyBP = new DestroyBlockProgress(0 ,iterBlock.getX(), iterBlock.getY(), iterBlock.getZ());
                newDestroyBP.setPartialBlockDamage(iterBlock.getProgress());
                newDestroyBP.setCloudUpdateTick(lastCloudTickReading);
                vanillaDestroyBlockProgressMap.put(i, newDestroyBP);
                
                if (iterBlock.isFinished())
                {
                    EntityPlayer player = thePlayer;
                    World w = player.worldObj;
                    w.destroyBlockInWorldPartially(player.entityId, x, y, z, -1);
                    
                    int blockID = w.getBlockId(x, y, z);
                    Block block = Block.blocksList[blockID];
                    if (block != null)
                    {
                        int meta = w.getBlockMetadata(x, y, z);
                        if (block.removeBlockByPlayer(w, player, x, y, z))
                        {
                            block.onBlockDestroyedByPlayer(w, x, y, z, meta);
                            block.harvestBlock(w, player, x, y, z, meta);
                        }
						
                        //w.playAuxSFX(2001, x, y, z, blockID + meta << 12);
						w.playSound(x+0.5D, y+0.5D, z+0.5D, block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F, false);
                    }
                    onBlockMineFinishedDamagePlayerItem(player, blockID, x, y, z);

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
    private void onBlockMineFinishedDamagePlayerItem(EntityPlayer player, int blockID, int x, int y, int z)
    {
        if (x != this.curBlockX
        || y != curBlockY
        || z != curBlockZ)
        {
            return;
        }
        
        ItemStack itemStack = player.getCurrentEquippedItem();
        if (itemStack != null
        && Item.itemsList[itemStack.itemID] != null
        && Block.blocksList[blockID] != null)
        {
            itemStack.onBlockDestroyed(player.worldObj, blockID, x, y, z, player);

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
    public void onServerSentPartialBlockDeleteCommand(int x, int y, int z)
    {
        for (int i = 0; i < partiallyMinedBlocksArray.length; i++)
        {
            if (partiallyMinedBlocksArray[i] != null
            && partiallyMinedBlocksArray[i].getX() == x
            && partiallyMinedBlocksArray[i].getY() == y
            && partiallyMinedBlocksArray[i].getZ() == z)
            {
                partiallyMinedBlocksArray[i] = null;
                vanillaDestroyBlockProgressMap.remove(i);
                break;
            }
        }
    }

    /**
     * Called by the server via packet to enforce his excluded Block IDs upon the client.
     * @param excludedBlocksString String of Block IDs, seperated by a comma
     */
    public void onServerSentExcludedBlocksList(String excludedBlocksString)
    {
        MultiMine.instance().setExcludedBlocksString(excludedBlocksString);
    }

    /**
     * Called by the server via packet to enforce his excluded Item IDs upon the client.
     * @param excludedItemsString String of Item IDs, seperated by a comma
     */
    public void onServerSentExcludedItemsList(String excludedItemsString)
    {
        MultiMine.instance().setExcludedItemssString(excludedItemsString);
    }
    
    private void updateCloudTickReading()
    {
        if (System.currentTimeMillis() > nextTimeCloudTickReading && !vanillaDestroyBlockProgressMap.isEmpty())
        {
            nextTimeCloudTickReading = System.currentTimeMillis() + cloudTickReadingIntervalMillis;
            
            DestroyBlockProgress dbp = vanillaDestroyBlockProgressMap.get(0);
            mc.renderGlobal.destroyBlockPartially(0, 1, 2, 3, 4);
            lastCloudTickReading = vanillaDestroyBlockProgressMap.get(0).getCreationCloudUpdateTick();
            lastCloudTickReading += 400;
            if (dbp != null)
            {
                vanillaDestroyBlockProgressMap.put(0, dbp);
            }
            else
            {
                vanillaDestroyBlockProgressMap.remove(0);
            }
            //System.out.println("lastCloudTickReading is now "+lastCloudTickReading);
        }
    }
}
