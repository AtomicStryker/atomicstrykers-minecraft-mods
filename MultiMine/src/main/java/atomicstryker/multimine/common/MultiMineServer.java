package atomicstryker.multimine.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameData;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MultiMineServer
{
    private static MultiMineServer instance;
    private static MinecraftServer serverInstance;
    private final HashMap<Integer, List<PartiallyMinedBlock>> partiallyMinedBlocksListByDimension;
    private final BlockRegenQueue blockRegenQueue;
    public final HashMap<String, Boolean> blacklistedBlocksAndTools;

    /**
     * Server instance of Multi Mine Mod. Keeps track of Players having the Mod
     * installed, the Blocks they damage, and the Block regeneration Queue,
     * which is watched by the integrated Tick Handler.
     */
    public MultiMineServer()
    {
        partiallyMinedBlocksListByDimension = Maps.<Integer, List<PartiallyMinedBlock>>newHashMap();
        instance = this;
        blockRegenQueue = new BlockRegenQueue(30, new BlockAgeComparator());
        blacklistedBlocksAndTools = Maps.<String, Boolean>newHashMap();
        
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

        FMLCommonHandler.instance().bus().register(this);
    }

    public static MultiMineServer instance()
    {
        return instance;
    }

    /**
     * Called when a client has completed mining a tenth of a block. Stack that
     * tenth on the already existing partial Block or create one if not present,
     * and send that information back to all interested players.
     * 
     * @param player
     *            player doing the digging
     * @param x
     *            coordinate of Block
     * @param y
     *            coordinate of Block
     * @param z
     *            coordinate of Block
     * @param dimension
     *            of Block and Player
     */
    public void onClientSentPartialBlockPacket(EntityPlayerMP player, int x, int y, int z, int dim)
    {
        serverInstance = FMLCommonHandler.instance().getMinecraftServerInstance();
        int dimension = player.dimension;
        MultiMine.instance().debugPrint("multi mine client sent progress packet from dimension " + dim + ", server says its dimension " + dimension);
        
        final BlockPos pos = new BlockPos(x, y, z);
        final IBlockState state = player.worldObj.getBlockState(pos);
        final Block block = state.getBlock();
        if (isItemBanned(player.getCurrentEquippedItem()) || isBlockBanned(block, block.getMetaFromState(state)))
        {
            return;
        }
        
        List<PartiallyMinedBlock> partiallyMinedBlocks = getPartiallyMinedBlocksForDimension(dimension);

        if (partiallyMinedBlocks == null)
        {
            partiallyMinedBlocks = Lists.<PartiallyMinedBlock> newArrayList();
            partiallyMinedBlocksListByDimension.put(dimension, partiallyMinedBlocks);
        }

        final PartiallyMinedBlock newblock = new PartiallyMinedBlock(x, y, z, dimension, 1);
        newblock.setLastTimeMined(System.currentTimeMillis() + MultiMine.instance().getInitialBlockRegenDelay());
        for (PartiallyMinedBlock iterBlock : partiallyMinedBlocks)
        {
            if (iterBlock.equals(newblock))
            {
                iterBlock.advanceProgress();
                iterBlock.setLastTimeMined(System.currentTimeMillis() + MultiMine.instance().getInitialBlockRegenDelay());
                MultiMine.instance().debugPrint("Server advancing partial block at: ["+x+"|"+y+"|"+z+"], progress now: "+iterBlock.getProgress());

                // send the newly advanced partialblock to all relevant players
                sendPartiallyMinedBlockUpdateToAllPlayers(iterBlock);

                if (iterBlock.isFinished())
                {
                    MultiMine.instance().debugPrint("Server finishing partial block at: ["+x+"|"+y+"|"+z+"]");
                    // and if its done, destroy the world block
                    player.worldObj.sendBlockBreakProgress(player.getEntityId(), pos, -1);
                    
                    if (block.getMaterial() != Material.air)
                    {
                        final int event =
                                ForgeHooks.onBlockBreakEvent(player.worldObj, player.theItemInWorldManager.getGameType(), player, pos);
                        if (event != -1)
                        {
                            if (block.removedByPlayer(player.worldObj, pos, player, true))
                            {
                                block.onBlockDestroyedByPlayer(player.worldObj, pos, state);
                                onBlockMineFinishedDamagePlayerItem(player, block, x, y, z);
                                if (block.canHarvestBlock(player.worldObj, pos, player))
                                {
                                    block.harvestBlock(player.worldObj, player, pos, state, player.worldObj.getTileEntity(pos));
                                }
                                
                                final S23PacketBlockChange packet = new S23PacketBlockChange(player.worldObj, pos);
                                FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager()
                                        .sendPacketToAllPlayersInDimension(packet, player.dimension);
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

    private boolean isBlockBanned(Block block, int meta)
    {
        final String ident = GameData.getBlockRegistry().getNameForObject(block).toString()+"-"+meta;
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

    private boolean isItemBanned(ItemStack item)
    {
        if (item == null || item.getItem() == null)
        {
            return false;
        }
        
        final String ident = GameData.getItemRegistry().getNameForObject(item.getItem()).toString()+"-"+item.getItemDamage();
        Boolean result = blacklistedBlocksAndTools.get(ident);
        if (result != null)
        {
            return result;
        }
        
        final Configuration config = MultiMine.instance().config;
        config.load();
        result = config.get("banneditems", ident, false).getBoolean(false);
        config.save();
        blacklistedBlocksAndTools.put(ident, result);
        
        return result;
    }

    /**
     * Helper method to emulate vanilla behaviour of damaging your Item as you
     * finish mining a Block.
     * 
     * @param player
     *            Player doing the mining
     * @param blockID
     *            id of the Block which was destroyed
     * @param x
     *            Coordinates of the Block
     * @param y
     *            Coordinates of the Block
     * @param z
     *            Coordinates of the Block
     */
    private void onBlockMineFinishedDamagePlayerItem(EntityPlayer player, Block blockID, int x, int y, int z)
    {
        final ItemStack itemStack = player.getCurrentEquippedItem();
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
     * Tells all clients to delete this partially mined Block off their local
     * storage, the server exceeds the max amount of concurrent partial Blocks.
     * 
     * @param partiallyMinedBlock
     *            partial Block to be deleted
     */
    private void sendPartiallyMinedBlockDeleteCommandToAllPlayers(PartiallyMinedBlock block)
    {
        MultiMine.instance().networkHelper.sendPacketToAllAroundPoint(new PartialBlockRemovalPacket(block.getPos()),
                new TargetPoint(block.getDimension(), block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), 30D));
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        final EntityPlayerMP player = (EntityPlayerMP) event.player;
        int dimension = player.worldObj.provider.getDimensionId();
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
     * @param dim
     *            Dimension of the world
     * @return the List of partial Blocks for the dimension. Can be null, can be
     *         empty.
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
     * @param block
     *            PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockUpdateToAllPlayers(PartiallyMinedBlock block)
    {
        MultiMine.instance().networkHelper.sendPacketToAllAroundPoint(new PartialBlockPacket("server", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(),
                block.getProgress()), new TargetPoint(block.getDimension(), block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), 32D));
    }

    /**
     * Sends a partial Block Packet to a particular player.
     * 
     * @param p
     *            Player targeted
     * @param block
     *            PartiallyMinedBlock instance
     */
    private void sendPartiallyMinedBlockToPlayer(EntityPlayerMP p, PartiallyMinedBlock block)
    {
        MultiMine.instance().networkHelper.sendPacketToPlayer(
                new PartialBlockPacket("server", block.getPos().getX(), block.getPos().getY(), block.getPos().getZ(), block.getProgress()), p);
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
            if (blockRegenQueue.peek().getLastTimeMined() + MultiMine.instance().getBlockRegenInterval() < curTime)
            {
                block = blockRegenQueue.poll();

                block.setProgress(block.getProgress() - 1);
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
     * Helper method to determine if a Block was removed by other means
     * (Explosion, Sand/Gravel falling, Pistons...)
     * 
     * @param block
     *            PartiallyMinedBlock to check
     * @return true if the PartiallyMinedBlock Block coordinates return 0 in a
     *         getBlockId check, false otherwise
     */
    private boolean isBlockGone(PartiallyMinedBlock block)
    {
        return serverInstance.worldServerForDimension(block.getDimension()).isAirBlock(block.getPos());
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
