package atomicstryker.ropesplus.common;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BlockRopePseudoEnt
{
	private int delay;
	private World world;
	private int ix;
	private int iy;
	private int iz;
	private int remainrope;

    public BlockRopePseudoEnt(World w, int x, int y, int z, int l)
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
				
				world.setBlockWithNotify(ix, iy - 1, iz, RopesPlusCore.blockRopeCentralPos.blockID);
				BlockRopePseudoEnt newent = new BlockRopePseudoEnt(world, ix, iy - 1, iz, remainrope);
				RopesPlusCore.addRopeToArray(newent);
				
				int[] coords = new int[3];
				coords[0] = ix;
				coords[1] = iy - 1;
				coords[2] = iz;
				RopesPlusCore.addCoordsToRopeArray(coords);
			}
			return true;
		}
		
		return false;
	}
}
