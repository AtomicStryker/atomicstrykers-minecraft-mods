package atomicstryker.magicyarn.client;

import java.util.Comparator;


public class AStarNode
{
	final public int x;
	final public int y;
	final public int z;
	
	public AStarNode parent = null;
	public int parentxoffset;
	public int parentyoffset;
	public int parentzoffset;
	
	public int g_BlockDistToStart;
	public double f_distanceToGoal;
	public double h_reachCost;
	
	public AStarNode(int ix, int iy, int iz, int dist)
	{
		x = ix;
		y = iy;
		z = iz;
		g_BlockDistToStart = dist;
	}
	
	public AStarNode(int ix, int iy, int iz, int dist, AStarNode node)
	{
		x = ix;
		y = iy;
		z = iz;
		g_BlockDistToStart = dist;
		parent = node;
		updateParentOffset();
	}
	
	public void updateReachCost(double input)
	{
		f_distanceToGoal = input;
		h_reachCost = g_BlockDistToStart + f_distanceToGoal;
	}
	
	public boolean updateDistance(int checkingDistance, AStarNode parentOtherNode)
	{
		if (checkingDistance < this.g_BlockDistToStart)
		{
			g_BlockDistToStart = checkingDistance;
			h_reachCost = g_BlockDistToStart + f_distanceToGoal;
			parent = parentOtherNode;
			updateParentOffset();
			return true;
		}
		
		return false;
	}
	
	public void updateParentOffset()
	{
		parentxoffset = parent.x - x;
		parentyoffset = parent.y - y;
		parentzoffset = parent.z - z;
	}
	
    @Override
	public boolean equals(Object checkagainst)
	{
		if (checkagainst instanceof AStarNode)
		{
			AStarNode check = (AStarNode) checkagainst;
			if (check.hashCode() == hashCode() && check.x == x && check.y == y && check.z == z)
			{
				return true;
			}
		}
		
		return false;
	}
    
    public int compare(Object a, Object b)
    {
    	if (a instanceof AStarNode && b instanceof AStarNode)
    	{
    		AStarNode anode = (AStarNode) a;
    		AStarNode bnode = (AStarNode) b;
    		if (anode.h_reachCost == bnode.h_reachCost)
    		{
    			return 0;
    		}
    		else if (anode.h_reachCost < bnode.h_reachCost)
    		{
    			return -1;
    		}
    		else return 1;
    	}
    	
    	return 0;
    }
	
    @Override
    public int hashCode()
    {
        return (x << 16) ^ z ^(y<<24);
    }
}