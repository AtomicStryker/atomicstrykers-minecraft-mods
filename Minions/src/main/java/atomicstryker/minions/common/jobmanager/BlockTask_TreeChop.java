package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.ForgeHooks;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * BlockTask dummy to compute tree chopping time, give out the Items, and destroy the tree. The actual work is done in the threaded AS_TreeScanner
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_TreeChop extends BlockTask
{
    private final ArrayList<BlockPos> treeBlockList;
    private final ArrayList<BlockPos> leaveBlockList;
	
    public BlockTask_TreeChop(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz, ArrayList<BlockPos> treeBlocks, ArrayList<BlockPos> leaveBlocks)
    {
    	super(boss, input, ix, iy, iz);
    	
    	treeBlockList = treeBlocks;
    	leaveBlockList = leaveBlocks;
    	this.setTaskDuration(1000L * treeBlockList.size());
    }

    @Override
    public void onUpdate()
    {
    	super.onUpdate();
    }
    
    @Override
    public void onFinishedTask()
    {
    	super.onFinishedTask();
    	
    	// count tree wood blocks, place wood in minion inventory, destroy tree
    	placeWoodInMinionInventory(this.worker);
    	chopTree();
    }
    
    @Override
    public void onReachedTaskBlock()
    {
        super.onReachedTaskBlock();
        worker.setHeldItemAxe();
    }
    
    private void placeWoodInMinionInventory(EntityMinion output)
    {
        BlockPos c;
        for (int i = 0; i < treeBlockList.size(); i++)
        {
            c = treeBlockList.get(i);
            List<ItemStack> stacks = getItemStacksFromWorldBlock(output.worldObj, c.getX(), c.getY(), c.getZ());
            for (ItemStack stack : stacks)
            {
                if (!output.inventory.addItemStackToInventory(stack))
                {
                    EntityItem item = new EntityItem(output.worldObj, output.posX, output.posY - 0.30000001192092896D + (double)output.getEyeHeight(), output.posZ, stack);
                    item.setPickupDelay(40);
                    output.worldObj.spawnEntityInWorld(item);
                }
            }
        }
    }
    
    private void chopTree()
    {
    	BlockPos tempCoords;
    	for (int i = treeBlockList.size()-1; i >= 0; i--)
    	{
    		tempCoords = treeBlockList.get(i);
    		
    		int event = ForgeHooks.onBlockBreakEvent(worker.worldObj, worker.worldObj.getWorldInfo().getGameType(), (EntityPlayerMP) worker.master, tempCoords);
            if (event != -1)
            {
                worker.worldObj.setBlockState(new BlockPos(tempCoords),  Blocks.air.getStateFromMeta( 0));
            }
    	}
    	
    	if (leaveBlockList.size() > 0)
    	{
    		tempCoords = leaveBlockList.get(0);
    		Block id = worker.worldObj.getBlockState(new BlockPos(tempCoords)).getBlock();
    		if (id != Blocks.air)
    		{
    	    	for (int i = leaveBlockList.size()-1; i >= 0; i--)
    	    	{
    	    		tempCoords = leaveBlockList.get(i);
    	    		
    	    		int event = ForgeHooks.onBlockBreakEvent(worker.worldObj, worker.worldObj.getWorldInfo().getGameType(), (EntityPlayerMP) worker.master, tempCoords);
    	            if (event != -1)
    	            {
    	                id.dropBlockAsItem(worker.worldObj, tempCoords, worker.worldObj.getBlockState(tempCoords), 0);
                        worker.worldObj.setBlockToAir(tempCoords);
    	            }
    	    	}
    		}
    	}
    }
}
