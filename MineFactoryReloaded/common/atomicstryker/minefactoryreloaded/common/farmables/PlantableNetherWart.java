package atomicstryker.minefactoryreloaded.common.farmables;

import atomicstryker.minefactoryreloaded.common.api.IFactoryPlantable;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class PlantableNetherWart implements IFactoryPlantable
{
	@Override
	public int getSourceId()
	{
		return Item.netherStalkSeeds.shiftedIndex;
	}

	@Override
	public int getPlantedBlockId(World world, int x, int y, int z, ItemStack stack)
	{
		return Block.netherStalk.blockID;
	}

	@Override
	public int getPlantedBlockMetadata(World world, int x, int y, int z, ItemStack stack)
	{
		return 0;
	}

	@Override
	public boolean canBePlantedHere(World world, int x, int y, int z, ItemStack stack)
	{
		return world.getBlockId(x, y - 1, z) == Block.slowSand.blockID && world.isAirBlock(x, y, z);
	}

	@Override
	public void prePlant(World world, int x, int y, int z, ItemStack stack)
	{
	}

	@Override
	public void postPlant(World world, int x, int y, int z)
	{
	}

}
