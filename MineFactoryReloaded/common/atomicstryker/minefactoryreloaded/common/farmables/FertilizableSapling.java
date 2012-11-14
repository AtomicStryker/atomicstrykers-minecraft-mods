package atomicstryker.minefactoryreloaded.common.farmables;

import atomicstryker.minefactoryreloaded.common.api.IFactoryFertilizable;
import net.minecraft.src.Block;
import net.minecraft.src.BlockSapling;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class FertilizableSapling implements IFactoryFertilizable
{	
	@Override
	public int getFertilizableBlockId()
	{
		return Block.sapling.blockID;
	}

	@Override
	public boolean canFertilizeBlock(World world, int x, int y, int z, ItemStack fertilizer)
	{
		return fertilizer.itemID == Item.dyePowder.shiftedIndex && fertilizer.getItemDamage() == 15;
	}

	@Override
	public boolean fertilize(World world, int x, int y, int z, ItemStack fertilizer)
	{
		((BlockSapling)Block.sapling).growTree(world, x, y, z, world.rand);
        return world.getBlockId(x, y, z) != Block.sapling.blockID;
	}
}
