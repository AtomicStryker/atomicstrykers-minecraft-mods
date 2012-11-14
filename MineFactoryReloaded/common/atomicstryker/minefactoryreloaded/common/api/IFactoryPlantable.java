package atomicstryker.minefactoryreloaded.common.api;

import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public interface IFactoryPlantable
{
	public int getSourceId();
	
	public int getPlantedBlockId(World world, int x, int y, int z, ItemStack stack);
	public int getPlantedBlockMetadata(World world, int x, int y, int z, ItemStack stack);
	
	public boolean canBePlantedHere(World world, int x, int y, int z, ItemStack stack);
	
	public void prePlant(World world, int x, int y, int z, ItemStack stack);
	public void postPlant(World world, int x, int y, int z);
}
