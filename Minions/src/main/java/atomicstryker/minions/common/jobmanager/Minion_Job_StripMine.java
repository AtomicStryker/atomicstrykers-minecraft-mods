package atomicstryker.minions.common.jobmanager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
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

    public Minion_Job_StripMine(EntityMinion m, int ix, int iy, int iz)
    {
        super(ix, iy, iz);

        currentSegment = -1;

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

        worldObj = m.worldObj;

        startX = this.pointOfOrigin.getX();
        startY = this.pointOfOrigin.getY();
        startZ = this.pointOfOrigin.getZ();

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

    }

    @Override
    public boolean onJobUpdateTick()
    {
        super.onJobUpdateTick();
        if (workerList.isEmpty())
        {
            onJobFinished();
            return true;
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
        return isFinished;
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
            if (worldObj.getLightBrightness(new BlockPos(nextX, startY, nextZ)) < 10F)
            {
                int event = ForgeHooks.onBlockBreakEvent(worldObj, worldObj.getWorldInfo().getGameType(), 
                        (EntityPlayerMP) workerList.get(0).master, new BlockPos(nextX-2*xDirection, startY, nextZ-2*zDirection));
                if (event != -1)
                {
                    worldObj.setBlockState(new BlockPos(nextX-2*xDirection,  startY,  nextZ-2*zDirection),  Blocks.torch.getStateFromMeta( 0));
                }
            }
        }

        // check previous segment floor for having been dug away
        for (int len = 0; len < 4; len++)
        {
            if (worldObj.isAirBlock(new BlockPos(nextX-(xDirection*len), startY-1, nextZ-(zDirection*len))))
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
        if (MinionsCore.instance.isBlockValueable(worldObj.getBlockState(new BlockPos(x, y, z)).getBlock()))
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