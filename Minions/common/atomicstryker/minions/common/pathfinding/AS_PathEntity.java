package atomicstryker.minions.common.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Vec3;

/**
 * Extension of the Minecraft PathEntity to try and fix some of its horrible shortcomings
 * 
 * 
 * @author AtomicStryker
 */

public class AS_PathEntity extends PathEntity
{
	private long timeLastPathIncrement = 0L;
	private final PathPoint[] pointsCopy;
	private int pathIndexCopy;
	
    public AS_PathEntity(PathPoint[] var1)
    {
    	super(var1);
    	timeLastPathIncrement = System.currentTimeMillis();
    	this.pointsCopy = var1;
    	this.pathIndexCopy = 0;
    }
    
    public void advancePathIndex()
    {
        timeLastPathIncrement = System.currentTimeMillis();
        pathIndexCopy++;
        setCurrentPathIndex(pathIndexCopy);
    }
    
    @Override
    public void setCurrentPathIndex(int par1)
    {
        timeLastPathIncrement = System.currentTimeMillis();
        pathIndexCopy = par1;
        super.setCurrentPathIndex(par1);
    }

    public long getTimeSinceLastPathIncrement()
    {
    	return (System.currentTimeMillis() - timeLastPathIncrement);
    }
    
    public PathPoint getCurrentTargetPathPoint()
    {
    	if (this.isFinished()) return null;
    	return this.pointsCopy[pathIndexCopy];
    }
    
    @Override
    public Vec3 getPosition(Entity var1)
    {
    	if (super.isFinished()) return null;
    	return super.getPosition(var1);
    }
}
