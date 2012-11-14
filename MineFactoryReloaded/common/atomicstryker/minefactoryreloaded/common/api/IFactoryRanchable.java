package atomicstryker.minefactoryreloaded.common.api;

import java.util.List;

import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityRancher;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public interface IFactoryRanchable
{
	public Class<?> getRanchableEntity();
	
	public List<ItemStack> ranch(World world, EntityLiving entity, TileEntityRancher rancher);
	public boolean getDamageRanchedEntity(World world, EntityLiving entity, List<ItemStack> drops);
	
	public int getDamageAmount(World world, EntityLiving entity, List<ItemStack> drops);
}
