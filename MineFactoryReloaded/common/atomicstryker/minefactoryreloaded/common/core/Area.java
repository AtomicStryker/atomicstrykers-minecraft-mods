package atomicstryker.minefactoryreloaded.common.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.AxisAlignedBB;

public class Area
{
	public int xMin;
	public int xMax;
	public int yMin;
	public int yMax;
	public int zMin;
	public int zMax;
	
	public Area(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax)
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
	}
	
	public Area(BlockPosition p1, BlockPosition p2)
	{
		BlockPosition pmin = p1.min(p2);
		BlockPosition pmax = p1.max(p2);
		xMin = pmin.x;
		xMax = pmax.x;
		yMin = pmin.y;
		yMax = pmax.y;
		zMin = pmin.z;
		zMax = pmax.z;
	}
	
	public Area(BlockPosition center, int radius, int yNegOffset, int yPosOffset)
	{
		xMin = center.x - radius;
		xMax = center.x + radius;
		yMin = center.y + yNegOffset;
		yMax = center.y + yPosOffset;
		zMin = center.z - radius;
		zMax = center.z + radius;
	}
	
	public List<BlockPosition> getPositionsTopFirst()
	{
		ArrayList<BlockPosition> positions = new ArrayList<BlockPosition>();
		for(int y = yMax; y >= yMin; y--)
		{
			for(int x = xMin; x <= xMax; x++)
			{
				for(int z = zMin; z <= zMax; z++)
				{
					positions.add(new BlockPosition(x, y, z));
				}
			}
		}
		return positions;
	}
	
	public List<BlockPosition> getPositionsBottomFirst()
	{
		ArrayList<BlockPosition> positions = new ArrayList<BlockPosition>();
		for(int y = yMin; y <= yMax; y++)
		{
			for(int x = xMin; x <= xMax; x++)
			{
				for(int z = zMin; z <= zMax; z++)
				{
					positions.add(new BlockPosition(x, y, z));
				}
			}
		}
		return positions;
	}
	
	public AxisAlignedBB toAxisAlignedBB()
	{
		return AxisAlignedBB.getBoundingBox(xMin, yMin, zMin, xMax + 1, yMax + 1, zMax + 1);
	}
}
