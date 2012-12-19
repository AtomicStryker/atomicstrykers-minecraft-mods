package atomicstryker.minions.common.jobmanager;

/**
 * Minion Job Manager superclass. Provides minion control methods and keeps a Workerlist.
 * Provides a job Queue and events.
 * Also interfaces with the main mod class.
 * 
 * @author AtomicStryker
 */

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.util.ChunkCoordinates;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.entity.EnumMinionState;


public abstract class Minion_Job_Manager
{
	/**
	 * Contains all of a player's minions after init. Removing them from here nulls their consideration for new jobs.
	 */
	protected ArrayList<EntityMinion> workerList = new ArrayList();
	
	/**
	 * XYZ coordinates of where the player 'placed' the job
	 */
	public ChunkCoordinates pointOfOrigin;
	
	/**
	 * Contains all Blocktasks the Job needs done in ascending order. Once all are finished, the job is done.
	 */
	protected ArrayList<BlockTask> jobQueue = new ArrayList();
	
	/**
	 * The player's username
	 */
	public String masterName = null;
	
	public Minion_Job_Manager()
	{
	}
	
    public Minion_Job_Manager(EntityMinion[] minions, int ix, int iy, int iz)
    {
    	int i = 0;
    	while (i < minions.length)
    	{
    		this.workerList.add(minions[i]);
    		
   			minions[i].currentState = EnumMinionState.AWAITING_JOB;
    		minions[i].lastOrderedState = EnumMinionState.WALKING_TO_COORDS;
    		
    		if (minions[i].riddenByEntity != null)
    		{
    			minions[i].riddenByEntity.mountEntity(null);
    		}
    		
    		if (masterName == null)
    		{
    			masterName = minions[i].masterUsername;
    		}
    		
    		i++;
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
    		if ((temp.currentState == EnumMinionState.AWAITING_JOB && !temp.hasTask())
    		|| (temp.currentState == EnumMinionState.IDLE && temp.lastOrderedState == EnumMinionState.RETURNING_GOODS))
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
    		if (temp.currentState == EnumMinionState.AWAITING_JOB && !temp.hasTask())
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
    
    public void onJobStarted()
    {
    	
    }
    
    public void onJobUpdateTick()
    {
    	
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
    
    /**
     * event coming back from an issued Blocktask, useful to put it back into the jobqueue or such
     * 
     * @param worker Minion which was unable to reach the Task Block
     * @param x coordinate of task
     * @param y coordinate of task
     * @param z coordinate of task
     */
    public void onTaskUnpathable(BlockTask task, int x, int y, int z)
    {
    	//System.out.println("A "+task+" threw an unpathable error, coords: ["+x+"|"+y+"|"+z+"]");
    }
}
