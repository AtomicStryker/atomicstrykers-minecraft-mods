package atomicstryker.ropesplus.common;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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
			if ((world.getBlockState(new BlockPos(ix, iy - 1, iz)).getBlock() == Blocks.air || world.getBlockState(new BlockPos(ix, iy - 1, iz)).getBlock() == Blocks.snow))
			{
				remainrope -= 1;
				if (remainrope <= 0)
				{
					return true;
				}
				
				world.setBlockState(new BlockPos(ix,  iy - 1,  iz),  world.getBlockState(new BlockPos(ix, iy, iz)));
				TileEntityRope newent = new TileEntityRope(world, ix, iy - 1, iz, remainrope);
				RopesPlusCore.instance.addRopeToArray(newent);
			}
			return true;
		}
		
		return false;
	}
}
