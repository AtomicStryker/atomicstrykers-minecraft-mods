package atomicstryker.minions.common.pathfinding.jps;

import java.util.ArrayList;
import java.util.PriorityQueue;

import atomicstryker.minions.common.pathfinding.AStarNode;
import atomicstryker.minions.common.pathfinding.AStarPathPlanner;
import atomicstryker.minions.common.pathfinding.AStarStatic;
import atomicstryker.minions.common.pathfinding.AStarWorker;

import net.minecraftforge.common.ForgeDirection;

public class AStarWorkerJPS extends AStarWorker
{
    private final PriorityQueue<NodeWithDirections> queue;
    private NodeWithDirections currentNWD;
    private AStarNode skipperNode;
    private int x;
    private int y;
    private int yAheadCached;
    private int z;
    private int dist;

    public AStarWorkerJPS(AStarPathPlanner creator)
    {
        super(creator);
        queue = new PriorityQueue<NodeWithDirections>();
    }

    public ArrayList<AStarNode> getPath(AStarNode start, AStarNode end, boolean searchMode)
    {
        NodeWithDirections startNode = new NodeWithDirections(start);
        queue.offer(startNode);
        targetNode = end;
        currentNWD = startNode;
        
        //create fake parent for start node on side opposite to target node
        int parentXO = (start.x - end.x < 0) ? -1: 1;
        int parentZO = (start.z - end.z < 0) ? 1: -1;
        start.parent = new AStarNode(start.x+parentXO, start.y, start.z+parentZO, 0, null);
        
        for(;;)
        {
            if (queue.isEmpty())
            {
                return null;
            }
            
            if (checkNextDirection(currentNWD)) // TODO if this works on first try ill sacrifice a goat
            {
                break;
            }
            
            while (currentNWD.getDirIndex() >= EnumDirection.DIAG_R_RIGHT.ordinal())
            {
                if (currentNWD == startNode && currentNWD.getDirIndex() == EnumDirection.BACKWARDS.ordinal())
                {
                    // first node and first node only can go backwards, once!
                    break;
                }
                closedNodes.add(currentNWD.getAStarNode());
                currentNWD = queue.poll();
            }
        }
        
        ArrayList<AStarNode> foundpath = new ArrayList<AStarNode>();
        foundpath.add(currentNWD.getAStarNode());
        while (!currentNWD.getAStarNode().equals(start))
        {
            // TODO create a Node-by-Node path from the jumping Points
            AStarNode jumpTo = currentNWD.getAStarNode().parent;
            
        }
        return foundpath;
    }
    
    /**
     * Executes the Skip-movement along the next Direction that is still open
     * If it runs into a Jump point, it adds the natural and forced
     * Neighbours into the open queue and returns false
     * If it runs over the target node, sets the target Node as currentNWD
     * and returns true
     * 
     * @param nodeWithDirections current NodeWithDirections
     * @return true if the target node was run over, false otherwise
     */
    private boolean checkNextDirection(NodeWithDirections nodeWithDirections)
    {
        EnumDirection dir = nodeWithDirections.getCurrentDirection();
        AStarNode node = nodeWithDirections.getAStarNode();
        x = node.x;
        y = node.y;
        z = node.z;
        dist = 1;
        
        // compute direction parent -> this, aka STRAIGHT
        int xDir = node.x - node.parent.x;
        int zDir = node.z - node.parent.z;
        
        if (dir.isStraightMove())
        {
            // rotation!
            AbsToRelDirStraight rotMap = new AbsToRelDirStraight(xDir, zDir);
            int[] rotated = rotMap.getRotatedOffsets(dir);
            xDir = rotated[0];
            zDir = rotated[1];
            
            for(; skipAheadStraight(xDir, zDir); dist++)
            {
                // will continue skipping until skipAhead returns false
                // should probably check for target node
                if (x == targetNode.x && y == targetNode.y && z == targetNode.z)
                {
                    targetNode.parent = currentNWD.getAStarNode();
                    return true;
                }
            }
        }
        else
        {
            // rotation!
            AbsToRelDirDiagonal rotMap = new AbsToRelDirDiagonal(xDir, zDir);
            int[] rotated = rotMap.getRotatedOffsets(dir);
            xDir = rotated[0];
            zDir = rotated[1];
            
            //TODO diagonal moves
        }
        
        nodeWithDirections.closeCurrentDirection();
        
        currentNWD = queue.peek(); // in case a new jump point is better!
        return false;
    }
    
    /**
     * Helper method to execute straight skipping. Will continue skipping the worker
     * forward until it runs into an obstacle or forced nodes, at which point the
     * forced nodes are saved.
     * 
     * @param xDir x direction to move towards
     * @param zDir z direction to move towards
     * @return true if the move forward was successful and without forced nodes, false otherwise
     */
    private boolean skipAheadStraight(int xDir, int zDir)
    {
        x += xDir;
        z += zDir;
        int newY = yAheadCached == 0 ? getGroundNodeHeight(x, y, z) : yAheadCached;
        if (newY != 0) // movement straight ahead succeeded!
        {
            y = newY; // setup new y value, flush cache
            yAheadCached = 0;
            
            /*
             * For starters, check the node directly ahead. If that is blocked, we bail immediatly.
             */
            int forwardY = getGroundNodeHeight(x+xDir, y, z+zDir);
            boolean forced = false;
            if (forwardY != 0)
            {
                /* 
                 * now check for forced nodes, the only case that concerns us is the adjacent nodes
                 * left and right being obstacles, at which point the nodes behind those (in moving
                 * direction) can become forced nodes if available.
                 * 
                 * if xDir != 0 we are moving on x, and left and right are z+1 and z-1
                 * if zDir != 0 we are moving on z, and left and right are x+1 and x-1
                 */
                
                int leftX = (xDir != 0) ? 0 : 1;
                int rightX = (xDir != 0) ? 0 : -1;
                int leftZ = (zDir != 0) ? 0 : 1;
                int rightZ = (zDir != 0) ? 0 : -1;
                
                // merge offsets with absolute values
                leftX += x;
                rightX += x;
                leftZ += z;
                rightZ += z;
                
                // cache the forward y so we dont have to run the node twice
                yAheadCached = forwardY;
                
                // check left forced node, queue it if it exists
                int leftY = getGroundNodeHeight(leftX, y, leftZ);
                if (leftY == 0)
                {
                    forced = true;
                    int yForcedLeft = getGroundNodeHeight(leftX+xDir+xDir, yAheadCached, leftZ+zDir+zDir);
                    if (yForcedLeft != 0)
                    {
                        // TODO closed nodes check, update open nodes?
                        queue.offer(new NodeWithDirections(new AStarNode(leftX+xDir+xDir, yForcedLeft, leftZ+zDir+zDir, dist+2, currentNWD.getAStarNode(), targetNode)));
                    }
                }
                
                // check right forced node, queue it if it exists
                int rightY = getGroundNodeHeight(rightX, y, rightZ);
                if (rightY == 0)
                {
                    forced = true;
                    int yForcedRight = getGroundNodeHeight(rightX+xDir+xDir, yAheadCached, rightZ+zDir+zDir);
                    if (yForcedRight != 0)
                    {
                        // TODO closed nodes check, update open nodes?
                        queue.offer(new NodeWithDirections(new AStarNode(rightX+xDir+xDir, yForcedRight, rightZ+zDir+zDir, dist+2, currentNWD.getAStarNode(), targetNode)));
                    }
                }
            }
            else // obstacle ahead! ABANDON SHIP
            {
                yAheadCached = 0;
                return false;
            }

            if (!forced && dist < 25) // lets also force automatically after x blocks, to keep in an area
            {
                return true;
            }
            else // we got a forced node left and/or right, add the one a step ahead
            {
                // TODO closed nodes check, update open nodes?
                queue.offer(new NodeWithDirections(new AStarNode(x+xDir, y, z+zDir, dist+1, currentNWD.getAStarNode(), targetNode)));
            }
        }
        
        return false;
    }
    
    /**
     * Attempts to find a viable Node (height) next to the coordinates provided,
     * with a maximum allowed offset of 1.
     * 
     * @param xN x coordinate
     * @param yN y coordinate
     * @param zN z coordinate
     * @return y coordinate of a viable Node or 0 if no such Node can be found
     */
    private int getGroundNodeHeight(int xN, int yN, int zN)
    {
        AStarNode node = new AStarNode(xN, yN, zN, 0, null);
        if (AStarStatic.isViable(worldObj, node, 0))
        {
            return yN;
        }
        
        node = new AStarNode(xN, yN-1, zN, 0, null);
        if (AStarStatic.isViable(worldObj, node, -1))
        {
            return yN-1;
        }
        
        node = new AStarNode(xN, yN+1, zN, 0, null);
        if (AStarStatic.isViable(worldObj, node, 1))
        {
            return yN+1;
        }
        return 0;
    }
}
