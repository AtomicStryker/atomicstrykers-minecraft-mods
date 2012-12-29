package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.entity.EnumMinionState;
import atomicstryker.minions.common.pathfinding.AStarNode;
import atomicstryker.minions.common.pathfinding.AStarStatic;

/**
 * Blocktask super schematic. By default a Blocktask doesnt change the Block.
 * 
 * 
 * @author AtomicStryker
 */

public abstract class BlockTask
{
	public Minion_Job_Manager boss;
	public final int posX;
	public final int posY;
	public final int posZ;
	public boolean startedTask;
	public EntityMinion worker;
	protected double accessRange;
	protected long taskDurationMillis;
	public boolean isWorkerInRange;
	protected long timeBlockReached;
	protected AStarNode[] possibleAccessNodes;
	protected int currentAccessNode;
	private long taskTimeStarted;
	private double startMinionX;
	private double startMinionZ;
	
	/**
	 * Creates a new BlockTast instance
	 * @param boss Task Manager to notify about events coming from here
	 * @param input worker to be assigned to the task, CAN be null to just store the job for later. Task will not start until a worker is assigned.
	 * @param ix Block x coordinate
	 * @param iy Block y coordinate
	 * @param iz Block z coordinate
	 */
    public BlockTask(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz)
    {
    	//System.out.println("BlockTask created!");
    	this.boss = boss;
    	this.worker = input;
    	this.posX = ix;
    	this.posY = iy;
    	this.posZ = iz;
    	startedTask = false;
    	accessRange = 4.0D;
    	taskDurationMillis = 1000L;
    	isWorkerInRange = false;
    }
    
    /**
     * Assigns a worker to this Task. The worker should forward Update Ticks to this task.
     * @param input worker to be assigned
     */
    public void setWorker(EntityMinion input)
    {
    	this.worker = input;
    }
    
    public void setAccessRange(double input)
    {
    	this.accessRange = input;
    }
    
    public void setTaskDuration(long input)
    {
    	this.taskDurationMillis = input;
    }
    
    /**
     * Update tick coming from the assigned worker. Hence, does not get called when there is no worker assigned.
     */
    public void onUpdate()
    {
    	if (!startedTask)
    	{
        	if (!worker.inventoryFull)
        	{
        		onStartedTask();
        	}
        	else if (worker.currentState != EnumMinionState.RETURNING_GOODS
        	&& !worker.hasPath()
        	&& !worker.pathPlanner.isBusy())
        	{
        		worker.currentState = EnumMinionState.RETURNING_GOODS;
        		//System.out.println("Blocktask worker is full, sending to return goods");
        	}
    	}
    	else if (!isWorkerInRange && System.currentTimeMillis() - taskTimeStarted > 3000L)
    	{
    		if (Math.abs(startMinionX - worker.posX) < 1D && Math.abs(startMinionZ - worker.posZ) < 1D)
    		{
    		    onWorkerPathFailed();
    		}
    		else
    		{
        		taskTimeStarted = System.currentTimeMillis();
        		startMinionX = worker.posX;
        		startMinionZ = worker.posZ;
    		}
    	}
    	
    	if (this.isWorking())
    	{
    		this.worker.faceBlock(posX, posY, posZ);
    		
    		worker.getDataWatcher().updateObject(12, Integer.valueOf(1));
    		worker.getDataWatcher().updateObject(13, Integer.valueOf(posX));
    		worker.getDataWatcher().updateObject(14, Integer.valueOf(posY));
    		worker.getDataWatcher().updateObject(15, Integer.valueOf(posZ));
    	}
    	
    	if (!isWorkerInRange)
    	{
    		if (Math.sqrt(this.worker.getDistanceSq(posX+0.5D, posY+0.5D, posZ+0.5D)) < accessRange)
    		{
        		this.onReachedTaskBlock();
    		}
    	}
    	else if ((System.currentTimeMillis() - timeBlockReached) > (taskDurationMillis/worker.workSpeed))
    	{
    		this.onFinishedTask();
    	}
    }
    
    /**
     * Causes the assigned worker to attempt to navigate the next pre-computed path. If all paths failed,
     * as a last resort the worker is teleported to the tasked Block.
     */
    public void onWorkerPathFailed()
    {
    	if (possibleAccessNodes != null && this.currentAccessNode < possibleAccessNodes.length-1)
    	{
    		this.currentAccessNode++;
    		this.worker.orderMinionToMoveTo(possibleAccessNodes[currentAccessNode].x, possibleAccessNodes[currentAccessNode].y, possibleAccessNodes[currentAccessNode].z, false);
    		//System.out.println("BlockTask onWorkerPathFailed assigning next path");
    	}
    	else
    	{
    	    //System.out.println("BlockTask onWorkerPathFailed all paths failed, teleporting dat minion");
    		worker.performTeleportToTarget();
    		onFinishedTask();
    	}
    }
    
    /**
     * Called when a worker comes within accessing range of the Block. Commences animation, sets
     * starting time, stops movement.
     */
    public void onReachedTaskBlock()
    {
        //System.out.println("BlockTask onReachedTaskBlock");
    	isWorkerInRange = true;
    	timeBlockReached = System.currentTimeMillis();
    	this.worker.currentState = EnumMinionState.MINING;
    	this.worker.setPathToEntity(null);
    }
    
    /**
     * Called when the BlockTask got a worker assigned and should commence execution. Starts path
     * computing, aborts if no path can be found.
     */
    public void onStartedTask()
    {
        //System.out.println("onStartedTask ["+this.posX+"|"+this.posY+"|"+this.posZ+"]");
    	if (startedTask) return;
    	startedTask = true;
    	
    	taskTimeStarted = System.currentTimeMillis();
    	startMinionX = worker.posX;
    	startMinionZ = worker.posZ;
    	
    	this.worker.currentState = EnumMinionState.THINKING;
    	this.currentAccessNode = 0;
    	this.possibleAccessNodes = getAccessNodesSorted(MathHelper.floor_double(worker.posX), MathHelper.floor_double(worker.posY)-1, MathHelper.floor_double(worker.posZ));
    	
    	if (this.possibleAccessNodes.length != 0)
    	{
    	    //System.out.println("Ordering Minion to move to possible path no.: "+currentAccessNode);
    		this.worker.orderMinionToMoveTo(possibleAccessNodes[currentAccessNode].x, possibleAccessNodes[currentAccessNode].y, possibleAccessNodes[currentAccessNode].z, false);
    		this.worker.currentState = EnumMinionState.WALKING_TO_COORDS;
    	}
    	else
    	{
    	    worker.performTeleportToTarget();
    	}
    }
    
    /**
     * Called when the deed is done. Resets the worker. Notifies the taskmanager.
     */
    public void onFinishedTask()
    {
        //System.out.println("onFinishedTask ["+this.posX+"|"+this.posY+"|"+this.posZ+"], resetting minion");
        worker.getDataWatcher().updateObject(12, Integer.valueOf(0));
    	this.worker.currentState = EnumMinionState.AWAITING_JOB;
    	this.worker.giveTask(null, true);
    	
    	boss.onTaskFinished(this, posX, posY, posZ);
    }
    
    public boolean isWorking()
    {
    	return isWorkerInRange;
    }
    
    public boolean isEntityInAccessRange(EntityLiving ent)
    {
    	return (ent.getDistanceSq(this.posX, this.posY, this.posZ) < accessRange);
    }
    
    /**
     * @param workerX
     * @param workerY
     * @param workerZ
     * @return an Array of pathable AStarNodes, starting with the closest one to parameter coordinates and ascending. Array can be size 0 but is != null
     */
    public AStarNode[] getAccessNodesSorted(int workerX, int workerY, int workerZ)
    {
    	return AStarStatic.getAccessNodesSorted(worker.worldObj, workerX, workerY, workerZ, posX, posY, posZ);
    }
    
    /**
     * Figures out what ItemStack would result from breaking a Block in the World
     */
    public ArrayList<ItemStack> getItemStacksFromWorldBlock(World world, int i, int j, int k)
    {
    	Block block = Block.blocksList[world.getBlockId(i, j, k)];
    	if (block == null
    	|| block.blockMaterial == Material.water
    	|| block.blockMaterial == Material.lava
    	|| block.blockMaterial == Material.leaves
    	|| block.blockMaterial == Material.plants)
    	{
    		return new ArrayList<ItemStack>();
    	}
    	
    	return block.getBlockDropped(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
    }
    
    public void putBlockHarvestInWorkerInventory(ArrayList<ItemStack> stackList)
    {
        if (stackList != null)
        {
            for (int i = 0; i < stackList.size(); i++)
            {
                if (!this.worker.inventory.addItemStackToInventory(stackList.get(i)))
                {
                    worker.inventoryFull = true;
                    worker.worldObj.spawnEntityInWorld(new EntityItem(worker.worldObj, this.posX, this.posY, this.posZ, stackList.get(i)));
                }
            }
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
    	if (o instanceof BlockTask)
    	{
    		BlockTask checktask = (BlockTask) o;
    		return (this.posX == checktask.posX && this.posY == checktask.posY && this.posZ == checktask.posZ);
    	}
    	return false;	
    }
}
