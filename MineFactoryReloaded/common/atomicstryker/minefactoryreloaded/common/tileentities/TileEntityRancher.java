package atomicstryker.minefactoryreloaded.common.tileentities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.api.IFactoryRanchable;
import atomicstryker.minefactoryreloaded.common.core.Util;

public class TileEntityRancher extends TileEntityFactoryInventory
{
	private static Map<Class<?>, IFactoryRanchable> ranchables = new HashMap<Class<?>, IFactoryRanchable>();

	public static void registerRanchable(IFactoryRanchable ranchable)
	{
		ranchables.put(ranchable.getRanchableEntity(), ranchable);
	}
	
	public TileEntityRancher()
	{
		super(25, 25);
	}

	@Override
	public String getInvName()
	{
		return "Rancher";
	}
	
	@Override
	protected int getHarvestRadius()
	{
		return 2;
	}
	
	@Override
	protected int getHarvestDistanceDown()
	{
		return 1;
	}
	
	@Override
	protected int getHarvestDistanceUp()
	{
		return 3;
	}
	
	/*
	@Override
	protected boolean canDropInPipeAt(ForgeDirection o)
	{
		return o == getDirectionFacing().reverse();
	}
	*/

	@Override
	public void doWork()
	{
		if(!powerAvailable())
		{
			return;
		}
		
		float dropOffsetX = 0.0F;
		float dropOffsetZ = 0.0F;
		
		if(getDirectionFacing() == ForgeDirection.NORTH)
		{
			dropOffsetX = -0.5F;
			dropOffsetZ = 0.5F;
		}
		else if(getDirectionFacing() == ForgeDirection.EAST)
		{
			dropOffsetX = 0.5F;
			dropOffsetZ = -0.5F;
		}
		else if(getDirectionFacing() == ForgeDirection.SOUTH)
		{
			dropOffsetX = 1.5F;
			dropOffsetZ = 0.5F;
		}
		else if(getDirectionFacing() == ForgeDirection.WEST)
		{
			dropOffsetX = 0.5F;
			dropOffsetZ = 1.5F;
		}
		
		List<?> entities = worldObj.getEntitiesWithinAABB(net.minecraft.src.EntityLiving.class, getHarvestArea().toAxisAlignedBB());
		
		for(Object o : entities)
		{
			if(!(o instanceof EntityLiving))
			{
				continue;
			}
			EntityLiving e = (EntityLiving)o;
			if(ranchables.containsKey(e.getClass()))
			{
				IFactoryRanchable r = ranchables.get(e.getClass());
				List<ItemStack> drops = r.ranch(worldObj, e, this);
				for(ItemStack s : drops)
				{
				    LiquidStack produced = LiquidContainerRegistry.getLiquidForFilledItem(s);
					if(produced != null)
					{
						produceLiquid(produced); // the 'milk Item' doesnt exist as anything but liquid, so no need to check this
						continue;
					}
					dropStack(s, dropOffsetX, 0, dropOffsetZ);
				}
				
				if(Util.getBool(MineFactoryReloadedCore.rancherInjuresAnimals) && r.getDamageRanchedEntity(worldObj, e, drops))
				{
					e.attackEntityFrom(DamageSource.generic, r.getDamageAmount(worldObj, e, drops));
				}
			}
		}
	}
    
}
