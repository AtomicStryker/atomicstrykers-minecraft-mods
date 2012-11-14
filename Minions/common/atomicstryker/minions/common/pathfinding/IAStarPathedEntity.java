package atomicstryker.minions.common.pathfinding;

import java.util.ArrayList;

/**
 * Smallest ever interface. One can create his own AStarPathed Entities with this.
 * 
 * 
 * @author AtomicStryker
 */

public interface IAStarPathedEntity
{
	public void onFoundPath(ArrayList result);
	
	public void onNoPathAvailable();
}