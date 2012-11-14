package atomicstryker.minefactoryreloaded.common.farmables;

import java.util.LinkedList;
import java.util.List;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.api.IFactoryRanchable;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityRancher;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class RanchableCow implements IFactoryRanchable
{
	@Override
	public Class<?> getRanchableEntity()
	{
		return net.minecraft.src.EntityCow.class;
	}

	@Override
	public List<ItemStack> ranch(World world, EntityLiving entity, TileEntityRancher rancher)
	{
		List<ItemStack> drops = new LinkedList<ItemStack>();
		if(world.rand.nextInt(100) < 40)
		{
			drops.add(new ItemStack(Item.leather));
		}
		if(world.rand.nextInt(100) < 20)
		{
			drops.add(new ItemStack(Item.beefRaw));
		}
		if(world.rand.nextInt(100) < 30)
		{
			int bucketIndex = rancher.findFirstStack(Item.bucketEmpty.shiftedIndex, 0);
			if(bucketIndex >= 0)
			{
				drops.add(new ItemStack(Item.bucketMilk));
				rancher.setInventorySlotContents(bucketIndex, null);
			}
			else
			{
				drops.add(new ItemStack(MineFactoryReloadedCore.milkItem));
			}
		}
		
		return drops;
	}

	@Override
	public boolean getDamageRanchedEntity(World world, EntityLiving entity, List<ItemStack> drops)
	{
		return world.rand.nextInt(100) < 35;
	}

	@Override
	public int getDamageAmount(World world, EntityLiving entity, List<ItemStack> drops)
	{
		return 2;
	}
}
