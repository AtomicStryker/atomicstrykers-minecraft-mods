package atomicstryker.minefactoryreloaded.common.farmables;

import java.util.ArrayList;
import java.util.List;

import atomicstryker.minefactoryreloaded.common.api.HarvestType;
import atomicstryker.minefactoryreloaded.common.api.IFactoryHarvestable;

import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class HarvestableVine implements IFactoryHarvestable
{
	@Override
	public int getSourceId()
	{
		return Block.vine.blockID;
	}

	@Override
	public HarvestType getHarvestType()
	{
		return HarvestType.TreeLeaf;
	}

	@Override
	public boolean canBeHarvested(World world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public boolean hasDifferentDrops()
	{
		return true;
	}

	@Override
	public List<ItemStack> getDifferentDrops(World world, int x, int y, int z)
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		int yOffset = 0;
		while(world.getBlockId(x, y + yOffset, z) == Block.vine.blockID)
		{
			drops.add(new ItemStack(Block.vine));
			yOffset--;
		}
		return drops;
	}

	@Override
	public void preHarvest(World world, int x, int y, int z)
	{
	}

	@Override
	public void postHarvest(World world, int x, int y, int z)
	{
	}
}
