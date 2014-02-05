package atomicstryker.ropesplus.common;

import net.minecraft.init.Blocks;
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
			if ((world.getBlock(ix, iy - 1, iz) == Blocks.air || world.getBlock(ix, iy - 1, iz) == Blocks.snow))
			{
				remainrope -= 1;
				if (remainrope <= 0)
				{
					return true;
				}
				
				world.setBlock(ix, iy - 1, iz, RopesPlusCore.instance.blockRopeWall, world.getBlockMetadata(ix, iy, iz), 3);
				TileEntityRope newent = new TileEntityRope(world, ix, iy - 1, iz, remainrope);
				RopesPlusCore.instance.addRopeToArray(newent);
			}
			return true;
		}
		
		return false;
	}
}
