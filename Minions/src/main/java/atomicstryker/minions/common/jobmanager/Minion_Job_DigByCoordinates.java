package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.astarpathing.AStarStatic;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Minion Job class for digging out a NxN sized space, automatically digging a support stairwell if one is needed
 * 
 * @author AtomicStryker
 */

public class Minion_Job_DigByCoordinates extends Minion_Job_Manager
{
	private final int BLOCKS_HANDLED_PER_TICK = 30;
	private final long MAX_LOITER_TIME = 4000L;
	
	private World worldObj;
	
	private final int startX;
	private final int startY;
	private final int startZ;
	
	private final int xZtoDig;
	private final int ytoDig;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private int minZ;
	private int maxZ;
	
	private final int xDirection;
	private final int zDirection;
	
	private int[] stopCoords = new int[3];
	private boolean indexFinished;
	private ArrayList<BlockTask> blocksToMine;
	
	private int stairDirection[] = new int[2];
	private ArrayList<StairSegment> stairSegments = new ArrayList<StairSegment>();
	
	private long lastTaskCompleteTime = -1L;
	
	
    public Minion_Job_DigByCoordinates(List<EntityMinion> minions, int ix, int iy, int iz, int ixzSize, int iySize)
    {
    	super(minions, ix, iy, iz);
    	EntityMinion m = minions.get(0);
    	
    	this.worldObj = m.worldObj;
    	
    	startX = this.pointOfOrigin.getX();
    	startY = this.pointOfOrigin.getY();
    	startZ = this.pointOfOrigin.getZ();
    	xZtoDig = ixzSize;
    	ytoDig = iySize;
    	
    	Entity boss = m.master;
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
    	
    	minX = startX;
    	minX += (xDirection == 0) ? (-(xZtoDig - 1) / 2)
				: (xDirection == 1) ? 0 :
				/* xDirection == -1 */-(xZtoDig - 1);
    	
    	// minX is ALWAYS the lowest x value of the cube, hence maxX is ALWAYS a full length higher
    	maxX = minX + (xZtoDig-1);
    	
    	minZ = startZ;
    	minZ += (zDirection == 0) ? (-(xZtoDig - 1) / 2)
				: (zDirection == 1) ? 0 :
				/* zDirection == -1 */-(xZtoDig - 1);
    	
    	// minZ is ALWAYS the lowest z value of the cube, hence maxZ is ALWAYS a full length higher
    	maxZ = minZ + (xZtoDig-1);
    	
    	minY = startY;
    	maxY = startY + ytoDig-1;
    	
		stairDirection[0] = (zDirection == 1) ? 1 
				: (zDirection == -1) ? -1 :
				/* zDirection == 0 */0;
		
		stairDirection[1] = (xDirection == 1) ? -1 
				: (xDirection == -1) ? 1 :
				/* xDirection == 0 */0;
    	
    	// the initial 3 blocks are always a safe bet to get mined, and first.
		BlockTask_MineBlock mineTask = new BlockTask_MineBlock(this, null, startX, startY, startZ, true);
		jobQueue.add(mineTask);
		mineTask = new BlockTask_MineBlock(this, null, startX, startY+1, startZ, true);
		jobQueue.add(mineTask);
		mineTask = new BlockTask_MineBlock(this, null, startX, startY+2, startZ, true); // first stairwell head space
		jobQueue.add(mineTask);
    }
    
    @Override
	public boolean onJobUpdateTick()
	{
		if (!indexFinished)
		{
			progressIndexing();
			return false;
		}
    	
    	BlockTask nextBlock = null;
    	EntityMinion worker = null;
    	boolean hasJobs = (!blocksToMine.isEmpty());
    	
    	if (hasJobs)
    	{
    		if (!jobQueue.isEmpty())
    		{
        		nextBlock = (BlockTask) this.jobQueue.get(0);
    	    	worker = this.getNearestAvailableWorker(nextBlock.posX, nextBlock.posY, nextBlock.posZ);
    		}
    		
    		if (lastTaskCompleteTime > 0
    		&& !blocksToMine.isEmpty()
    		&& System.currentTimeMillis() > lastTaskCompleteTime+MAX_LOITER_TIME)
    		{
    			lastTaskCompleteTime = System.currentTimeMillis();
    			jobQueue.add(blocksToMine.get(0));
    			blocksToMine.remove(0);
    		}
    	}
    	else
    	{
    		worker = this.getAnyAvailableWorker();
    	}
    	if (worker != null)
    	{
    		if (hasJobs)
    		{    			
    			BlockTask job = this.jobQueue.get(0);
    			if (worldObj.getBlockState(new BlockPos(job.posX, job.posY, job.posZ)).getBlock() == Blocks.air
    			&& job instanceof BlockTask_MineBlock
    			&& ((BlockTask_MineBlock)job).disableDangerCheck)
    			{
    				//System.out.println("killing job ["+job.posX+"|"+job.posY+"|"+job.posZ+"] because there's nothing to do");
    				
    				this.jobQueue.remove(0);
    				blocksToMine.remove(job);
    			}
    			else
    			{
        			worker.giveTask(job, true);
        			job.setWorker(worker);
        			
        			//System.out.println("worker "+workerList.indexOf(worker)+" is given job ["+job.posX+"|"+job.posY+"|"+job.posZ+"], jobs queued: "+jobQueue.size());
        			
        			this.jobQueue.remove(0);
    			}
    		}
    		else
    		{
    			this.setWorkerFree(worker);
    		}
    	}
    	return isFinished;
    }
    
    private void progressIndexing()
    {
		if (blocksToMine == null)
		{
			stopCoords[0] = minX;
			stopCoords[1] = maxY;
			stopCoords[2] = minZ;
			blocksToMine = new ArrayList<BlockTask>();
			
			//System.out.println("Indexing starts at ["+minX+"|"+maxY+"|"+minZ+"]");
		}

		int x = stopCoords[0];
		int y = stopCoords[1];
		int z = stopCoords[2];
		//System.out.println("Loading Index at: ["+x+"|"+y+"|"+z+"]");

		for (int i = BLOCKS_HANDLED_PER_TICK; i > 0;)
		{
			if (x > maxX)
			{
				x = minX;
				z++;
				if (z > maxZ)
				{
					z = minZ;
					y--;
					if (y < minY)
					{
						indexFinished = true;
						//System.out.println("Coord Dig Indexing finished, Blocks to mine indexed: "+blocksToMine.size());
						//System.out.println("minX: "+minX+", maxX: "+maxX+", minZ: "+minZ+", maxZ: "+maxZ);
						
						onFinishedIndexing();
						break;
					}
				}
			}

			i--;
			//System.out.println("Now checking: ["+x+"|"+y+"|"+z+"]");
			if (worldObj.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.air)
			{
				BlockTask_MineBlock mineTask = new BlockTask_MineBlock(this, null, x, y, z);
				if (!blocksToMine.contains(mineTask))
				{
					blocksToMine.add(mineTask);
				}
			}
			x++;
		}

		stopCoords[0] = x;
		stopCoords[1] = y;
		stopCoords[2] = z;
		//System.out.println("Pausing Index at: ["+x+"|"+y+"|"+z+"]");
    }
    
    private void onFinishedIndexing()
    {
    	if (maxY - minY > 2 && !canReachTopLayer())
    	{    		
    		planStairWellFrom(startX, startY, startZ);
    		
    		// put in the first segment into job queue
			StairSegment current = stairSegments.get(0);
			for (int j = 0; j < current.taskTriple.length; j++)
			{
				if (!jobQueue.contains(current.taskTriple[j]))
				{
					//System.out.println("Added first Stairwell segment job: ["+current.taskTriple[j].posX+"|"+current.taskTriple[j].posY+"|"+current.taskTriple[j].posZ+"]");
					jobQueue.add(current.taskTriple[j]);
				}
			}
    	}
    }
    
    private boolean canReachTopLayer()
    {
    	int highestY = -1;
    	Iterator<BlockTask> iter = blocksToMine.iterator();
    	while (iter.hasNext())
    	{
    		BlockTask task = iter.next();
    		if (highestY == -1)
    		{
    			highestY = task.posY;
    		}
    		
    		if (task.posY < highestY)
    		{
    			// left top layer? stop checking.
    			break;
    		}
    		
    		// so we have one of the topmost layer's blocks infront of us now. Check if Minions can reach it.
    		if(AStarStatic.getAccessNodesSorted(worldObj, startX, startY, startZ, task.posX, task.posY, task.posZ).length > 0)
    		{
    			//System.out.println("canReachTopLayer actually found an access to the topmost layer! No Stairwell will be made!");
    			jobQueue.add(task);
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    @Override
    public void onTaskFinished(BlockTask task, int x, int y, int z)
    {
    	super.onTaskFinished(task, x, y, z);
    	
    	//System.out.println("Task finished: ["+x+"|"+y+"|"+z+"], worker: "+this.workerList.indexOf(task.worker));
    	
    	lastTaskCompleteTime = System.currentTimeMillis();
    	blocksToMine.remove(task);
    	
    	if (!stairSegments.isEmpty())
    	{
    		StairSegment current = stairSegments.get(0);
    		current.scratchTask(task);
    		if (current.allDone())
    		{
    			stairSegments.remove(0);
    			
    			if (!stairSegments.isEmpty())
    			{
    				//System.out.println("Stairwell Segment complete, loading up the next one!");
	    			current = stairSegments.get(0);
	    			for (int j = 0; j < current.taskTriple.length; j++)
	    			{
	    				if (!jobQueue.contains(current.taskTriple[j]))
	    				{
	    					//System.out.println("Added Stairwell segment job: ["+current.taskTriple[j].posX+"|"+current.taskTriple[j].posY+"|"+current.taskTriple[j].posZ+"]");
	    					jobQueue.add(current.taskTriple[j]);
	    				}
	    			}
    			}
    			else
    			{
    				//System.out.println("Stairwell Segment complete, stairwell done, accessing main job now!");
    				findNextTaskFrom(current.taskTriple[2].posX, current.taskTriple[2].posY, current.taskTriple[2].posZ);
    			}
    		}
    	}
    	else
    	{
    		findNextTaskFrom(x, y, z);
    	}
    }
    
    private void planStairWellFrom(int x, int y, int z)
    {
    	if (y+3 > maxY)
    	{
    		return;
    	}
    	
    	if (stairDirection[0] > 0) // stairs progress in x+ direction
    	{
    		if (x + stairDirection[0] <= maxX)
    		{    			
    			planStairWellSegment(x, y, z);
    		}
    		else
    		{
    			// switch direction to x=0, z=1 and recur
    			stairDirection[0] = 0;
    			stairDirection[1] = 1;
    			planStairWellFrom(x, y, z);
    		}
    	}
    	else if (stairDirection[0] < 0) // stairs progress in x- direction
    	{
    		if (x + stairDirection[0] >= minX)
    		{
    			planStairWellSegment(x, y, z);
    		}
    		else
    		{
    			// switch direction to x=0, z=-1 and recur
    			stairDirection[0] = 0;
    			stairDirection[1] = -1;
    			planStairWellFrom(x, y, z);
    		}
    	}
    	else if (stairDirection[1] > 0) // stairs progress in z+ direction
    	{
    		if (z + stairDirection[1] <= maxZ)
    		{
    			planStairWellSegment(x, y, z);
    		}
    		else
    		{
    			// switch direction to x=-1, z=0 and recur
    			stairDirection[0] = -1;
    			stairDirection[1] = 0;
    			planStairWellFrom(x, y, z);
    		}
    	}
    	else if (stairDirection[1] < 0) // stairs progress in z- direction
    	{
    		if (z + stairDirection[1] >= minZ)
    		{
    			planStairWellSegment(x, y, z);
    		}
    		else
    		{
    			// switch direction to x=1, z=0 and recur
    			stairDirection[0] = 1;
    			stairDirection[1] = 0;
    			planStairWellFrom(x, y, z);
    		}
    	}
    }
    
    private void planStairWellSegment(int x, int y, int z)
    {
		BlockTask_MineBlock mineTaskA = new BlockTask_MineBlock(this, null, x + stairDirection[0], y+3, z + stairDirection[1], true);
		BlockTask_MineBlock mineTaskB = new BlockTask_MineBlock(this, null, x + stairDirection[0], y+2, z + stairDirection[1], true);
		BlockTask_MineBlock mineTaskC = new BlockTask_MineBlock(this, null, x + stairDirection[0], y+1, z + stairDirection[1]);
		
		StairSegment segment = new StairSegment(mineTaskA, mineTaskB, mineTaskC);
		stairSegments.add(segment);
		
		planStairWellFrom(x + stairDirection[0], y+1, z + stairDirection[1]);
    }
    
    /**
     * This only covers flat expansion, hence just sprawl in all directions and go lower if there's nothing left
     */
    private void findNextTaskFrom(int x, int y, int z)
    {
    	if (blocksToMine.isEmpty())
    	{
    		return;
    	}
    	
    	BlockTask_MineBlock mineTask;
    	if (blocksToMine.get(0).posY < y)
    	{
    		// we finished off a y layer! go lower then
    		if (y > minY)
    		{
        		mineTask = new BlockTask_MineBlock(this, null, x, y-1, z, true);
        		if (!jobQueue.contains(mineTask))
        		{
        			jobQueue.add(mineTask);
        		}
    		}
    	}
    	else
    	{
    		for (int xIter = -1; xIter <= 1; xIter++)
    		{
        		for (int zIter = -1; zIter <= 1; zIter++)
        		{
        			for (int yIter = 1; yIter >= 0; yIter--)
        			{
            			int xFin = x+xIter;
            			int zFin = z+zIter;
            			int yFin = y+yIter;
            			
            			if (xFin >= minX
            			&& xFin <= maxX
            			&& zFin >= minZ
            			&& zFin <= maxZ
            			&& yFin <= maxY)
            			{
            				if (worldObj.getBlockState(new BlockPos(xFin, yFin, zFin)).getBlock() != Blocks.air)
            				{
                				mineTask = new BlockTask_MineBlock(this, null, xFin, yFin, zFin, true);
                        		if (blocksToMine.contains(mineTask) && !jobQueue.contains(mineTask))
                        		{
                        			jobQueue.add(mineTask);
                        		}
            				}
            			}
        			}
        		}
    		}
    	}
    }
    
    private class StairSegment
    {
    	public boolean[] taskDone = new boolean[3];
    	public BlockTask[] taskTriple;
    	
    	public StairSegment(BlockTask a, BlockTask b, BlockTask c)
    	{
    		taskTriple = new BlockTask[3];
    		taskTriple[0] = a;
    		taskTriple[1] = b;
    		taskTriple[2] = c;
    	}
    	
    	public void scratchTask(BlockTask done)
    	{
    		for (int x = 0; x < taskTriple.length; x++)
    		{
    			if (taskTriple[x].equals(done))
    			{
    				taskDone[x] = true;
    				break;
    			}
    		}
    	}
    	
    	public boolean allDone()
    	{
    		return taskDone[0] && taskDone[1] && taskDone[2];
    	}
    }
}