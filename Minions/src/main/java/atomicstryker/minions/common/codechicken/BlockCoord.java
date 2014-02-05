package atomicstryker.minions.common.codechicken;

/**
 * @author ChickenBones
 * BlockCoord class, part of ChickenCore
 * Available at: http://www.minecraftforum.net/topic/909223-125-smp-chickenbones-mods/
 */

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;

public class BlockCoord extends ChunkPosition implements Comparable<BlockCoord>
{
	public BlockCoord(int x, int y, int z)
	{
		super(x, y, z);
	}
	
	public BlockCoord(TileEntity e)
	{
		super(e.xCoord, e.yCoord, e.zCoord);
	}

	public int compareTo(BlockCoord o)
	{
		if(chunkPosX != o.chunkPosX)return chunkPosX < o.chunkPosX ? 1 : -1;
		if(chunkPosY != o.chunkPosY)return chunkPosY < o.chunkPosY ? 1 : -1;
		if(chunkPosZ != o.chunkPosZ)return chunkPosZ < o.chunkPosZ ? 1 : -1;
		return 0;
	}

	public Vector3 toVector3Centered()
	{
		return new Vector3(chunkPosX+0.5, chunkPosY+0.5, chunkPosZ+0.5);
	}
}
