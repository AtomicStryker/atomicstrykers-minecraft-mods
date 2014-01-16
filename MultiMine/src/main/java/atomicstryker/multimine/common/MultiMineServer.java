package atomicstryker.multimine.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import atomicstryker.multimine.common.network.ForgePacketWrapper;
import atomicstryker.multimine.common.network.PacketDispatcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class MultiMineServer
{
    private static MultiMineServer instance;
    private static MinecraftServer serverInstance;
    private final HashMap<Integer, List<PartiallyMinedBlock>> partiallyMinedBlocksListByDimension;
    private final BlockRegenQueue blockRegenQueue;
    
    /**
     * Server instance of Multi Mine Mod. Keeps track of Players having the Mod installed,
     * the Blocks they damage, and the Block regeneration Queue, which is watched by the
     * integrated Tick Handler.
     */
    public MultiMineServer()
    {
        partiallyMinedBlocksListByDimension = Maps.<Integer, List<PartiallyMinedBlock>>newHashMap();
        instance = this;
        blockRegenQueue = new BlockRegenQueue(30, new BlockAgeComparator());
        
        FMLCommonHandler.instance().bus().register(this);
    }
    
    public static MultiMineServer instance()
    {
        return instance;
    }

    /**
     * Called when a client has completed mining a tenth of a block. Stack that tenth on the already existing partial
     * Block or create one if not present, and send that information back to all interested players.
     * @param player player doing the digging
     * @param x coordinate of Block
     * @param y coordinate of Block
     * @param z coordinate of Block
     * @param dimension of Block and Player
     */
    public void onClientSentPartialBlockPacket(EntityPlayerMP player, int x, int y, int z, int dim)
    {
        serverInstance = FMLCommonHandler.instance().getMinecraftServerInstance();
        int dimension = player.dimension;
        //System.out.println("multi mine client sent packet from dimension "+dim+", server says its actually "+dimension);
        
        List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);
        
        if (partiallyMinedBlocks == null)
        {
            partiallyMinedBlocks = Lists.<PartiallyMinedBlock>newArrayList();
            partiallyMinedBlocksListByDimension.put(dimension, partiallyMinedBlocks);
        }
        
        PartiallyMinedBlock newblock = new PartiallyMinedBlock(x, y, z, dimension, 1);
        newblock.setLastTimeMined(System.currentTimeMillis()+MultiMine.instance().getInitialBlockRegenDelay());
        for (PartiallyMinedBlock iterBlock : partiallyMinedBlocks)
        {
            if (iterBlock.equals(newblock))
            {
                iterBlock.advanceProgress();
                iterBlock.setLastTimeMined(System.currentTimeMillis()+MultiMine.instance().getInitialBlockRegenDelay());
                // System.out.println("Server advancing partial block at: ["+x+"|"+y+"|"+z+"], progress now: "+iterBlock.getProgress());

                // send the newly advanced partialblock to all relevant players
                sendPartiallyMinedBlockUpdateToAllPlayers(iterBlock);
                
                if (iterBlock.isFinished())
                {
                    //System.out.println("Server finishing partial block at: ["+x+"|"+y+"|"+z+"]");
                    // and if its done, destroy the world block
                    player.worldObj.func_147443_d(player.func_145782_y(), x, y, z, -1);
                    Block block = player.worldObj.func_147439_a(x, y, z);
                    
                    S23PacketBlockChange packet = new S23PacketBlockChange(x, y, z, player.worldObj);
                    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_148537_a(packet, player.dimension);
                    
                    if (block != Blocks.air)
                    {
                        int meta = player.worldObj.getBlockMetadata(x, y, z);
                        if (block.removedByPlayer(player.worldObj, player, x, y, z))
                        {
                            
                            BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(player.worldObj, player.theItemInWorldManager.getGameType(), player, x, y, z);
                            if (!event.isCanceled())
                            {
                                block.func_149664_b(player.worldObj, x, y, z, meta);
                                onBlockMineFinishedDamagePlayerItem(player, block, x, y, z);
                                if (block.canHarvestBlock(player, meta))
                                {
                                    block.func_149636_a(player.worldObj, player, x, y, z, meta);
                                }
                            }
                        }
                    }
                    
                    partiallyMinedBlocks.remove(iterBlock);
                    blockRegenQueue.remove(iterBlock);
                }
                
                blockRegenQueue.offer(iterBlock);
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
        ItemStack itemStack = player.getCurrentEquippedItem();
        if (itemStack != null)
        {
            itemStack.func_150999_a(player.worldObj, blockID, x, y, z, player);

            if (itemStack.stackSize == 0)
            {
                player.destroyCurrentEquippedItem();
            }
        }
    }

    /**
     * Tells all clients to delete this partially mined Block off their local storage, the server
     * exceeds the max amount of concurrent partial Blocks.
     * @param partiallyMinedBlock partial Block to be deleted
     */
    private void sendPartiallyMinedBlockDeleteCommandToAllPlayers(PartiallyMinedBlock block)
    {
        Object[] toSend = {block.getX(), block.getY(), block.getZ(), block.getProgress()};
        PacketDispatcher.sendToAllNear(block.getX(), block.getY(), block.getZ(), 30D, block.getDimension(), ForgePacketWrapper.createPacket("AS_MM", 2, toSend));
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        EntityPlayer player = event.player;
        int dimension = player.worldObj.provider.dimensionId;
        List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);
        
        if (partiallyMinedBlocks != null)
        {
            for (PartiallyMinedBlock block : partiallyMinedBlocks)
            {
                sendPartiallyMinedBlockToPlayer(player, block);                
            }
        }
        
        Object[] toSend = {MultiMine.instance().getExcludedBlocksString()};
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_MM", 3, toSend), player);
        
        Object[] toSend2 = {MultiMine.instance().getExcludedItemssString()};
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_MM", 4, toSend2), player);
    }
    
    /**
     * Helper method to get the correct partial Block list for a World Dimension
     * @param dim Dimension of the world
     * @return the List of partial Blocks for the dimension. Can be null, can be empty.
     */
    private List<PartiallyMinedBlock> getPartiallyMinedBlocksForDimension(int dim)
    {
        return partiallyMinedBlocksListByDimension.get(dim);
    }
    
    /**
     * Sends a partial Block Packet to all players in the matching dimension and a certain area.
     * Overwrites their local partial Block instances with whatever you send.
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockUpdateToAllPlayers(PartiallyMinedBlock block)
    {
        Object[] toSend = {block.getX(), block.getY(), block.getZ(), block.getProgress()};
        //System.out.println("Server sending partial Block Update ["+block.getX()+"|"+block.getY()+"|"+block.getZ()+"] in dimension "+block.getDimension());
        PacketDispatcher.sendToAllNear(block.getX(), block.getY(), block.getZ(), 30D, block.getDimension(), ForgePacketWrapper.createPacket("AS_MM", 1, toSend));
    }
    
    /**
     * Sends a partial Block Packet to a particular player.
     * @param p Player targeted
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockToPlayer(EntityPlayer p, PartiallyMinedBlock block)
    {
        Object[] toSend = {block.getX(), block.getY(), block.getZ(), block.getProgress()};
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_MM", 1, toSend), p);
    }
    
    /**
     * Tick Handler to achieve Block Regeneration. We keep track of our Block age
     * using a PriorityQueue and start repairing Blocks if they get too old.
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
            
            PartiallyMinedBlock block = null;
            for (Iterator<PartiallyMinedBlock> iter = blockRegenQueue.iterator(); iter.hasNext();)
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
            if (blockRegenQueue.peek().getLastTimeMined()+MultiMine.instance().getBlockRegenInterval() < curTime)
            {
                block = blockRegenQueue.poll();
                
                block.setProgress(block.getProgress()-1);
                block.setLastTimeMined(curTime);
                if (block.getProgress() < 1)
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
     * Helper method to determine if a Block was removed by other means (Explosion, Sand/Gravel falling, Pistons...)
     * @param block PartiallyMinedBlock to check
     * @return true if the PartiallyMinedBlock Block coordinates return 0 in a getBlockId check, false otherwise
     */
    private boolean isBlockGone(PartiallyMinedBlock block)
    {
        return serverInstance.worldServerForDimension(block.getDimension()).func_147439_a(block.getX(), block.getY(), block.getZ()) == Blocks.air;
    }
    
    /**
     * PriorityQueue sorting Blocks by age, in order to only check one each tick
     */
    private class BlockRegenQueue extends PriorityQueue<PartiallyMinedBlock>
    {
        private static final long serialVersionUID = 1L;

        public BlockRegenQueue(int initialSize, Comparator<PartiallyMinedBlock> comparator)
        {
            super(initialSize, comparator);
        }
        
        /**
         * Overriding the offer method in order to delete older instances in favor of more
         * recently mined ones.
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
