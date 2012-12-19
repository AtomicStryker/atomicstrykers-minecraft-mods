package atomicstryker.multimine.common;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet14BlockDig;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.server.MinecraftServer;
import atomicstryker.ForgePacketWrapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class MultiMineServer
{
    private static MultiMineServer instance;
    private static MinecraftServer serverInstance;
    private HashMap<Integer, List> partiallyMinedBlocksListByDimension;
    private HashSet<Integer> registeredMultiMineUsers;
    private BlockRegenQueue blockRegenQueue;
    
    /**
     * Server instance of Multi Mine Mod. Keeps track of Players having the Mod installed,
     * the Blocks they damage, and the Block regeneration Queue, which is watched by the
     * integrated Tick Handler.
     */
    public MultiMineServer()
    {
        partiallyMinedBlocksListByDimension = Maps.<Integer, List>newHashMap();
        registeredMultiMineUsers = Sets.<Integer>newHashSet();
        instance = this;
        blockRegenQueue = new BlockRegenQueue(30, new BlockAgeComparator());
        
        serverInstance = FMLCommonHandler.instance().getMinecraftServerInstance();
        TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
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
    public void onClientSentPartialBlockPacket(EntityPlayer player, int x, int y, int z, int dim)
    {
        int dimension = player.dimension;
        //System.out.println("multi mine client sent packet from dimension "+dim+", server says its actually "+dimension);
        
        List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);
        
        if (partiallyMinedBlocks == null)
        {
            partiallyMinedBlocks = Lists.<PartiallyMinedBlock>newArrayList();
            partiallyMinedBlocksListByDimension.put(dimension, partiallyMinedBlocks);
        }
        
        PartiallyMinedBlock newblock = new PartiallyMinedBlock(x, y, z, dimension);
        newblock.setLastTimeMined(System.currentTimeMillis()+MultiMine.instance().getInitialBlockRegenDelay());
        PartiallyMinedBlock iterBlock;
        for (int i = 0; i < partiallyMinedBlocks.size(); i++)
        {
            iterBlock = partiallyMinedBlocks.get(i);
            if (iterBlock.equals(newblock))
            {
                iterBlock.advanceProgress();
                iterBlock.setLastTimeMined(System.currentTimeMillis()+MultiMine.instance().getInitialBlockRegenDelay());
                //System.out.println("Server advancing partial block at: ["+x+"|"+y+"|"+z+"], progress now: "+iterBlock.getProgress());

                // send the newly advanced partialblock to all relevant players
                sendPartiallyMinedBlockUpdateToAllPlayers(iterBlock);
                
                if (iterBlock.isFinished())
                {
                    //System.out.println("Server finishing partial block at: ["+x+"|"+y+"|"+z+"]");
                    // and if its done, destroy the world block
                    player.worldObj.destroyBlockInWorldPartially(player.entityId, x, y, z, -1);
                    int blockID = player.worldObj.getBlockId(x, y, z);
                    PacketDispatcher.sendPacketToAllAround(x, y, z, 30D, dimension, new Packet53BlockChange(x, y, z, player.worldObj));
                    
                    Block block = Block.blocksList[blockID];
                    if (block != null)
                    {
                        int meta = player.worldObj.getBlockMetadata(x, y, z);
                        if (block.removeBlockByPlayer(player.worldObj, player, x, y, z))
                        {
                            block.onBlockDestroyedByPlayer(player.worldObj, x, y, z, meta);
                            onBlockMineFinishedDamagePlayerItem(player, blockID, x, y, z);
                            
                            if (block.canHarvestBlock(player, meta))
                            {
                                block.harvestBlock(player.worldObj, player, x, y, z, meta);
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
        //System.out.println("Server creating new partial block at: ["+x+"|"+y+"|"+z+"]");
        
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
    private void onBlockMineFinishedDamagePlayerItem(EntityPlayer player, int blockID, int x, int y, int z)
    {
        ItemStack itemStack = player.getCurrentEquippedItem();
        if (itemStack != null)
        {
            itemStack.onBlockDestroyed(player.worldObj, blockID, x, y, z, player);

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
        PacketDispatcher.sendPacketToAllAround(block.getX(), block.getY(), block.getZ(), 30D, block.getDimension(), ForgePacketWrapper.createPacket("AS_MM", 2, toSend));
    }

    /**
     * Called when a Player logged in and sent a Handshake Packet announcing his installed Multi Mine client.
     * Fetch a List of all relevant partial Blocks and send it to him.
     * @param player who logged in
     */
    public void onPlayerLoggedIn(Player player)
    {
        int dimension = ((EntityPlayer)player).worldObj.getWorldInfo().getDimension();
        List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);
        registeredMultiMineUsers.add(((EntityPlayer)player).entityId);
        
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
        PacketDispatcher.sendPacketToAllAround(block.getX(), block.getY(), block.getZ(), 30D, block.getDimension(), ForgePacketWrapper.createPacket("AS_MM", 1, toSend));
    }
    
    /**
     * Sends a partial Block Packet to a particular player.
     * @param p Player targeted
     * @param block PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockToPlayer(Player p, PartiallyMinedBlock block)
    {
        Object[] toSend = {block.getX(), block.getY(), block.getZ(), block.getProgress()};
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_MM", 1, toSend), p);
    }
    
    /**
     * Called by the Transformed-in Method in NetServerHandler, decides if a Packet14BlockDig gets dropped or handled normally by the server
     * @param playerId entityId of the sending player
     * @param packet Packet14BlockDig with Block coordinates in question
     * @return true when Multi Mine is handling the current Block, false otherwise
     */
    public boolean getShouldIgnoreBlockDigPacket(int playerId, Packet14BlockDig packet)
    {
        // leaving this at default false doesnt seem to introduce errors, why did i block this again
        return false;
        /*
        System.out.println("server blockdig packet, status: "+packet.status);
        
        if (packet.status == 4) // Player dropped a single Item with Q, don't intervene
        // || packet.status == 2) // mine finished? dont mess with it
        {
            return false;
        }
        
        if (registeredMultiMineUsers.contains(playerId))
        {
            List<EntityPlayer> list = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
            for (EntityPlayer player : list)
            {
                if (player.entityId == playerId)
                {
                    if (player.capabilities.isCreativeMode)
                    {
                        return false;
                    }
                    
                    if (MultiMine.instance().getIsExcludedItem(player.getCurrentEquippedItem()))
                    {
                        return false;
                    }
                    
                    
                    return (!MultiMine.instance().getIsExcludedBlock(player.worldObj.getBlockId(packet.xPosition, packet.yPosition, packet.zPosition)));
                }
            }
        }
        
        return false;
        */
    }

    /**
     * Used by the Connection Handler to delete a disconnected Player's Id from the registered users list.
     * @param playerId entity ID of Player Entity who left
     */
    public void unRegisterMultiMineClient(int playerId)
    {
        if (registeredMultiMineUsers.contains(playerId))
        {
            registeredMultiMineUsers.remove(playerId);
        }
    }
    
    /**
     * Tick Handler Class to achieve Block Regeneration. We keep track of our Block age
     * using a PriorityQueue and start repairing Blocks if they get too old.
     */
    private class ServerTickHandler implements ITickHandler
    {
        private final EnumSet tickTypes = EnumSet.of(TickType.WORLD);
        
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
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
        
        /**
         * Helper method to determine if a Block was removed by other means (Explosion, Sand/Gravel falling, Pistons...)
         * @param block PartiallyMinedBlock to check
         * @return true if the PartiallyMinedBlock Block coordinates return 0 in a getBlockId check, false otherwise
         */
        private boolean isBlockGone(PartiallyMinedBlock block)
        {
        	return serverInstance.worldServerForDimension(block.getDimension()).getBlockId(block.getX(), block.getY(), block.getZ()) == 0;
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

        @Override
        public String getLabel()
        {
            return "MultiMine";
        }
    }
    
    /**
     * PriorityQueue sorting Blocks by age, in order to only check one each tick
     */
    private class BlockRegenQueue extends PriorityQueue<PartiallyMinedBlock>
    {
        public BlockRegenQueue(int initialSize, Comparator comparator)
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
