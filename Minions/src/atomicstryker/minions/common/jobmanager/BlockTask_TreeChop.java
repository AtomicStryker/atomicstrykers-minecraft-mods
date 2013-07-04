package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * BlockTask dummy to compute tree chopping time, give out the Items, and destroy the tree. The actual work is done in the threaded AS_TreeScanner
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_TreeChop extends BlockTask
{
    private final ArrayList<ChunkCoordinates> treeBlockList;
    private final ArrayList<ChunkCoordinates> leaveBlockList;
	
    public BlockTask_TreeChop(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz, ArrayList<ChunkCoordinates> treeBlocks, ArrayList<ChunkCoordinates> leaveBlocks)
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
        ChunkCoordinates c;
        for (int i = 0; i < treeBlockList.size(); i++)
        {
            c = treeBlockList.get(i);
            ArrayList<ItemStack> stacks = getItemStacksFromWorldBlock(output.worldObj, c.posX, c.posY, c.posZ);
            for (ItemStack stack : stacks)
            {
                if (!output.inventory.addItemStackToInventory(stack))
                {
                    EntityItem item = new EntityItem(output.worldObj, output.posX, output.posY - 0.30000001192092896D + (double)output.getEyeHeight(), output.posZ, stack);
                    item.delayBeforeCanPickup = 40;
                    output.worldObj.spawnEntityInWorld(item);
                }
            }
        }
    }
    
    private void chopTree()
    {
    	ChunkCoordinates tempCoords;
    	for (int i = treeBlockList.size()-1; i >= 0; i--)
    	{
    		tempCoords = treeBlockList.get(i);
    		worker.worldObj.setBlock(tempCoords.posX, tempCoords.posY, tempCoords.posZ, 0, 0, 3);
    	}
    	
    	if (leaveBlockList.size() > 0)
    	{
    		tempCoords = leaveBlockList.get(0);
    		int id = worker.worldObj.getBlockId(tempCoords.posX, tempCoords.posY, tempCoords.posZ);
    		if (id > 0)
    		{
    			Block leave = Block.blocksList[id];
    	    	for (int i = leaveBlockList.size()-1; i >= 0; i--)
    	    	{
    	    		tempCoords = leaveBlockList.get(i);
    	    		leave.dropBlockAsItem(worker.worldObj, tempCoords.posX, tempCoords.posY, tempCoords.posZ, worker.worldObj.getBlockMetadata(tempCoords.posX, tempCoords.posY, tempCoords.posZ), 0);
    	    		worker.worldObj.setBlock(tempCoords.posX, tempCoords.posY, tempCoords.posZ, 0, 0, 3);
    	    	}
    		}
    	}
    }
}
