package atomicstryker.minions.common.jobmanager;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.entity.EnumMinionState;

/**
 * Minion Job class for digging a horizontal 2x1 mineshaft and mining ores of precious material in walls and ceiling (not floor)
 * Unlike other Minion Jobs, multiple of these can exist at a time.
 * 
 * @author AtomicStryker
 */

public class Minion_Job_StripMine extends Minion_Job_Manager
{
	private final int STRIPMINE_MAX_LENGTH = 80;
	
	private World worldObj;
	
	private int currentSegment = -1;
	
	private final int xDirection;
	private final int zDirection;
	
	private boolean isFinished = false;
	
	private final int startX;
	private final int startY;
	private final int startZ;
	
	private long timeLastSegmentAdded;
	
    public Minion_Job_StripMine(EntityMinion[] minions, int ix, int iy, int iz)
    {
    	int i = 0;
    	while (i < minions.length)
    	{
    		if (!minions[i].isStripMining)
    		{
	    		this.workerList.add(minions[i]);
	    		minions[i].isStripMining = true;
	    		
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
	    		break;
    		}
    		i++;
    	}
    	
    	if (this.workerList.isEmpty())
    	{
    		System.out.println("Attempted to create Strip Mine Job, but all Minions are already stripmining!");
    		this.onJobFinished();
    	}

    	this.pointOfOrigin = new ChunkCoordinates(ix, iy, iz);

    	this.worldObj = minions[0].worldObj;

    	startX = this.pointOfOrigin.posX;
    	startY = this.pointOfOrigin.posY;
    	startZ = this.pointOfOrigin.posZ;

    	Entity boss = minions[0].master;
    	int bossX = MathHelper.floor_double(boss.posX);
    	int bossZ = MathHelper.floor_double(boss.posZ);
    	
    	if (Math.abs(startX - bossX) > Math.abs(startZ - bossZ))
    	{
    		xDirection = (startX - bossX > 0) ? 1 : -1;
    		zDirection = 0;
    	}
    	else
    	{
    		xDirection = 0;
    		zDirection = (startZ - bossZ > 0) ? 1 : -1;
    	}
    }
    
    @Override
    public void onJobStarted()
    {
    	super.onJobStarted();
    	//System.out.println("Strip Mine onJobStarted()");
    }
    
    @Override
    public void onJobUpdateTick()
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
    		if (!workerList.isEmpty() && System.currentTimeMillis() > timeLastSegmentAdded + 10000L)
    		{
    			worker = (EntityMinion) workerList.get(0);
    			
    			if (worker.getCurrentTask() instanceof BlockTask_MineOreVein)
    			{
    				// System.out.println("10 sec abort on MineOreVein Task");
    				worker.giveTask(null, true);
    				worker.currentState = EnumMinionState.AWAITING_JOB;
    			}
    			else // eerie case in which minions get stuck doing nothing
    			{
    				// System.out.println("10 sec abort on NOT.MineOreVein Task");
    				currentSegment--;
    				worker.giveTask(null, true);
    				worker.currentState = EnumMinionState.AWAITING_JOB;
    			}
    		}
    		
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
    			worker.giveTask(job);
    			job.setWorker(worker);
    			worker.currentState = EnumMinionState.THINKING;
    			//System.out.println("Strip Mine assigned job to idle worker: ["+job.posX+"|"+job.posY+"|"+job.posZ+"]");
    			
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
    	super.onJobFinished();
    }
    
    public void setWorkerFree(EntityMinion input)
    {
    	input.isStripMining = false;
    	super.setWorkerFree(input);
    }
    
    private boolean canAddNextLayer()
    {
    	timeLastSegmentAdded = System.currentTimeMillis();
    	
   		currentSegment++;
   		//System.out.println("Strip Mine adding next layer, now: "+currentSegment);
   		
   		if (currentSegment >= STRIPMINE_MAX_LENGTH)
   		{
   			return false;
   		}
   		
   		int nextX = startX+(currentSegment*xDirection);
   		int nextZ = startZ+(currentSegment*zDirection);
   		
   		if (currentSegment > 0 && currentSegment%7 == 0)
   		{
   			if (worldObj.getLightBrightness(nextX, startY, nextZ) < 10F)
   			{
   				worldObj.setBlockWithNotify(nextX-2*xDirection, startY, nextZ-2*zDirection, Block.torchWood.blockID);
   			}
   		}
   		
   		BlockTask_MineBlock nextTask = new BlockTask_MineBlock(this, null, nextX, startY, nextZ);
   		this.jobQueue.add(nextTask);
   		nextTask = new BlockTask_MineBlock(this, null, nextX, startY+1, nextZ);
   		nextTask.disableDangerCheck = true;
   		this.jobQueue.add(nextTask);
   		
   		checkBlockValuables(nextX, startY+2, nextZ); // check roof
   		
   		if (xDirection == 0) // progressing z, check x sides
   		{
   			checkBlockValuables(nextX+1, startY, nextZ);
   			checkBlockValuables(nextX-1, startY, nextZ);
   			checkBlockValuables(nextX+1, startY+1, nextZ);
   			checkBlockValuables(nextX-1, startY+1, nextZ);
   		}
   		else // progressing x, check z sides
   		{
   			checkBlockValuables(nextX, startY, nextZ+1);
   			checkBlockValuables(nextX, startY, nextZ-1);
   			checkBlockValuables(nextX, startY+1, nextZ+1);
   			checkBlockValuables(nextX, startY+1, nextZ-1);
   		}
   		
   		checkBlockValuables(nextX, startY-1, nextZ); // check floor
    	
    	return true;
    }
    
    private void checkBlockValuables(int x, int y, int z)
    {
    	int checkBlockID = worldObj.getBlockId(x, y, z);

    	if (MinionsCore.isBlockValueable(checkBlockID))
    	{
    		BlockTask_MineOreVein minetask = new BlockTask_MineOreVein(this, null, x, y, z);
    		if (minetask.posY > startY)
    		{
    			minetask.disableDangerCheck = true;
    		}
    		
    		this.jobQueue.add(minetask);
    		
    		if (y == startY+1)
    		{
    			checkBlockValuables(x, startY+2, y); // check roof of newly broken area
    		}
    	}
    }
}