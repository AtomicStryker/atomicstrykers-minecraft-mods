package atomicstryker.minions.common.jobmanager;

/**
 * Minion Job Manager superclass. Provides minion control methods and keeps a Workerlist.
 * Provides a job Queue and events.
 * Also interfaces with the main mod class.
 * 
 * @author AtomicStryker
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.minecraft.util.ChunkCoordinates;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;

public abstract class Minion_Job_Manager
{
	/**
	 * Contains all of a player's minions after init. Removing them from here nulls their consideration for new jobs.
	 */
	protected final ArrayList<EntityMinion> workerList;
	
	/**
	 * XYZ coordinates of where the player 'placed' the job
	 */
	public ChunkCoordinates pointOfOrigin;
	
	/**
	 * Contains all Blocktasks the Job needs done in ascending order. Once all are finished, the job is done.
	 */
	protected final ArrayList<BlockTask> jobQueue;
	
	/**
	 * The player's username
	 */
	public String masterName;
	
	private boolean isWorking;
	
	public Minion_Job_Manager()
	{
	    workerList = new ArrayList<EntityMinion>();
	    jobQueue = new ArrayList<BlockTask>();
	    isWorking = false;
	    masterName = null;
	}
	
    public Minion_Job_Manager(Collection<EntityMinion> minions, int ix, int iy, int iz)
    {
        this();
        
        for (EntityMinion m : minions)
        {
            workerList.add(m);
            m.returningGoods = m.followingMaster = false;
            
            if (m.riddenByEntity != null)
            {
                m.riddenByEntity.mountEntity(null);
            }
            
            if (masterName == null)
            {
                masterName = m.getMasterUserName();
            }
        }
    	
    	this.pointOfOrigin = new ChunkCoordinates(ix, iy, iz);
    }
    
    /**
     * @param x coordinate
     * @param y coordinate
     * @param z coordinate
     * @return the closest available Minion or null
     */
    public EntityMinion getNearestAvailableWorker(int x, int y, int z)
    {
    	Iterator<EntityMinion> iter = workerList.iterator();
    	EntityMinion temp;
    	EntityMinion result = null;
    	
    	double distance = 9999D;
    	double distTemp;
    	
    	while (iter.hasNext())
    	{
    		temp = iter.next();
    		if (temp.getCurrentTask() == null)
    		{
    			distTemp = temp.getDistanceSq(x, y, z);
        		if (distTemp < distance)
        		{
        			result = temp;
        			distance = distTemp;
        		}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * @return the first available Minion from the worker List
     */
    public EntityMinion getAnyAvailableWorker()
    {
    	if (workerList.isEmpty())
    	{
    		return null;
    	}
    	
    	Iterator<EntityMinion> iter = workerList.iterator();
    	EntityMinion temp;
    	
    	while (iter.hasNext())
    	{
    		temp = iter.next();
    		if (temp.getCurrentTask() == null)
    		{
    			return temp;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Removes a worker from the workerlist and sets it idle. Also checks for having workers left and if not terminates the Job.
     * @param input Minion to remove from the workforce
     */
    public void setWorkerFree(EntityMinion input)
    {
    	input.giveTask(null);
    	workerList.remove(input);
    	
    	if (workerList.isEmpty())
    	{
    		this.onJobFinished();
    	}
    }
    
    /**
     * Called by the first Job Update tick
     */
    public void onJobStarted()
    {
    	
    }
    
    /**
     * Method to be called by some Updatetick propagating device, either a mod or an Entity
     */
    public void onJobUpdateTick()
    {
    	if (!isWorking)
    	{
    	    onJobStarted();
    	    isWorking = true;
    	}
    }
    
    /**
     * Sets all Workers free and Idle and removes this Job Manager from the mod's registry
     */
    public void onJobFinished()
    {
    	while(!this.workerList.isEmpty())
    	{
    		this.setWorkerFree((EntityMinion) this.workerList.get(0));
    	}
    	
    	MinionsCore.onJobHasFinished(this);
    }
    
    /**
     * event coming back from an issued Blocktask, useful in recursive tasks or similar
     * 
     * @param worker Minion having finished a BlockTask
     * @param x coordinate of task
     * @param y coordinate of task
     * @param z coordinate of task
     */
    public void onTaskFinished(BlockTask task, int x, int y, int z)
    {
    	
    }
    
}
