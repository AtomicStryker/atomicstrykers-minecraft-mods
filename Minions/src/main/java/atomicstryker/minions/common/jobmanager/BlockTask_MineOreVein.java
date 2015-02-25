package atomicstryker.minions.common.jobmanager;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Blocktask for destroying any number of interconnected Blocks
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_MineOreVein extends BlockTask_MineBlock
{
    private HashSet<BlockPos> oreVeinBlocks;
	
    public BlockTask_MineOreVein(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz)
    {
    	super(boss, input, ix, iy, iz);
    }
    
    public BlockTask_MineOreVein(Minion_Job_Manager boss, EntityMinion input, HashSet<BlockPos> moreBlocks, int ix, int iy, int iz)
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
    		oreVeinBlocks = new HashSet<BlockPos>();
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
    		oreVeinBlocks = new HashSet<BlockPos>();
    	}
    	
    	BlockPos check = new BlockPos(posX, posY, posZ);
		oreVeinBlocks.remove(check);
		BlockPos[] arr = new BlockPos[1];
		arr = oreVeinBlocks.toArray(arr);
		if (arr.length > 0 && arr[0] != null)
		{
			check = arr[0];
			BlockTask_MineOreVein next = new BlockTask_MineOreVein(boss, worker, oreVeinBlocks, check.getX(), check.getY(), check.getZ());
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
    	Block checkBlockID = worker.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
    	if (!MinionsCore.instance.isBlockValueable(checkBlockID))
    	{
    		return;
    	}

    	if (checkBlockID == this.blockState.getBlock())
    	{
    		oreVeinBlocks.add(new BlockPos(x, y, z));
    	}
    }
}
