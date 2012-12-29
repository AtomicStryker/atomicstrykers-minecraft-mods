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
	private final long SEGMENT_MAX_DELAY = 10000L;
	private final int xDirection;
	private final int zDirection;
	
	private EntityMinion worker;
	private World worldObj;
	private int currentSegment;
	
	private final int startX;
	private final int startY;
	private final int startZ;
	
	private long timeForceNextSegment;
	
    public Minion_Job_StripMine(EntityMinion[] minions, int ix, int iy, int iz)
    {
        currentSegment = -1;
    	for (EntityMinion m : minions)
    	{
            if (!m.isStripMining)
            {
                worker = m;
                m.isStripMining = true;
                
                m.giveTask(null, true);
                m.currentState = EnumMinionState.AWAITING_JOB;
                m.lastOrderedState = EnumMinionState.WALKING_TO_COORDS;
                
                if (m.riddenByEntity != null)
                {
                    m.riddenByEntity.mountEntity(null);
                }
                
                if (masterName == null)
                {
                    masterName = m.masterUsername;
                }
                break;
            }
    	}
    	
    	if (worker == null)
    	{
    		System.out.println("Attempted to create Strip Mine Job, but all Minions are already stripmining!");
    		zDirection = xDirection = startX = startY = startZ = 0;
    	}
    	else
    	{
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
    	if (worker == null)
    	{
    	    onJobFinished();
    	    return;
    	}
    	else if (!worker.hasTask() && !jobQueue.isEmpty())
    	{
            BlockTask job = (BlockTask) this.jobQueue.get(0);
            worker.currentState = EnumMinionState.THINKING;
            worker.giveTask(job);           
            job.setWorker(worker);
    	}
    	
        if (System.currentTimeMillis() > timeForceNextSegment)
        {
            //currentSegment++;
            queueCurrentSegmentJobs();
        }
    }
    
    @Override
    public void onJobFinished()
    {
        worker = null;
    	super.onJobFinished();
    }
    
    @Override
    public void onTaskFinished(BlockTask task, int x, int y, int z)
    {
        timeForceNextSegment = System.currentTimeMillis() + SEGMENT_MAX_DELAY;
        jobQueue.remove(task);
        
        if (jobQueue.isEmpty())
        {
            currentSegment++;
            queueCurrentSegmentJobs();
        }
    }
    
    public void setWorkerFree(EntityMinion input)
    {
    	input.isStripMining = false;
    	super.setWorkerFree(input);
    }
    
    /**
     * Creates and queues the current 2-by-1 Segment to dig away
     * @return false if the Stripmine has reached max length, true otherwise
     */
    private boolean queueCurrentSegmentJobs()
    {
        jobQueue.clear();
        worker.giveTask(null, true);
        worker.currentState = EnumMinionState.AWAITING_JOB;
        
    	timeForceNextSegment = System.currentTimeMillis() + SEGMENT_MAX_DELAY;
    	
   		//System.out.println("Strip Mine adding next layer, now: "+currentSegment);
   		
   		if (currentSegment >= STRIPMINE_MAX_LENGTH)
   		{
   		    onJobFinished();
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