package atomicstryker.ropesplus.common;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class TileEntityRope
{
	private int delay;
	private World world;
	private int ix;
	private int iy;
	private int iz;
	private int remainrope;
	
    public TileEntityRope(World w, int x, int y, int z, int l)
    {
		world = w;
		ix = x;
		iy = y;
		iz = z;
		delay = 20;
		remainrope = l;
    }
	
	public boolean OnUpdate()
	{
		if (delay < 0) return true;
		delay--;
		
		if (delay == 0)
		{
			if ((world.getBlockId(ix, iy - 1, iz) == 0 || world.getBlockId(ix, iy - 1, iz) == Block.snow.blockID))
			{
				remainrope -= 1;
				if (remainrope <= 0)
				{
					return true;
				}
				
				world.setBlockWithNotify(ix, iy - 1, iz, RopesPlusCore.blockRopeWallPos.blockID);
				world.setBlockMetadataWithNotify(ix, iy - 1, iz, world.getBlockMetadata(ix, iy, iz));
				TileEntityRope newent = new TileEntityRope(world, ix, iy - 1, iz, remainrope);
				RopesPlusCore.addRopeToArray(newent);
			}
			return true;
		}
		
		return false;
	}
}
