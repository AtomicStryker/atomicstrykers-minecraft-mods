package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.astarpathing.AStarNode;
import atomicstryker.astarpathing.AStarStatic;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Blocktask super schematic. By default a Blocktask doesnt change the Block.
 * 
 * 
 * @author AtomicStryker
 */

public abstract class BlockTask
{
    protected final Minion_Job_Manager boss;
    public final int posX;
    public final int posY;
    public final int posZ;
    public final BlockPos pos;
    private boolean startedTask;
    protected EntityMinion worker;
    protected double accessRangeSq;
    protected long taskDurationMillis;
    public boolean workerReachedBlock;
    protected long timeBlockReached;
    private long taskTimeStarted;
    private double startMinionX;
    private double startMinionZ;

    /**
     * Creates a new BlockTast instance
     * 
     * @param boss
     *            Task Manager to notify about events coming from here
     * @param input
     *            worker to be assigned to the task, CAN be null to just store
     *            the job for later. Task will not start until a worker is
     *            assigned.
     * @param ix
     *            Block x coordinate
     * @param iy
     *            Block y coordinate
     * @param iz
     *            Block z coordinate
     */
    public BlockTask(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz)
    {
        // System.out.println("BlockTask created!");
        this.boss = boss;
        this.worker = input;
        this.posX = ix;
        this.posY = iy;
        this.posZ = iz;
        pos = new BlockPos(ix, iy, iz);
        startedTask = false;
        accessRangeSq = 9.0D;
        taskDurationMillis = 1000L;
        workerReachedBlock = false;
    }

    /**
     * Assigns a worker to this Task. The worker should forward Update Ticks to
     * this task.
     * 
     * @param input
     *            worker to be assigned
     */
    public void setWorker(EntityMinion input)
    {
        MinionsCore.debugPrint("task "+this+" at ["+posX+"|"+posY+"|"+posZ+"] assigned to worker "+worker);
        this.worker = input;
    }

    /**
     * Specifies from which "block reach" range this task can be done
     * 
     * @param input
     */
    public void setAccessRange(double input)
    {
        this.accessRangeSq = input;
    }

    /**
     * Specifies the task's work duration in millis
     * 
     * @param input
     */
    public void setTaskDuration(long input)
    {
        this.taskDurationMillis = input;
    }

    /**
     * Update tick coming from the assigned worker. Hence, does not get called
     * when there is no worker assigned.
     */
    public void onUpdate()
    {
        if (!startedTask)
        {
            if (!worker.inventoryFull)
            {
                onStartedTask();
            }
            else
            {
                worker.returningGoods = true;
                worker.runInventoryDumpLogic();
                MinionsCore.debugPrint("Blocktask "+this+" worker "+worker+" is full, sending to return goods");
            }
        }
        else if (!workerReachedBlock && System.currentTimeMillis() - taskTimeStarted > 1000L)
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

        if (isWorking())
        {
            worker.faceBlock(posX, posY, posZ);
            worker.getDataWatcher().updateObject(16, Integer.valueOf(1));
            worker.getDataWatcher().updateObject(17, Integer.valueOf(posX));
            worker.getDataWatcher().updateObject(18, Integer.valueOf(posY));
            worker.getDataWatcher().updateObject(19, Integer.valueOf(posZ));
        }

        if (!workerReachedBlock)
        {
            if (isEntityInAccessRange(worker))
            {
                onReachedTaskBlock();
            }
        }
        else if ((System.currentTimeMillis() - timeBlockReached) > (taskDurationMillis / worker.workSpeed))
        {
            onFinishedTask();
        }
    }

    /**
     * Causes the assigned worker to attempt to navigate the next pre-computed
     * path. If all paths failed, as a last resort the worker is teleported to
     * the tasked Block.
     */
    public void onWorkerPathFailed()
    {
        // System.out.println("BlockTask onWorkerPathFailed all paths failed, teleporting dat minion");
        worker.performTeleportToTarget();
        onReachedTaskBlock();
    }

    /**
     * Called when a worker comes within accessing range of the Block. Commences
     * animation, sets starting time, stops movement.
     */
    public void onReachedTaskBlock()
    {
        // System.out.println("BlockTask onReachedTaskBlock");
        workerReachedBlock = true;
        timeBlockReached = System.currentTimeMillis();
        this.worker.setWorking(true);
        //this.worker.setPathToEntity(null);

        worker.adaptItem(worker.worldObj.getBlockState(new BlockPos(posX, posY, posZ)).getBlock().getMaterial());
    }

    /**
     * Called when the BlockTask got a worker assigned and should commence
     * execution. Starts path computing, aborts if no path can be found.
     */
    public void onStartedTask()
    {
        if (startedTask)
            return;
        startedTask = true;
        MinionsCore.debugPrint("onStartedTask "+this+" ["+this.posX+"|"+this.posY+"|"+this.posZ+"], worker "+worker);

        taskTimeStarted = System.currentTimeMillis();
        startMinionX = worker.posX;
        startMinionZ = worker.posZ;
        
        AStarNode[] possibleAccessNodes = getAccessNodesSorted(MathHelper.floor_double(worker.posX), MathHelper.floor_double(worker.posY) - 1, MathHelper.floor_double(worker.posZ));
        if (possibleAccessNodes.length != 0)
        {
            this.worker.orderMinionToMoveTo(possibleAccessNodes, false);
        }
        else
        {
            MinionsCore.debugPrint("Teleporting Minion to impathable task "+this);
            worker.performTeleportToTarget();
        }
    }

    /**
     * Called when the deed is done. Resets the worker. Notifies the
     * taskmanager.
     */
    public void onFinishedTask()
    {
        MinionsCore.debugPrint("onFinishedTask "+this+" ["+this.posX+"|"+this.posY+"|"+this.posZ+"], resetting minion "+worker);
        this.worker.giveTask(null, true);
        
        if (boss != null)
        {
            boss.onTaskFinished(this, posX, posY, posZ);
        }
    }

    /**
     * @return true when the worker has moved within reach distance of the
     *         target Block, false otherwise
     */
    public boolean isWorking()
    {
        return workerReachedBlock;
    }

    /**
     * @param ent
     * @return true when the entity currently is within reach distance of the
     *         target Block, false otherwise
     */
    private boolean isEntityInAccessRange(EntityLivingBase ent)
    {
        return (ent.getDistanceSq(this.posX, this.posY, this.posZ) < accessRangeSq);
    }

    /**
     * @param workerX
     * @param workerY
     * @param workerZ
     * @return an Array of pathable AStarNodes, starting with the closest one to
     *         parameter coordinates and ascending. Array can be size 0 but is
     *         != null
     */
    private AStarNode[] getAccessNodesSorted(int workerX, int workerY, int workerZ)
    {
        return AStarStatic.getAccessNodesSorted(worker.worldObj, workerX, workerY, workerZ, posX, posY, posZ);
    }

    /**
     * Figures out what ItemStack would result from breaking a Block in the
     * World
     */
    protected List<ItemStack> getItemStacksFromWorldBlock(World world, int i, int j, int k)
    {
        Block block = world.getBlockState(new BlockPos(i, j, k)).getBlock();
        Material m = block.getMaterial();
        
        if (block == Blocks.air || m == Material.water || m == Material.lava || m == Material.leaves || m == Material.plants)
        {
            return new ArrayList<ItemStack>();
        }
        
        BlockPos pos = new BlockPos(i, j, k);
        IBlockState state = world.getBlockState(pos);
        return block.getDrops(world, pos, state, state.getBlock().getMetaFromState(state));
    }

    protected void putBlockHarvestInWorkerInventory(List<ItemStack> stackList)
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
