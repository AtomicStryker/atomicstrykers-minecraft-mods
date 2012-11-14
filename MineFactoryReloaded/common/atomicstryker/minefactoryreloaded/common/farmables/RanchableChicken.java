package atomicstryker.minefactoryreloaded.common.farmables;

import java.util.LinkedList;
import java.util.List;

import atomicstryker.minefactoryreloaded.common.api.IFactoryRanchable;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityRancher;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class RanchableChicken implements IFactoryRanchable
{
	@Override
	public Class<?> getRanchableEntity()
	{
		return net.minecraft.src.EntityChicken.class;
	}

	@Override
	public List<ItemStack> ranch(World world, EntityLiving entity, TileEntityRancher rancher)
	{
		List<ItemStack> drops = new LinkedList<ItemStack>();
		if(world.rand.nextInt(100) < 40)
		{
			drops.add(new ItemStack(Item.feather));
		}
		if(world.rand.nextInt(100) < 30)
		{
			drops.add(new ItemStack(Item.egg));
		}
		if(world.rand.nextInt(100) < 20)
		{
			drops.add(new ItemStack(Item.chickenRaw));
		}
		return drops;
	}

	@Override
	public boolean getDamageRanchedEntity(World world, EntityLiving entity, List<ItemStack> drops)
	{
		return world.rand.nextInt(100) < 45;
	}

	@Override
	public int getDamageAmount(World world, EntityLiving entity, List<ItemStack> drops)
	{
		return 1;
	}

}
