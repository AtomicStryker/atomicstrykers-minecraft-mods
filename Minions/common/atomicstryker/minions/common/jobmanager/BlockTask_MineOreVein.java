package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.entity.EnumMinionState;

/**
 * Blocktask for destroying any number of interconnected Blocks
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_MineOreVein extends BlockTask_MineBlock
{
    private ArrayList oreVeinBlocks;
	
    public BlockTask_MineOreVein(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz)
    {
    	super(boss, input, ix, iy, iz);
    }
    
    public BlockTask_MineOreVein(Minion_Job_Manager boss, EntityMinion input, ArrayList moreBlocks, int ix, int iy, int iz)
    {
    	super(boss, input, ix, iy, iz);
    	oreVeinBlocks = moreBlocks;
    }

    public void onStartedTask()
    {
    	super.onStartedTask();
    }
    
    public void onReachedTaskBlock()
    {
    	super.onReachedTaskBlock();

    	if (oreVeinBlocks == null)
    	{
    		oreVeinBlocks = new ArrayList();
    	}
    	checkAdjacentBlocks();
    }
    
    public void onUpdate()
    {
    	super.onUpdate();	
    }
    
    public void onTaskNotPathable()
    {
    	super.onTaskNotPathable();
    	
    	worker.getDataWatcher().updateObject(12, Integer.valueOf(0));
    	
    	ChunkCoordinates check = new ChunkCoordinates(posX, posY, posZ);
		if (oreVeinBlocks != null && oreVeinBlocks.contains(check))
		{
			oreVeinBlocks.remove(check);
		}
		if (oreVeinBlocks != null && !oreVeinBlocks.isEmpty())
		{
			check = (ChunkCoordinates) oreVeinBlocks.get(0);
			BlockTask_MineOreVein next = new BlockTask_MineOreVein(boss, worker, oreVeinBlocks, check.posX, check.posY, check.posZ);
			worker.giveTask(next);
		}
		else
		{
	    	this.worker.currentState = EnumMinionState.AWAITING_JOB;
	    	this.worker.giveTask(null, true);
		}
    }
    
    public void onFinishedTask()
    {
    	worker.getDataWatcher().updateObject(12, Integer.valueOf(0));
    	checkDangers();
    	
    	this.blockID = worker.worldObj.getBlockId(posX, posY, posZ); // check against interference mining
    	checkBlockForCaveIn(posX, posY+1, posZ);
    	if (blockID != 0 && Block.blocksList[blockID].getBlockHardness(worker.worldObj, posX, posY, posZ) >= 0F && blockID != Block.chest.blockID)
    	{
    	    ArrayList<ItemStack> stackList = getItemStacksFromWorldBlock(worker.worldObj, posX, posY, posZ);
    		if (this.worker.worldObj.setBlockWithNotify(posX, posY, posZ, 0))
    		{
    			putBlockHarvestInWorkerInventory(stackList);
    		}
    	}
    	
    	checkForVeinContinueTask();
    }
    
    private void checkBlockForCaveIn(int x, int y, int z)
    {
    	int checkBlockID = worker.worldObj.getBlockId(x, y, z);
    	
    	if (checkBlockID > 0)
    	{
    		if (checkBlockID == Block.sand.blockID || checkBlockID == Block.gravel.blockID)
    		{            	
            	this.worker.inventory.consumeInventoryItem(Block.dirt.blockID);
            	this.worker.worldObj.setBlockWithNotify(x, y, z, Block.dirt.blockID);
    		}
    	}
	}
    
    private void checkForVeinContinueTask()
    {
    	if (oreVeinBlocks == null)
    	{
    		oreVeinBlocks = new ArrayList();
    	}
    	
    	ChunkCoordinates check = new ChunkCoordinates(posX, posY, posZ);
		if (oreVeinBlocks.contains(check))
		{
			oreVeinBlocks.remove(check);
		}
		
		if (!oreVeinBlocks.isEmpty())
		{
			check = (ChunkCoordinates) oreVeinBlocks.get(0);
			BlockTask_MineOreVein next = new BlockTask_MineOreVein(boss, worker, oreVeinBlocks, check.posX, check.posY, check.posZ);
			worker.giveTask(next);
		}
		else
		{
	    	this.worker.currentState = EnumMinionState.AWAITING_JOB;
	    	this.worker.giveTask(null, true);
		}
	}

	public void checkAdjacentBlocks()
    {
    	// check adjacent blocks for being the same id
    	checkBlockForVein(posX, posY-1, posZ);
    	checkBlockForVein(posX, posY+1, posZ);
    	checkBlockForVein(posX+1, posY, posZ);
    	checkBlockForVein(posX-1, posY, posZ);
    	checkBlockForVein(posX, posY, posZ+1);
    	checkBlockForVein(posX, posY, posZ-1);
    }
    
    private void checkBlockForVein(int x, int y, int z)
    {
    	int checkBlockID = worker.worldObj.getBlockId(x, y, z);
    	if (!MinionsCore.isBlockValueable(checkBlockID))
    	{
    		return;
    	}

    	if (checkBlockID == this.blockID)
    	{
    		ChunkCoordinates check = new ChunkCoordinates(x, y, z);
    		if (!oreVeinBlocks.contains(check))
    		{
    			oreVeinBlocks.add(check);
    		}
    	}
    }
}
