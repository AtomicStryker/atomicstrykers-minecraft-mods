package atomicstryker.minions.common.pathfinding;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class AStarWorkerJPS extends AStarWorker
{
    private final PriorityQueue<NodeWithDirections> queue;
    private NodeWithDirections currentNWD;

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
        
        for(;;)
        {
            if (queue.isEmpty())
            {
                return null;
            }
            
            if (checkNextDirection(currentNWD))
            {
                break;
            }
            
            while (currentNWD.shouldBeClosed())
            {
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
     * If it runs over the target node, sets the target Node as currentNWD
     * and returns true to abort the main loop
     * 
     * @param nodeWithDirections current NodeWithDirections
     * @return true if the target node was run over, false otherwise
     */
    private boolean checkNextDirection(NodeWithDirections nodeWithDirections)
    {
        EnumDirection dir = nodeWithDirections.getCurrentDirection();
        
        if (dir.isStraightMove())
        {
            //TODO
        }
        else
        {
            //TODO
        }
        
        nodeWithDirections.closeCurrentDirection();
        
        currentNWD = queue.peek(); // in case a new jump point is better!
        return false;
    }

    /**
     * Helper data structure to keep track of the Directions from a Node we already checked
     * @author AtomicStryker
     *
     */
    private class NodeWithDirections implements Comparable
    {
        private final AStarNode node;
        private int index;
        
        NodeWithDirections(AStarNode n)
        {
            node = n;
            index = 0;
        }
        
        AStarNode getAStarNode()
        {
            return node;
        }
        
        EnumDirection getCurrentDirection()
        {
            return EnumDirection.values()[index];
        }
        
        void closeCurrentDirection()
        {
            index++;
        }
        
        boolean shouldBeClosed()
        {
            return index >= EnumDirection.values().length;
        }

        @Override
        public int compareTo(Object o)
        {
            return node.compareTo(o);
        }
    }
    
    /**
     * Helper enum for iterating all JPS directions from a Node.
     * Also contains the "relative" movement given a Node and a direction.
     * (1,1) is FORWARD, LEFT. (-1,0) is BACKWARD, NOTHING
     * 
     * @author AtomicStryker
     *
     */
    private enum EnumDirection
    {
        STRAIGHT(1,0),
        DIAG_LEFT(1,1),
        DIAG_RIGTH(1,-1),
        LEFT(0,1),
        RIGHT(0,-1),
        DIAG_B_LEFT(-1,1),
        DIAG_R_RIGHT(-1,-1);
        
        final int xO;
        final int yO;
        
        EnumDirection(int xOffset, int yOffset)
        {
            xO = xOffset;
            yO = yOffset;
        }
        
        boolean isStraightMove()
        {
            return (xO != 0 && yO == 0) || (xO == 0 && yO != 0);
        }
    }
}
