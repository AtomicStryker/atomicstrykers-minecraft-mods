package atomicstryker.minefactoryreloaded.common.farmables;

import java.util.LinkedList;
import java.util.List;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.api.IFactoryRanchable;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityRancher;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMooshroom;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class RanchableMooshroom implements IFactoryRanchable {

	@Override
	public Class<?> getRanchableEntity()
	{
		return EntityMooshroom.class;
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
		if(world.rand.nextInt(100) < 30)
		{
			int bowlIndex = rancher.findFirstStack(Item.bowlEmpty.shiftedIndex, 0);
			if(bowlIndex >= 0)
			{
				drops.add(new ItemStack(Item.bowlSoup));
				rancher.decrStackSize(bowlIndex, 1);
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
