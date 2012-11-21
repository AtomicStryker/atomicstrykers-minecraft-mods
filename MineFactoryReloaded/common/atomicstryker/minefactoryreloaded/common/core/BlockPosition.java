package atomicstryker.minefactoryreloaded.common.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactory;

public class BlockPosition
{
	public int x;
	public int y;
	public int z;
	public ForgeDirection orientation;
	
	public BlockPosition(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		orientation = ForgeDirection.UNKNOWN;
	}
	
	public BlockPosition(int x, int y, int z, ForgeDirection corientation)
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
		
		orientation = ForgeDirection.UNKNOWN;
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
		case EAST:
			x = x - step;
			break;
		case WEST:
			x = x + step;    			
			break;
		case NORTH:
			z = z + step;
			break;
		case SOUTH:
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
		case UP:
			y = y + step;
			break;
		case DOWN:
			y = y - step;
			break;
		case EAST:
			z = z + step;
			break;
		case WEST:
			z = z - step;	
			break;
		case NORTH:
			x = x + step;
			break;		
		case SOUTH:
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
		case EAST: case WEST: case NORTH: case SOUTH:
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