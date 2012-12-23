package atomicstryker.minions.common.pathfinding;

import java.util.ArrayList;

import net.minecraft.world.World;

/**
 * Control Class for AstarPath, creates workers and manages returns
 * 
 * @author AtomicStryker
 */

public class AStarPathPlanner
{
    private AStarWorker worker;
    private World worldObj;
    private boolean isWorking;
    private IAStarPathedEntity pathedEntity;
    private long timeStarted;
    private boolean accesslock;

    public AStarPathPlanner(World world)
    {
        worldObj = world;
        accesslock = false;
        timeStarted = 0L;
        isWorking = false;
    }

    public AStarPathPlanner(World worldObj, IAStarPathedEntity ent)
    {
        this(worldObj);
        pathedEntity = ent;
    }

    public boolean isBusy()
    {
        if (timeStarted != 0L && System.currentTimeMillis() - timeStarted > 5000L)
        {
            onNoPathAvailable();
        }

        return isWorking;
    }

    public void getPath(int startx, int starty, int startz, int destx, int desty, int destz, boolean allowDropping)
    {
        AStarNode starter = new AStarNode(startx, starty, startz, 0, null);
        AStarNode finish = new AStarNode(destx, desty, destz, -1, null);

        getPath(starter, finish, allowDropping);
    }

    public void getPath(AStarNode start, AStarNode end, boolean allowDropping)
    {
        if (isWorking)
        {
            stopPathSearch();
        }

        while (accesslock) { Thread.yield(); }
        accesslock = true;

        timeStarted = System.currentTimeMillis();
        worker = new AStarWorker(this);
        worker.setup(worldObj, start, end, allowDropping);
        worker.start();
        isWorking = true;

        accesslock = false;
    }

    public void onFoundPath(ArrayList<AStarNode> result)
    {
        flushWorker();
        if (pathedEntity != null)
        {
            pathedEntity.onFoundPath(result);
        }
    }

    public void onNoPathAvailable()
    {
        flushWorker();
        if (pathedEntity != null)
        {
            pathedEntity.onNoPathAvailable();
        }
    }

    public void stopPathSearch()
    {
        flushWorker();
        if (pathedEntity != null)
        {
            pathedEntity.onNoPathAvailable();
        }
    }

    private void flushWorker()
    {
        if (!accesslock) // only flush if we arent starting!
        {
            timeStarted = 0L;
            if (worker != null)
            {
                worker.interrupt();
                worker = null;
            }
            isWorking = false;
        }
    }
}