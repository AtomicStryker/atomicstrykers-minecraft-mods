package atomicstryker.minefactoryreloaded.common.farmables;

import java.util.ArrayList;
import java.util.List;

import atomicstryker.minefactoryreloaded.common.api.HarvestType;
import atomicstryker.minefactoryreloaded.common.api.IFactoryHarvestable;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class HarvestableCocoa extends HarvestableStandard implements IFactoryHarvestable
{
	public HarvestableCocoa()
	{
		super(Block.cocoaPlant.blockID, HarvestType.Normal);
	}
	
	@Override
	public boolean canBeHarvested(World world, int x, int y, int z)
	{
		int blockMetadata = world.getBlockMetadata(x, y, z);
		return ((blockMetadata & 12) >> 2) >= 2;
	}
	
	@Override
	public boolean hasDifferentDrops()
	{
	    return true;
	}

	@Override
	public List<ItemStack> getDifferentDrops(World world, int x, int y, int z)
	{
	    ArrayList<ItemStack> result = new ArrayList<ItemStack>();
	    result.add(new ItemStack(Item.dyePowder, 3, 3));
	    return result;
	}
}
