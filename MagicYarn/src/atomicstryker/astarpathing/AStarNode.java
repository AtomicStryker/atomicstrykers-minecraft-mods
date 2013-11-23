package atomicstryker.astarpathing;


/**
 * Path Node class for AstarPath
 * 
 * 
 * @author AtomicStryker
 */

public class AStarNode implements Comparable<AStarNode>, Cloneable
{
	final public int x;
	final public int y;
	final public int z;
	final AStarNode target;
	
	public AStarNode parent;
	
	/**
	 * AStar G value, the total distance from the start Node to this Node
	 */
	private int g;
	
	/**
	 * AStar H value, cost to goal estimated value, sometimes called heuristic value
	 */
	private double h;
	
	/**
	 * AStarNode constructor
	 * @param ix x coordinate
	 * @param iy y coordinate
	 * @param iz z coordinate
	 * @param dist Node reaching distance from start
	 * @param p parent Node
	 */
	public AStarNode(int ix, int iy, int iz, int dist, AStarNode p)
	{
		x = ix;
		y = iy;
		z = iz;
		g = dist;
		parent = p;
		target = null;
	}
	
	public AStarNode(int ix, int iy, int iz, int dist, AStarNode p, AStarNode t)
	{
	    x = ix;
	    y = iy;
	    z = iz;
	    g = dist;
	    parent = p;
	    target = t;
	    updateTargetCostEstimate();
	}
	
	public int getG()
	{
	    return g;
	}
	
	public double getF()
	{
	    return g+h;
	}
	
	/**
	 * Tries to update this Node instance with a new Nodechain to it, but checks
	 * if that improves the Node cost first
	 * @param checkingDistance new G distance if the update is accepted
	 * @param parentOtherNode new parent Node if the update is accepted
	 * @return true if the new cost is lower and the update was accepted, false otherwise
	 */
	public boolean updateDistance(int checkingDistance, AStarNode parentOtherNode)
	{
		if (checkingDistance < g)
		{
			g = checkingDistance;
			parent = parentOtherNode;
			updateTargetCostEstimate();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Computes the H or heuristic value by estimating the total cost from here
	 * to the target Node (if it exists).
	 */
	private void updateTargetCostEstimate()
	{
	    if (target != null)
	    {
	        // we prefer "less distance to target" over "short path" by a huge factor, here 10!
	        h = g + AStarStatic.getDistanceBetweenNodes(this, target)*10;
	    }
	    else
	    {
	        h = 0;
	    }
	}
    
    @Override
    public int compareTo(AStarNode o)
    {
        if (getF() < o.getF()) // lower cost = smaller natural value
        {
            return -1;
        }
        else if (getF() > o.getF()) // higher cost = higher natural value
        {
            return 1;
        }
        
    	return 0;
    }
    
    @Override
    public boolean equals(Object checkagainst)
    {
        if (checkagainst instanceof AStarNode)
        {
            AStarNode check = (AStarNode) checkagainst;
            if (check.x == x && check.y == y && check.z == z)
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public AStarNode clone()
    {
        return new AStarNode(x, y, z, g, parent);
    }
	
    @Override
    public int hashCode()
    {
        return (x << 16) ^ z ^(y<<24);
    }
    
    @Override
    public String toString()
    {
        if (parent == null)
            return String.format("[%d|%d|%d], dist %d, F: %f", x, y, z, g, getF());
        else
            return String.format("[%d|%d|%d], dist %d, parent [%d|%d|%d], F: %f", x, y, z, g, parent.x, parent.y, parent.z, getF());
    }
}