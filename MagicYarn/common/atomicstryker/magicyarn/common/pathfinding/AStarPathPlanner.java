package atomicstryker.magicyarn.common.pathfinding;

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
    private boolean accesslock;
    private boolean isJPS;
    private AStarNode lastStart;
    private AStarNode lastEnd;
    
    public AStarPathPlanner(World world, IAStarPathedEntity ent)
    {
        worker = new AStarWorker(this);
        worldObj = world;
        accesslock = false;
        isWorking = false;
        pathedEntity = ent;
        isJPS = true;
    }
    
    public void setJPS(boolean b)
    {
        isJPS = b;
        flushWorker();
    }

    public boolean isBusy()
    {
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
        flushWorker();
        accesslock = true;
        
        lastStart = start;
        lastEnd = end;
        
        worker.setup(worldObj, start, end, allowDropping);
        worker.start();
        isWorking = true;

        accesslock = false;
    }

    public void onFoundPath(ArrayList<AStarNode> result)
    {
        setJPS(true);
        if (pathedEntity != null)
        {
            pathedEntity.onFoundPath(result);
        }
    }

    public void onNoPathAvailable()
    {
        if (isJPS) // in case of JPS failure switch to old best first algorithm
        {
            setJPS(false);
            getPath(lastStart, lastEnd, false);
            return;
        }
        
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
            isWorking = false;
            worker = isJPS ? new AStarWorkerJPS(this) : new AStarWorker(this);
        }
    }
}