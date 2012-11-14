package atomicstryker.minefactoryreloaded.common.core;

import java.util.ArrayList;
import java.util.List;


import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactory;
import buildcraft.api.core.Orientations;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class BlockPosition
{
	public int x;
	public int y;
	public int z;
	public Orientations orientation;
	
	public BlockPosition(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		orientation = Orientations.Unknown;
	}
	
	public BlockPosition(int x, int y, int z, Orientations corientation)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		orientation = corientation;
	}
	
	public BlockPosition(BlockPosition p)
	{
		x = p.x;
		y = p.y;
		z = p.z;
		orientation = p.orientation;
	}
	
	public BlockPosition(NBTTagCompound nbttagcompound)
	{
		x = nbttagcompound.getInteger("i");
		y = nbttagcompound.getInteger("j");
		z = nbttagcompound.getInteger("k");
		
		orientation = Orientations.Unknown;
	}
	
	public BlockPosition(TileEntity tile)
	{
		x = tile.xCoord;
		y = tile.yCoord;
		z = tile.zCoord;
	}
	
	public static BlockPosition fromFactoryTile(TileEntityFactory te)
	{
		BlockPosition bp = new BlockPosition(te);
		bp.orientation = te.getDirectionFacing();
		return bp;
	}
	
	public void moveRight(int step)
	{
		switch(orientation)
		{
		case ZPos:
			x = x - step;
			break;
		case ZNeg:
			x = x + step;    			
			break;
		case XPos:
			z = z + step;
			break;
		case XNeg:
			z = z - step;
			break;
		}
	}
	
	public void moveLeft(int step)
	{
		moveRight(-step);
	}
	
	public void moveForwards(int step)
	{
		switch(orientation)
		{
		case YPos:
			y = y + step;
			break;
		case YNeg:
			y = y - step;
			break;
		case ZPos:
			z = z + step;
			break;
		case ZNeg:
			z = z - step;	
			break;
		case XPos:
			x = x + step;
			break;		
		case XNeg:
			x = x - step;
			break;
		}
	}	
	
	public void moveBackwards(int step)
	{
		moveForwards(-step);
	}
	
	public void moveUp(int step)
	{
		switch(orientation)
		{
		case ZPos: case ZNeg: case XPos: case XNeg:
			y = y + step;
			break;
		}
		
	}
	
	public void moveDown(int step)
	{
		moveUp(-step);
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setDouble("i", x);
		nbttagcompound.setDouble("j", y);
		nbttagcompound.setDouble("k", z);
	}
	
	public String toString ()
	{
		return "{" + x + ", " + y + ", " + z + "}";
	}
	
	public BlockPosition min(BlockPosition p)
	{
		return new BlockPosition(p.x > x ? x : p.x, p.y > y ? y : p.y, p.z > z ? z : p.z);
	}
	
	public BlockPosition max (BlockPosition p)
	{
		return new BlockPosition(p.x < x ? x : p.x, p.y < y ? y : p.y, p.z < z ? z : p.z);
	}
	
	public List<BlockPosition> getAdjacent(boolean includeVertical)
	{
		List<BlockPosition> a = new ArrayList<BlockPosition>();
		a.add(new BlockPosition(x + 1, y, z));
		a.add(new BlockPosition(x - 1, y, z));
		a.add(new BlockPosition(x, y, z + 1));
		a.add(new BlockPosition(x, y, z - 1));
		if(includeVertical)
		{
			a.add(new BlockPosition(x, y + 1, z));
			a.add(new BlockPosition(x, y - 1, z));
		}
		return a;
	}
}