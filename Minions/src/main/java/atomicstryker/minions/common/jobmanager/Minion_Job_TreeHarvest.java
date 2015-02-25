package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Minion Job class for scanning for Trees and then keeping track of the minions harvesting them.
 * 
 * 
 * @author AtomicStryker
 */

public class Minion_Job_TreeHarvest extends Minion_Job_Manager
{
	private Thread thread;
	private World worldObj;
	private boolean doneLookingForTrees;
	
    public Minion_Job_TreeHarvest(Collection<EntityMinion> minions, int ix, int iy, int iz)
    {
    	super(minions, ix, iy, iz);
    	this.worldObj = minions.iterator().next().worldObj;
    	doneLookingForTrees = false;
    }
    
    @Override
    public void onJobStarted()
    {
    	super.onJobStarted();
    	
    	TreeScanner treeScanWorker = new TreeScanner(this);
    	treeScanWorker.setup(pointOfOrigin, worldObj);
    	thread = new Thread(treeScanWorker);
    	thread.start();
    }
    
    @Override
    public boolean onJobUpdateTick()
    {
    	super.onJobUpdateTick();
    	
    	BlockTask_TreeChop nextTree = null;
    	EntityMinion worker = null;
    	boolean hasJobs = !this.jobQueue.isEmpty();
    	
    	if (hasJobs)
    	{
	    	nextTree = (BlockTask_TreeChop) this.jobQueue.get(0);
	    	worker = this.getNearestAvailableWorker(nextTree.posX, nextTree.posY, nextTree.posZ);
    	}
    	else if (doneLookingForTrees) // only get a worker to fire from the job if there will be nothing left to do
    	{
    		worker = this.getAnyAvailableWorker();
    	}
    	if (worker != null)
    	{
    		if (hasJobs)
    		{
    			// order him to walk there and chop the tree
    			((BlockTask_TreeChop) this.jobQueue.get(0)).setWorker(worker);
    			worker.giveTask((BlockTask_TreeChop) this.jobQueue.get(0), true);
    			this.jobQueue.remove(0);
    		}
    		else
    		{
    			this.setWorkerFree(worker);
    		}
    	}
    	return isFinished;
    }
    
    @Override
    public void onJobFinished()
    {
    	if (thread != null && !thread.isInterrupted())
    	{
    		thread.interrupt();
    	}
    	
    	super.onJobFinished();
    }
    
	public void onFoundTreeBase(int ix, int iy, int iz, ArrayList<BlockPos> treeBlockList, ArrayList<BlockPos> leaveBlockList)
	{		
		BlockTask_TreeChop newJob = new BlockTask_TreeChop(this, null, ix, iy, iz, treeBlockList, leaveBlockList);
		if (!this.jobQueue.contains(newJob))
		{
			this.jobQueue.add(newJob);
		}
		//System.out.println("Found Tree at: ["+ix+"|"+iy+"|"+iz+"], TreeBlocks: ["+treeBlockList.size()+"], Leaves: ["+leaveBlockList.size()+"] job Queue size now: "+this.jobQueue.size());
	}
	
	public void onDoneFindingTrees()
	{
		doneLookingForTrees = true;
	}
}