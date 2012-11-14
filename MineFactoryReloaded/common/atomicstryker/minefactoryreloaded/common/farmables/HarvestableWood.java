package atomicstryker.minefactoryreloaded.common.farmables;

import atomicstryker.minefactoryreloaded.common.api.HarvestType;
import atomicstryker.minefactoryreloaded.common.api.IFactoryHarvestable;
import net.minecraft.src.Block;
import net.minecraft.src.BlockLog;
import net.minecraft.src.World;

public class HarvestableWood extends HarvestableStandard implements IFactoryHarvestable
{
	public HarvestableWood()
	{
		super(Block.wood.blockID, HarvestType.Tree);
	}
	
	@Override
	public boolean canBeHarvested(World world, int x, int y, int z)
	{
		return BlockLog.limitToValidMetadata(world.getBlockMetadata(x, y, z)) != 3;
	}
}
