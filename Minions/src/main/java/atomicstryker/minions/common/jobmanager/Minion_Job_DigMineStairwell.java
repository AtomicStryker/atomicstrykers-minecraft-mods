package atomicstryker.minions.common.jobmanager;

import java.util.Collection;

import net.minecraft.init.Blocks;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Minion Job class for digging a 5x5 vertical mineshaft with stairwell.
 * 
 * 
 * @author AtomicStryker
 */

public class Minion_Job_DigMineStairwell extends Minion_Job_Manager
{
	private int currentDepth = -1;
	private int currentSegment = 0;
	
	private final int startX;
	private final int startY;
	private final int startZ;
	
    public Minion_Job_DigMineStairwell(Collection<EntityMinion> minions, int ix, int iy, int iz)
    {
    	super(minions, ix, iy, iz);
    	startX = this.pointOfOrigin.getX();
    	startY = this.pointOfOrigin.getY();
    	startZ = this.pointOfOrigin.getZ();
    }
    
    @Override
    public boolean onJobUpdateTick()
    {
    	super.onJobUpdateTick();
    	
    	BlockTask nextBlock = null;
    	EntityMinion worker;
    	boolean hasJobs = (!this.jobQueue.isEmpty());
    	if (!hasJobs && !isFinished)
    	{
    		hasJobs = canAddNextLayer();
    	}
    	
    	if (hasJobs)
    	{
    		nextBlock = (BlockTask) this.jobQueue.get(0);
	    	worker = this.getNearestAvailableWorker(nextBlock.posX, nextBlock.posY, nextBlock.posZ);
    	}
    	else
    	{
    		worker = this.getAnyAvailableWorker();
    	}
    	if (worker != null)
    	{
    		if (hasJobs)
    		{
    			// get job from queue
    			// worker.giveTask();
    			// job.setworker!
    			
    			BlockTask job = (BlockTask) this.jobQueue.get(0);
    			worker.giveTask(job, true);
    			job.setWorker(worker);
    			
    			this.jobQueue.remove(0);
    		}
    		else
    		{
    			this.setWorkerFree(worker);
    		}
    	}
    	
    	return isFinished;
    }
    
    private boolean canAddNextLayer()
    {
    	currentDepth++;
    	if(currentDepth % 3 == 0)
    	{
    		// check for stairwell end
    		if (startY-currentDepth <= 8)
    		{
    			isFinished = true;
    			return false;
    		}
    		
    		currentSegment++;
    		if (currentSegment == 5)
    		{
    			currentSegment = 1;
    		}
    	}
    	
    	// loop all 5x5 Blocks, make exceptions for stairwell and corner blocks
		for (int ix = startX; ix <= startX + 4; ix++)
		{
			for (int iz = startZ; iz <= startZ + 4; iz++)
			{
				// corners override stairs, which would also trigger
				if (!this.isBlockCorner(ix, iz) && !this.isBlockStairs(ix, iz))
				{
					this.jobQueue.add(new BlockTask_MineBlock(this, null, ix, startY-currentDepth, iz));
				}
			}
		}
    	
    	return true;
    }
    
    private boolean isBlockCorner(int x, int z)
    {   
    	if (currentDepth%3 == 0)
    	{
        	int xDiff = x-startX;
        	int zDiff = z-startZ;
    		
	    	if (currentSegment == 1 && xDiff == 0 && zDiff == 0)
	    	{
	    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.cobblestone, 0));
	    		return true;
	    	}
	    	else if (currentSegment == 2 && xDiff == 4 && zDiff == 0)
	    	{
	    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.cobblestone, 0));
	    		return true;
	    	}
	    	else if (currentSegment == 3 && xDiff == 4 && zDiff == 4)
	    	{
	    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.cobblestone, 0));
	    		return true;
	    	}
	    	else if (currentSegment == 4 && xDiff == 0 && zDiff == 4)
	    	{
	    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.cobblestone, 0));
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
    private boolean isBlockStairs(int x, int z)
    {
    	int xDiff = x-startX;
    	int zDiff = z-startZ;
    	
    	if (currentSegment == 1 && ((xDiff-1) == (currentDepth%4)) && zDiff == 0)
    	{
    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.stone_stairs, getCurrentStairMeta()));
    		return true;
    	}
    	else if (currentSegment == 2 && xDiff == 4 &&
    			((zDiff == 1 && currentDepth%4 == 3)
    		|| (zDiff == 2 && currentDepth%4 == 0)
    		|| (zDiff == 3 && currentDepth%4 == 1)))
    	{
    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.stone_stairs, getCurrentStairMeta()));
    		return true;
    	}
    	else if (currentSegment == 3 && zDiff == 4 &&
    			((xDiff == 3 && currentDepth%4 == 2)
    		|| (xDiff == 2 && currentDepth%4 == 3)
    		|| (xDiff == 1 && currentDepth%4 == 0)))
    	{
    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.stone_stairs, getCurrentStairMeta()));
    		return true;
    	}
    	else if (currentSegment == 4 && xDiff == 0 && areModsCounterPosed(zDiff, currentDepth))
    	{
    		this.jobQueue.add(new BlockTask_ReplaceBlock(this, null, x, startY-currentDepth, z, Blocks.stone_stairs, getCurrentStairMeta()));
    		return true;
    	}
    	
    	return false;
    }
    
    private boolean areModsCounterPosed(int i, int j)
    {
		switch (i%4)
		{
			case 1: return (j%4 == 3);
			case 2: return (j%4 == 2);
			case 3: return (j%4 == 1);
		}
    	
		return false;
	}

	private int getCurrentStairMeta()
    {
    	switch (currentSegment)
    	{
    		case 1: return 1;
    		case 2: return 3;
    		case 3: return 0;
    		default: return 2;
    	}
    }
}