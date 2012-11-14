package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;

import atomicstryker.minions.common.entity.EntityMinion;

import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.World;

/**
 * Minion Job class for scanning for Trees and then keeping track of the minions harvesting them.
 * 
 * 
 * @author AtomicStryker
 */

public class Minion_Job_TreeHarvest extends Minion_Job_Manager
{
	private volatile TreeScanner treeScanWorker;
	private volatile Thread thread;
	private World worldObj;
	private boolean isWorking = false;
	private boolean doneLookingForTrees = false;
	
    public Minion_Job_TreeHarvest(EntityMinion[] minions, int ix, int iy, int iz)
    {
    	super(minions, ix, iy, iz);
    	this.worldObj = minions[0].worldObj;
    }
    
    @Override
    public void onJobStarted()
    {
    	super.onJobStarted();
    	
    	treeScanWorker = new TreeScanner(this);
    	treeScanWorker.setup(pointOfOrigin, worldObj);
    	
    	thread = new Thread(treeScanWorker);
    	thread.start();
    }
    
    @Override
    public void onJobUpdateTick()
    {
    	super.onJobUpdateTick();
    	
    	if (!isWorking)
    	{
    		//System.out.println("onJobUpdateTick, starting Tree Job now!");
    		isWorking = true;
    		onJobStarted();
    		return;
    	}
    	
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
    			worker.giveTask((BlockTask_TreeChop) this.jobQueue.get(0));
    			this.jobQueue.remove(0);
    		}
    		else
    		{
    			this.setWorkerFree(worker);
    		}
    	}
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
    
	public void onFoundTreeBase(int ix, int iy, int iz, ArrayList<ChunkCoordinates> treeBlockList, ArrayList<ChunkCoordinates> leaveBlockList)
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