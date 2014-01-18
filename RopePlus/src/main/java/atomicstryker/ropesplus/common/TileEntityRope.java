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
			if ((world.func_147439_a(ix, iy - 1, iz) == Blocks.air || world.func_147439_a(ix, iy - 1, iz) == Blocks.snow))
			{
				remainrope -= 1;
				if (remainrope <= 0)
				{
					return true;
				}
				
				world.func_147465_d(ix, iy - 1, iz, RopesPlusCore.blockRopeWallPos, world.getBlockMetadata(ix, iy, iz), 3);
				TileEntityRope newent = new TileEntityRope(world, ix, iy - 1, iz, remainrope);
				RopesPlusCore.addRopeToArray(newent);
			}
			return true;
		}
		
		return false;
	}
}
