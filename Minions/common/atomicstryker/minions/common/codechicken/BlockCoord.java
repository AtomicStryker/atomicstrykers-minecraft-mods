package atomicstryker.minions.common.codechicken;

/**
 * @author ChickenBones
 * BlockCoord class, part of ChickenCore
 * Available at: http://www.minecraftforum.net/topic/909223-125-smp-chickenbones-mods/
 */

import net.minecraft.src.ChunkPosition;
import net.minecraft.src.TileEntity;

public class BlockCoord extends ChunkPosition implements Comparable<BlockCoord>
{
	public BlockCoord(int x, int y, int z)
	{
		super(x, y, z);
	}
	
	public BlockCoord(TileEntity tile)
	{
		super(tile.xCoord, tile.yCoord, tile.zCoord);
	}

	public int compareTo(BlockCoord o)
	{
		if(x != o.x)return x < o.x ? 1 : -1;
		if(y != o.y)return y < o.y ? 1 : -1;
		if(z != o.z)return z < o.z ? 1 : -1;
		return 0;
	}

	public Vector3 toVector3Centered()
	{
		return new Vector3(x+0.5, y+0.5, z+0.5);
	}
}
