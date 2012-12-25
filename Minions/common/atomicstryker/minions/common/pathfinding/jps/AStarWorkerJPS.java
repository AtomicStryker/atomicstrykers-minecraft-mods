package atomicstryker.minions.common.pathfinding.jps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import atomicstryker.minions.common.pathfinding.AStarNode;
import atomicstryker.minions.common.pathfinding.AStarPathPlanner;
import atomicstryker.minions.common.pathfinding.AStarStatic;
import atomicstryker.minions.common.pathfinding.AStarWorker;

public class AStarWorkerJPS extends AStarWorker
{
    /**
     * Important preset value. Determines after how many non-jump-nodes in a
     * direction an abort is executed, in order to prevent near-infinite
     * loops in cases of unobstructed space pathfinding.
     * After this distance is met, the reached node is perceived as jump node
     * by default and treated as such.
     */
    private final static int MAX_SKIP_DISTANCE = 25;
    
    private final PriorityQueue<NodeWithDirections> queue;
    private NodeWithDirections currentNWD;
    private AStarNode skipperNode;
    private int x;
    private int y;
    private int yAheadCached;
    private int z;
    private int dist;
    private boolean foundPath;

    public AStarWorkerJPS(AStarPathPlanner creator)
    {
        super(creator);
        queue = new PriorityQueue<NodeWithDirections>();
    }

    public ArrayList<AStarNode> getPath(AStarNode start, AStarNode end, boolean searchMode)
    {
        foundPath = false;
        NodeWithDirections startNode = new NodeWithDirections(start);
        queue.offer(startNode);
        targetNode = end;
        currentNWD = startNode;
        
        //create fake parent for start node on side opposite to target node
        int parentXO = (start.x - end.x < 0) ? -1: 1;
        int parentZO = (start.z - end.z < 0) ? 1: -1;
        start.parent = new AStarNode(start.x+parentXO, start.y, start.z+parentZO, 0, null);
        
        for(; !foundPath;)
        {
            if (currentNWD == null)
            {
                return null;
            }
            
            if (checkNextDirection(currentNWD))
            {
                // we found the target! OH, MY!
                foundPath = true;
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
        
        // TODO this doesn't take skipped nodes into account. Interpolate something something.
        ArrayList<AStarNode> foundpath = new ArrayList<AStarNode>();
        foundpath.add(currentNWD.getAStarNode());
        AStarNode jumpTo = currentNWD.getAStarNode().parent;
        while (!jumpTo.equals(start))
        {
            foundpath.add(jumpTo);
            jumpTo = jumpTo.parent;
        }
        return foundpath;
    }
    
    /**
     * Executes the Skip-movement along the next Direction that is still open
     * If it runs into a Jump point, it adds the natural and forced
     * Neighbours into the open queue and returns false
     * If it runs over the target node, sets the target Node parent to the first
     * node of it's path and return true
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
        dist = currentNWD.getAStarNode().getG();
        
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
            
            NodeWithDirections retainedNode = currentNWD;
            for(; skipAheadDiagonal(xDir, zDir); dist+=2)
            {
                // check diagonal node we just jumped onto for being the target
                if (x == targetNode.x && y == targetNode.y && z == targetNode.z)
                {
                    targetNode.parent = currentNWD.getAStarNode();
                    return true;
                }
            }
            currentNWD = retainedNode;
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
        if (newY != 0)
        {
            y = newY; // setup new y value, flush cache
            yAheadCached = 0;
            
            /*
             * For starters, check the node directly ahead. If that is blocked, we bail immediatly.
             */
            int forwardY = getGroundNodeHeight(x+xDir, y, z+zDir);
            boolean forced = false;
            AStarNode forwardNode;
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
                
                //prepare a forward Node
                forwardNode = new AStarNode(x+xDir, y, z+zDir, dist+1, currentNWD.getAStarNode(), targetNode);
                
                // check left forced node, queue it if it exists
                int leftY = getGroundNodeHeight(leftX, y, leftZ);
                if (leftY == 0)
                {
                    forced = true;
                    int yForcedLeft = getGroundNodeHeight(leftX+xDir+xDir, yAheadCached, leftZ+zDir+zDir);
                    if (yForcedLeft != 0)
                    {
                        addOrUpdateNode(new AStarNode(leftX+xDir+xDir, yForcedLeft, leftZ+zDir+zDir, dist+2, currentNWD.getAStarNode(), forwardNode));
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
                        addOrUpdateNode(new AStarNode(rightX+xDir+xDir, yForcedRight, rightZ+zDir+zDir, dist+2, currentNWD.getAStarNode(), forwardNode));
                    }
                }
            }
            else // obstacle ahead! ABANDON SHIP
            {
                yAheadCached = 0;
                return false;
            }

            if (!forced && dist < MAX_SKIP_DISTANCE) // lets also force automatically after x blocks, to keep in an area
            {
                return true;
            }
            else // we got a forced node left and/or right, or distance, add the Node a step ahead
            {
                addOrUpdateNode(forwardNode);
            }
        }
        
        return false;
    }
    
    /**
     * Helper method to execute diagonal skipping. Will move diagonally,
     * then check the left and right straights for Jump Points. Upon finding
     * one, will save the current diagonal and any found Jump points into
     * the queue
     * 
     * NOTE: Due to the special case of mc environment (no small, closed
     * map) this actually cannot possibly do more than a single step. It
     * will ALWAYS yield at least itself as jump point.
     * 
     * @param xDir x diagonal offset to use each step
     * @param zDir z diagonal offset to use each step
     * @return true if the move forward was successful and without forced nodes, false otherwise
     */
    private boolean skipAheadDiagonal(int xDir, int zDir)
    {
        x += xDir;
        z += zDir;
        int newY = yAheadCached == 0 ? getGroundNodeHeight(x, y, z) : yAheadCached;
        if (newY != 0)
        {
            y = newY; // setup new y value, flush cache
            yAheadCached = 0;
            
            // save dist, setup base node for followups
            int originDist = dist;
            AStarNode diagBaseNode = new AStarNode(x, y, z, originDist, currentNWD.getAStarNode());
            
            // check x axis
            for(; skipAheadStraight(xDir, 0); dist++)
            {
                if (x == targetNode.x && y == targetNode.y && z == targetNode.z)
                {
                    targetNode.parent = diagBaseNode;
                    foundPath = true;
                    return false;
                }
            }
            
            // reset dist, check z axis
            dist = originDist;
            for(; skipAheadStraight(0, zDir); dist++)
            {
                if (x == targetNode.x && y == targetNode.y && z == targetNode.z)
                {
                    targetNode.parent = diagBaseNode;
                    foundPath = true;
                    return false;
                }
            }
            
            addOrUpdateNode(diagBaseNode);
        }
        
        return false;
    }
    
    /**
     * Adds a new AStarNode to the queue unless it is already among the closed nodes,
     * in which case it only updates the closed Node with the new distance.
     * @param newNode Node to be saved
     */
    private void addOrUpdateNode(AStarNode newNode)
    {
        if (closedNodes.contains(newNode))
        {
            Iterator<AStarNode> iter = closedNodes.iterator();
            while (iter.hasNext())
            {
                AStarNode toUpdate = iter.next();
                
                if (newNode.equals(toUpdate))
                {
                    toUpdate.updateDistance(newNode.getG(), newNode.parent);
                    break;
                }
            }
        }
        else
        {
            queue.offer(new NodeWithDirections(newNode));
        }
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
