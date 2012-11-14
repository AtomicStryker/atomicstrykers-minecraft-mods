package atomicstryker.minefactoryreloaded.common.farmables;

import java.util.ArrayList;
import java.util.List;

import atomicstryker.minefactoryreloaded.common.api.HarvestType;
import atomicstryker.minefactoryreloaded.common.api.IFactoryHarvestable;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class HarvestableNetherWart implements IFactoryHarvestable
{
	@Override
	public int getSourceId()
	{
		return Block.netherStalk.blockID;
	}

	@Override
	public HarvestType getHarvestType()
	{
		return HarvestType.Normal;
	}

	@Override
	public boolean canBeHarvested(World world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z) >= 3;
	}

	@Override
	public boolean hasDifferentDrops()
	{
		return true;
	}

	@Override
	public List<ItemStack> getDifferentDrops(World world, int x, int y, int z)
	{
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        int numDrops = 1;
        if(world.getBlockMetadata(x, y, z) >= 3)
        {
            numDrops = 2 + world.rand.nextInt(3);
        }
        for(int k1 = 0; k1 < numDrops; k1++)
        {
           drops.add(new ItemStack(Item.netherStalkSeeds));
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
