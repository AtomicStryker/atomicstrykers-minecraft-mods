package atomicstryker.minefactoryreloaded.common.farmables;

import atomicstryker.minefactoryreloaded.common.api.HarvestType;
import atomicstryker.minefactoryreloaded.common.api.IFactoryHarvestable;
import net.minecraft.src.Block;
import net.minecraft.src.World;

public class HarvestableCropPlant extends HarvestableStandard implements IFactoryHarvestable
{
	public HarvestableCropPlant(int blockID)
	{
		super(blockID, HarvestType.Normal);
	}
	
	@Override
	public boolean canBeHarvested(World world, int x, int y, int z)
	{
		int blockMetadata = world.getBlockMetadata(x, y, z);
		return blockMetadata == 7;
	}
}
