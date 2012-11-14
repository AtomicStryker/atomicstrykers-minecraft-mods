package atomicstryker.minefactoryreloaded.common.farmables;

import atomicstryker.minefactoryreloaded.common.api.IFactoryFertilizable;
import net.minecraft.src.Block;
import net.minecraft.src.BlockCrops;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class FertilizableCropPlant implements IFactoryFertilizable
{
    private final int blockID;
    
    public FertilizableCropPlant(int blockID)
    {
        this.blockID = blockID;
    }
    
	@Override
	public int getFertilizableBlockId()
	{
		return blockID;
	}

	@Override
	public boolean canFertilizeBlock(World world, int x, int y, int z, ItemStack fertilizer)
	{
		return fertilizer.itemID == Item.dyePowder.shiftedIndex && fertilizer.getItemDamage() == 15 &&
			world.getBlockMetadata(x, y, z) < 7;
	}

	@Override
	public boolean fertilize(World world, int x, int y, int z, ItemStack fertilizer)
	{
		((BlockCrops)Block.crops).fertilize(world, x, y, z);
		return true;
	}
}
