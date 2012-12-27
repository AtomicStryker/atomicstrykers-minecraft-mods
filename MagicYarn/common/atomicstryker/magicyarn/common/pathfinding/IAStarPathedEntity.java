package atomicstryker.magicyarn.common.pathfinding;

import java.util.ArrayList;

/**
 * Smallest ever interface. One can create his own AStarPathed Entities with this.
 * 
 * 
 * @author AtomicStryker
 */

public interface IAStarPathedEntity
{
	public void onFoundPath(ArrayList<AStarNode> result);
	
	public void onNoPathAvailable();
}