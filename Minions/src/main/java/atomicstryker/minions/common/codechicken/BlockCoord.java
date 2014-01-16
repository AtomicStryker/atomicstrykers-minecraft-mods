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
		super(e.field_145851_c, e.field_145848_d, e.field_145849_e);
	}

	public int compareTo(BlockCoord o)
	{
		if(field_151329_a != o.field_151329_a)return field_151329_a < o.field_151329_a ? 1 : -1;
		if(field_151327_b != o.field_151327_b)return field_151327_b < o.field_151327_b ? 1 : -1;
		if(field_151328_c != o.field_151328_c)return field_151328_c < o.field_151328_c ? 1 : -1;
		return 0;
	}

	public Vector3 toVector3Centered()
	{
		return new Vector3(field_151329_a+0.5, field_151327_b+0.5, field_151328_c+0.5);
	}
}
