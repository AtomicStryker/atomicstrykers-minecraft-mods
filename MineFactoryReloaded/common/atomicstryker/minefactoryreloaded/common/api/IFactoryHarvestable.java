package atomicstryker.minefactoryreloaded.common.api;

import java.util.List;

import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public interface IFactoryHarvestable
{
	public int getSourceId();
	public HarvestType getHarvestType();

	public boolean canBeHarvested(World world, int x, int y, int z);
	
	public boolean hasDifferentDrops();
	public List<ItemStack> getDifferentDrops(World world, int x, int y, int z);
	
	public void preHarvest(World world, int x, int y, int z);
	public void postHarvest(World world, int x, int y, int z);
}
