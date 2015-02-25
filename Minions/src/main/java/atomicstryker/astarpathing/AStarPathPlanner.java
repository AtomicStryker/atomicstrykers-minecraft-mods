package atomicstryker.astarpathing;

import java.util.ArrayList;

import net.minecraft.world.World;
import atomicstryker.minions.common.MinionsCore;

/**
 * Control Class for AstarPath, creates workers and manages returns
 * 
 * @author AtomicStryker
 */

public class AStarPathPlanner
{
    private AStarWorker worker;
    private World worldObj;
    private IAStarPathedEntity pathedEntity;
    private boolean isJPS;
    private AStarNode lastStart;
    private AStarNode lastEnd;
    
    private AStarNode[] queue;
    private int qindex;
    
    public AStarPathPlanner(World world, IAStarPathedEntity ent)
    {
        worldObj = world;
        pathedEntity = ent;
        isJPS = true;
        queue = null;
        qindex = 0;
    }
    
    public void setJPS(boolean b)
    {
        isJPS = b;
    }

    public boolean isBusy()
    {
        if (worker == null)
        {
            return false;
        }
        return worker.getState() != Thread.State.NEW;
    }
    
    private int checkYCoordViability(int startx, int starty, int startz)
    {
        if (!AStarStatic.isViable(worldObj, startx, starty, startz, 0))
        {
            starty--;
        }
        if (!AStarStatic.isViable(worldObj, startx, starty, startz, 0))
        {
            starty+=2;
        }
        if (!AStarStatic.isViable(worldObj, startx, starty, startz, 0))
        {
            starty--;
        }
        return starty;
    }

    public void getPath(int startx, int starty, int startz, int destx, int desty, int destz, boolean allowDropping)
    {
        // System.out.printf("getPath from [%d|%d|%d] to [%d|%d|%d]\n", startx, starty, startz, destx, desty, destz);
        starty = checkYCoordViability(startx, starty, startz);
        final AStarNode starter = new AStarNode(startx, starty, startz, 0, null);
        final AStarNode finish = new AStarNode(destx, desty, destz, -1, null);
        getPath(starter, finish, allowDropping);
    }
    
    public void getPath(int startx, int starty, int startz, AStarNode[] possibles, boolean allowDropping)
    {
        starty = checkYCoordViability(startx, starty, startz);
        queue = possibles;
        qindex = 0;
        final AStarNode starter = new AStarNode(startx, starty, startz, 0, null);
        getPath(starter, queue[qindex], allowDropping);
    }

    private synchronized void getPath(AStarNode start, AStarNode end, boolean allowDropping)
    {        
        lastStart = start;
        lastEnd = end;
        
        worker = isJPS ? new AStarWorkerJPS(this) : new AStarWorker(this);
        worker.setup(worldObj, start, end, allowDropping);
        worker.start();
    }

    public void onFoundPath(AStarWorker aStarWorker, ArrayList<AStarNode> result)
    {
        // System.out.println("onFoundPath from "+aStarWorker+", result "+result+", cached worker: "+worker);
        if (aStarWorker.equals(worker)) // disregard solutions from abandoned workers
        {
            setJPS(true);
            if (pathedEntity != null)
            {
                pathedEntity.onFoundPath(result);
            }
        }
    }

    public void onNoPathAvailable()
    {
        if (queue != null && qindex+1 < queue.length)
        {
            qindex++;
            getPath(lastStart, queue[qindex], false);
        }
        else
        {
            if (isJPS) // in case of JPS failure switch to old best first algorithm
            {
                setJPS(false);
                // System.out.println("JPS fail recorded for "+lastStart+" to "+lastEnd);
                getPath(lastStart, lastEnd, false);
            }
            else if (pathedEntity != null)
            {
                MinionsCore.debugPrint("Total AStar fail recorded for "+lastStart+" to "+lastEnd);
                pathedEntity.onNoPathAvailable();
            }
        }
    }
    
}