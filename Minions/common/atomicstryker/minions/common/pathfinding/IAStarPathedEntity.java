package atomicstryker.minions.common.pathfinding;

import java.util.ArrayList;

/**
 * Interface to facilitate the use of AStar pathfinding as implemented here.
 * Used with the PathPlanner implementation to notify the client about found
 * paths or lack thereof.
 * 
 * 
 * @author AtomicStryker
 */

public interface IAStarPathedEntity
{
    /**
     * Returned by a Pathfinding worker that found a solution for the
     * provided Start and End Nodes. Solution is provided as list of
     * AStarNodes, with the Target/End Node being index 0 and the
     * Origin/Start Node being the last in the list.
     * 
     * @param result ArrayList of AStarNodes depicting a path solution
     */
	public void onFoundPath(ArrayList<AStarNode> result);
	
	/**
     * Returned by a Pathfinding worker that either ran out of Nodes
     * to check or hit it's maximum searching time limit.
	 */
	public void onNoPathAvailable();
}