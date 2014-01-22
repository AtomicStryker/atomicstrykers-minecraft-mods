package atomicstryker.minions.common.jobmanager;

import java.util.Collection;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Minion Job class for digging a horizontal 2x1 mineshaft and mining ores of precious material in walls and ceiling (not floor)
 * Unlike other Minion Jobs, multiple of these can exist at a time.
 * 
 * @author AtomicStryker
 */

public class Minion_Job_StripMine extends Minion_Job_Manager
{
	private final int STRIPMINE_MAX_LENGTH = 80;
	private final long SEGMENT_MAX_DELAY = 3000L;
	private final int xDirection;
	private final int zDirection;
	
	private World worldObj;
	private int currentSegment;
	
	private final int startX;
	private final int startY;
	private final int startZ;
	
	private long timeForceNextSegment;
	
    public Minion_Job_StripMine(Collection<EntityMinion> minions, int ix, int iy, int iz)
    {
        super();
        
        currentSegment = -1;
    	for (EntityMinion m : minions)
    	{
            if (!m.isStripMining)
            {
                workerList.add(m);
                m.returningGoods = m.followingMaster = false;
                m.isStripMining = true;
                
                m.giveTask(null, true);
                if (m.riddenByEntity != null)
                {
                    m.riddenByEntity.mountEntity(null);
                }
                
                if (masterName == null)
                {
                    masterName = m.getMasterUserName();
                }
                break;
            }
    	}
    	
    	if (workerList.isEmpty())
    	{
    		System.out.println("Attempted to create Strip Mine Job, but all Minions are already stripmining!");
    		zDirection = xDirection = startX = startY = startZ = 0;
    	}
    	else
    	{
            this.pointOfOrigin = new ChunkCoordinates(ix, iy, iz);

            worldObj = workerList.get(0).worldObj;

            startX = this.pointOfOrigin.posX;
            startY = this.pointOfOrigin.posY;
            startZ = this.pointOfOrigin.posZ;

            Entity boss = workerList.get(0).master;
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
    public void onJobUpdateTick()
    {
    	super.onJobUpdateTick();
    	if (workerList.isEmpty())
    	{
    	    onJobFinished();
    	    return;
    	}
    	else if (!workerList.get(0).hasTask() && !jobQueue.isEmpty())
    	{
            BlockTask job = (BlockTask) this.jobQueue.get(0);
            workerList.get(0).giveTask(job, true);           
            job.setWorker(workerList.get(0));
    	}
    	
        if (System.currentTimeMillis() > timeForceNextSegment)
        {
            //currentSegment++;
            queueCurrentSegmentJobs();
        }
    }
    
    @Override
    public void onTaskFinished(BlockTask task, int x, int y, int z)
    {
        super.onTaskFinished(task, x, y, z);
        
        timeForceNextSegment = System.currentTimeMillis() + SEGMENT_MAX_DELAY;
        jobQueue.remove(task);
        
        if (jobQueue.isEmpty() && !workerList.isEmpty())
        {
            currentSegment++;
            queueCurrentSegmentJobs();
        }
    }
    
    @Override
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
        
        workerList.get(0).giveTask(null, true);
        
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
   				worldObj.func_147465_d(nextX-2*xDirection, startY, nextZ-2*zDirection, Blocks.torch, 0, 3);
   			}
   		}
   		
   		// check previous segment floor for having been dug away
   		for (int len = 0; len < 4; len++)
   		{
            if (worldObj.func_147439_a(nextX-(xDirection*len), startY-1, nextZ-(zDirection*len)) == Blocks.air)
            {
                jobQueue.add(new BlockTask_ReplaceBlock(this, null, nextX-(xDirection*len), startY-1, nextZ-(zDirection*len), Blocks.dirt, 0));
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
    	if (MinionsCore.instance.isBlockValueable(worldObj.func_147439_a(x, y, z)))
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