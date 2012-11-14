package atomicstryker.minions.common.pathfinding;

import java.lang.Thread.State;
import java.util.*;

import net.minecraft.src.World;

/**
 * Control Class for AstarPath, creates workers and manages returns
 * 
 * @author AtomicStryker
 */

public class AStarPath
{
    private AStarWorker worker;
    private World worldObj;
    private boolean isWorking = false;
    private ArrayList pathResult;
    private IAStarPathedEntity pathedEntity;
    private long timeStarted = 0L;
    private boolean accesslock;

    public AStarPath(World worldObj)
    {
        this.worldObj = worldObj;
        accesslock = false;
    }

    public AStarPath(World worldObj, IAStarPathedEntity ent)
    {
        this.worldObj = worldObj;
        pathedEntity = ent;
        accesslock = false;
    }

    public boolean isBusy()
    {
        if (timeStarted != 0L && System.currentTimeMillis() - timeStarted > 5000L)
        {
            OnNoPathAvailable();
        }

        return isWorking;
    }

    public void getPath(int startx, int starty, int startz, int destx, int desty, int destz, boolean allowDropping)
    {
        AStarNode starter = new AStarNode(startx, starty, startz, 0);
        AStarNode finish = new AStarNode(destx, desty, destz, -1);;

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

    public void OnFoundPath(ArrayList result)
    {
        flushWorker();
        pathResult = result;

        if (pathedEntity != null)
        {
            pathedEntity.onFoundPath(result);
        }
    }

    public void OnNoPathAvailable()
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