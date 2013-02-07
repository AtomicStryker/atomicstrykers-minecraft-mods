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
        return worker.getState() != Thread.State.NEW;
    }

    public void getPath(int startx, int starty, int startz, int destx, int desty, int destz, boolean allowDropping)
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
        
        AStarNode starter = new AStarNode(startx, starty, startz, 0, null);
        AStarNode finish = new AStarNode(destx, desty, destz, -1, null);
        getPath(starter, finish, allowDropping);
    }

    public void getPath(AStarNode start, AStarNode end, boolean allowDropping)
    {
        if (isBusy())
        {
            //System.out.println("getPath called while busy, stopping...");
            stopPathSearch();
        }
        
        while (accesslock) { Thread.yield(); }
        flushWorker();
        accesslock = true;
        
        lastStart = start;
        lastEnd = end;
        
        worker.setup(worldObj, start, end, allowDropping);
        worker.start();

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
            //System.out.println("JPS fail recorded for "+lastStart+" to "+lastEnd);
            getPath(lastStart, lastEnd, false);
        }
        else if (pathedEntity != null)
        {
            //System.out.println("Total AStar fail recorded for "+lastStart+" to "+lastEnd);
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
            worker = isJPS ? new AStarWorkerJPS(this) : new AStarWorker(this);
        }
    }
}