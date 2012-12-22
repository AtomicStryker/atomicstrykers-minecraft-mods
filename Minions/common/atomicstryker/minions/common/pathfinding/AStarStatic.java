package atomicstryker.minions.common.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.minions.common.MinionsCore;

/**
 * Static parts of AStarPath calculation and translation
 * 
 * @author AtomicStryker
 */

public class AStarStatic
{
    
	static boolean isViable(World worldObj, AStarNode target, int yoffset)
	{
		int x = target.x;
		int y = target.y;
		int z = target.z;
		int id = worldObj.getBlockId(x, y, z);

		if (id == Block.ladder.blockID)
		{
			return true;
		}

		if (!isPassableBlock(worldObj, x, y, z)
		|| !isPassableBlock(worldObj, x, y+1, z)
		|| (isPassableBlock(worldObj, x, y-1, z) && (id != Block.waterStill.blockID || id != Block.waterMoving.blockID)))
		{
			return false;
		}

		if (yoffset < 0) yoffset *= -1;
		int ycheckhigher = 1;
		while (ycheckhigher <= yoffset)
		{
			if (!isPassableBlock(worldObj, x, y+yoffset, z))
			{
				return false;
			}
			ycheckhigher++;
		}

		return true;
	}
	
	public static boolean isPassableBlock(World worldObj, int ix, int iy, int iz)
	{
		int id = worldObj.getBlockId(ix, iy, iz);

		if (id != 0)
		{
			return !Block.blocksList[id].blockMaterial.isSolid();
		}

		return true;
	}
	
	public static int getIntCoordFromDoubleCoord(double input)
	{
		return MathHelper.floor_double(input);
	}
	
	static double getEntityLandSpeed(EntityLiving entLiving)
	{
		return Math.sqrt((entLiving.motionX * entLiving.motionX) + (entLiving.motionZ * entLiving.motionZ));
	}
	
	/**
	 * Calculates the Euclidian distance between 2 AStarNode instances
	 * @param a Node
	 * @param b Node
	 * @return Euclidian Distance between the 2 given Nodes
	 */
	static double getDistanceBetweenNodes(AStarNode a, AStarNode b)
	{
		return Math.sqrt(Math.pow((a.x - b.x), 2) + Math.pow((a.y - b.y), 2) + Math.pow((a.z - b.z), 2));
	}
	
	public static double getDistanceBetweenCoords(int x, int y, int z, int posX, int posY, int posZ)
	{
		return Math.sqrt(Math.pow(x-posX, 2) + Math.pow(y-posY, 2) + Math.pow(z-posZ, 2));
	}
	
	/**
	 * Array of standard 3D neighbour Block offsets and their 'reach cost' as fourth value
	 */
	final static int candidates[][] =
	{
		{
			0, 0, -1, 1
		}, {
			0, 0, 1, 1
		}, {
			0, 1, 0, 1
		}, {
			1, 0, 0, 1
		}, {
			-1, 0, 0, 1
		}, {
			1, 1, 0, 2
		}, {
			-1, 1, 0, 2
		}, {
			0, 1, 1, 2
		}, {
			0, 1, -1, 2
		}, {
			1, -1, 0, 1
		}, {
			-1, -1, 0, 1
		}, {
			0, -1, 1, 1
		}, {
			0, -1, -1, 1
		}
	};

	final static int candidates_allowdrops[][] =
	{
		{
			0, 0, -1, 1
		}, {
			0, 0, 1, 1
		}, {
			1, 0, 0, 1
		}, {
			-1, 0, 0, 1
		}, {
			1, 1, 0, 2
		}, {
			-1, 1, 0, 2
		}, {
			0, 1, 1, 2
		}, {
			0, 1, -1, 2
		}, {
			1, -1, 0, 1
		}, {
			-1, -1, 0, 1
		}, {
			0, -1, 1, 1
		}, {
			0, -1, -1, 1
		}, {
			1, -2, 0, 1
		}, {
			-1, -2, 0, 1
		}, {
			0, -2, 1, 1
		}, {
			0, -2, -1, 1
		}
	};
	
	static boolean isLadder(World world, int blockID, int x, int y, int z)
	{
	    Block b = Block.blocksList[blockID];
	    if (b != null)
	    {
	        return b.isLadder(world, x, y, z);
	    }
	    return false;
	}
	
    public static AStarNode[] getAccessNodesSorted(World worldObj, int workerX, int workerY, int workerZ, int posX, int posY, int posZ)
    {
    	ArrayList<AStarNode> resultList = new ArrayList<AStarNode>();

		AStarNode check;
		for (int xIter = -2; xIter <= 2; xIter++)
		{
			for (int zIter = -2; zIter <= 2; zIter++)
			{
				for (int yIter = -3; yIter <= 2; yIter++)
				{
					check = new AStarNode(posX+xIter, posY+yIter, posZ+zIter, Math.abs(xIter)+Math.abs(yIter), null);
					if (AStarStatic.isViable(worldObj, check, 1))
					{
						resultList.add(check);
					}
				}
			}
		}
		
		Collections.sort(resultList);
		
		int count = 0;
		AStarNode[] returnVal = new AStarNode[resultList.size()];
		while (!resultList.isEmpty() && (check = (AStarNode) resultList.get(0)) != null)
		{
			returnVal[count] = check;
			resultList.remove(0);
			count++;
		}
		
    	return returnVal;
    }
	
    public static AS_PathEntity translateAStarPathtoPathEntity(ArrayList input)
    {
        AS_PathPoint[] points = new AS_PathPoint[input.size()];
        AStarNode reading;
        int i = 0;
        int size = input.size();
        //System.out.println("Translating AStar Path with "+size+" Hops:");

        while(size > 0)
        {
            reading = (AStarNode) input.get(size-1);
            points[i] = new AS_PathPoint(reading.x, i == 0 ? reading.y+1 : reading.y, reading.z); // MC demands the first path point to be at +1 height for some fucking reason
            points[i].isFirst = i == 0;
            points[i].setIndex(i);
            points[i].setTotalPathDistance(i);
            points[i].setDistanceToNext(1F);
            points[i].setDistanceToTarget(size);

            if (i>0)
            {
                points[i].setPrevious(points[i-1]);
            }
            //System.out.println("PathPoint: ["+reading.x+"|"+reading.y+"|"+reading.z+"]");

            input.remove(size-1);
            size --;
            i++;
        }
        //System.out.println("Translated AStar PathEntity with length: "+ points.length);

        return new AS_PathEntity(points);
    }
}