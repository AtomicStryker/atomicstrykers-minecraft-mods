package atomicstryker.minions.common.jobmanager;

import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Blocktask for mining a single Block, then replacing it with another
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_ReplaceBlock extends BlockTask_MineBlock
{
	public final int blockToPlace;
	public final int metaToPlace;
	
    public BlockTask_ReplaceBlock(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz, int blockOrdered, int metaOrdered)
    {
    	super(boss, input, ix, iy, iz);
    	blockToPlace = blockOrdered;
    	metaToPlace = metaOrdered;
    }
    
    public void onFinishedTask()
    {
    	super.onFinishedTask();
    	
    	this.worker.worldObj.setBlockAndMetadataWithNotify(posX, posY, posZ, blockToPlace, metaToPlace, 3);
    }
}