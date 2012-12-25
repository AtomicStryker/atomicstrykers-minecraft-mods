package atomicstryker.minions.common.pathfinding.jps;

import atomicstryker.minions.common.pathfinding.AStarNode;

/**
 * Helper data structure to keep track of the Directions from a Node we already checked
 * @author AtomicStryker
 *
 */
public class NodeWithDirections implements Comparable
{
    private final AStarNode node;
    private int index;
    
    public NodeWithDirections(AStarNode n)
    {
        node = n;
        index = 0;
    }
    
    public AStarNode getAStarNode()
    {
        return node;
    }
    
    public EnumDirection getCurrentDirection()
    {
        return EnumDirection.values()[index];
    }
    
    public void closeCurrentDirection()
    {
        index++;
    }
    
    public int getDirIndex()
    {
        return index;
    }

    @Override
    public int compareTo(Object o)
    {
        return node.compareTo(o);
    }
}