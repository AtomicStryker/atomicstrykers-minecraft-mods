package atomicstryker.minefactoryreloaded.common.farmables;

import java.util.LinkedList;
import java.util.List;

import atomicstryker.minefactoryreloaded.common.api.IFactoryRanchable;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityRancher;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class RanchableStandard implements IFactoryRanchable
{
	private Class<?> ranchableClass;
	private ItemStack dropStack;
	private int damageChance;
	private int entityDamage;
	private int dropChance;
	
	public RanchableStandard(Class<?> entityToRanch, ItemStack dropStack, int percentChanceToDamageEntity, int entityDamage,
			int percentChanceOfDrop)
	{
		ranchableClass = entityToRanch;
		this.dropStack = dropStack;
		damageChance = percentChanceToDamageEntity;
		this.entityDamage = entityDamage;
		dropChance = percentChanceOfDrop;
	}
	
	@Override
	public Class<?> getRanchableEntity()
	{
		return ranchableClass;
	}

	@Override
	public List<ItemStack> ranch(World world, EntityLiving entity, TileEntityRancher rancher)
	{
		List<ItemStack> drops = new LinkedList<ItemStack>();
		if(world.rand.nextInt(100) < dropChance)
		{
			drops.add(dropStack.copy());
		}
		return drops;
	}

	@Override
	public boolean getDamageRanchedEntity(World world, EntityLiving entity, List<ItemStack> drops)
	{
		return world.rand.nextInt(100) < damageChance;
	}

	@Override
	public int getDamageAmount(World world, EntityLiving entity, List<ItemStack> drops)
	{
		return entityDamage;
	}

}
