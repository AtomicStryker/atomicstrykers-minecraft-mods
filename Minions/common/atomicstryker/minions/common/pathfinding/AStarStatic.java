package atomicstryker.minions.common.pathfinding;

import java.util.ArrayList;

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

public final class AStarStatic
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
	
	static double getDistanceBetweenNodes(AStarNode a, AStarNode b)
	{
		return (Math.abs(a.x-b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z));
		//return Math.sqrt(Math.pow((a.x - b.x), 2) + Math.pow((a.y - b.y), 2) + Math.pow((a.z - b.z), 2));
	}
	
	public static double getDistanceBetweenCoords(int x, int y, int z, int posX, int posY, int posZ)
	{
		return Math.sqrt(Math.pow(x-posX, 2) + Math.pow(y-posY, 2) + Math.pow(z-posZ, 2));
	}
	
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
	
	static boolean isLadder(int id)
	{
		return (id == Block.ladder.blockID
			|| id == 242
			|| id == 243);
	}
	
    public static AStarNode[] getAccessNodesSorted(World worldObj, int workerX, int workerY, int workerZ, int posX, int posY, int posZ)
    {
    	ArrayList resultList = new ArrayList();

		AStarNode check;
		for (int xIter = -2; xIter <= 2; xIter++)
		{
			for (int zIter = -2; zIter <= 2; zIter++)
			{
				for (int yIter = -3; yIter <= 2; yIter++)
				{
					check = new AStarNode(posX+xIter, posY+yIter, posZ+zIter, 0);
					
					if (AStarStatic.isViable(worldObj, check, 1))
					{
						check.f_distanceToGoal = ((AStarStatic.getDistanceBetweenCoords(workerX, workerY, workerZ, check.x, check.y, check.z)));
						
						int index = 0;
						if (resultList.size() != 0)
						{
							while(index < resultList.size() && resultList.get(index) != null && ((AStarNode)resultList.get(index)).f_distanceToGoal <= check.f_distanceToGoal)
							{
								index++;
							}
						}
						resultList.add(index, check);
					}
				}
			}
		}
		
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
		return MinionsCore.translateAStarPathtoPathEntity(input);
	}
}