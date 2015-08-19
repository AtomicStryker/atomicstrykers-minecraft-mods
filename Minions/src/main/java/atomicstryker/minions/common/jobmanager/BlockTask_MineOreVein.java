package atomicstryker.minions.common.jobmanager;

import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;

import java.util.HashSet;

/**
 * Blocktask for destroying any number of interconnected Blocks
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_MineOreVein extends BlockTask_MineBlock
{
    private HashSet<ChunkCoordinates> oreVeinBlocks;
	
    public BlockTask_MineOreVein(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz)
    {
    	super(boss, input, ix, iy, iz);
    }
    
    public BlockTask_MineOreVein(Minion_Job_Manager boss, EntityMinion input, HashSet<ChunkCoordinates> moreBlocks, int ix, int iy, int iz)
    {
    	super(boss, input, ix, iy, iz);
    	oreVeinBlocks = moreBlocks;
    }

    @Override
    public void onStartedTask()
    {
    	super.onStartedTask();
    }
    
    @Override
    public void onReachedTaskBlock()
    {
    	super.onReachedTaskBlock();

    	if (oreVeinBlocks == null)
    	{
    		oreVeinBlocks = new HashSet<ChunkCoordinates>();
    	}
    	checkAdjacentBlocks();
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
    	checkForVeinContinueTask();
    }
    
    private void checkForVeinContinueTask()
    {
    	if (oreVeinBlocks == null)
    	{
    		oreVeinBlocks = new HashSet<ChunkCoordinates>();
    	}
    	
    	ChunkCoordinates check = new ChunkCoordinates(posX, posY, posZ);
		oreVeinBlocks.remove(check);
		ChunkCoordinates[] arr = new ChunkCoordinates[1];
		arr = oreVeinBlocks.toArray(arr);
		if (arr.length > 0 && arr[0] != null)
		{
			check = arr[0];
			BlockTask_MineOreVein next = new BlockTask_MineOreVein(boss, worker, oreVeinBlocks, check.posX, check.posY, check.posZ);
			worker.giveTask(next, true);
		}
		else
		{
	    	this.worker.giveTask(null, false);
		}
	}

	private void checkAdjacentBlocks()
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
    	Block checkBlockID = worker.worldObj.getBlock(x, y, z);
    	if (!MinionsCore.instance.isBlockValueable(checkBlockID))
    	{
    		return;
    	}

    	if (checkBlockID == this.blockID)
    	{
    		oreVeinBlocks.add(new ChunkCoordinates(x, y, z));
    	}
    }
}
