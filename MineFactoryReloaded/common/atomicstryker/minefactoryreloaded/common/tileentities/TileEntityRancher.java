package atomicstryker.minefactoryreloaded.common.tileentities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.api.IFactoryRanchable;
import atomicstryker.minefactoryreloaded.common.core.Util;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;

public class TileEntityRancher extends TileEntityFactoryInventory implements ITankContainer
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
	
	@Override
	protected boolean canDropInPipeAt(Orientations o)
	{
		return o == getDirectionFacing().reverse();
	}

	@Override
	public void doWork()
	{
		if(!powerAvailable())
		{
			return;
		}
		
		float dropOffsetX = 0.0F;
		float dropOffsetZ = 0.0F;
		
		if(getDirectionFacing() == Orientations.XPos)
		{
			dropOffsetX = -0.5F;
			dropOffsetZ = 0.5F;
		}
		else if(getDirectionFacing() == Orientations.ZPos)
		{
			dropOffsetX = 0.5F;
			dropOffsetZ = -0.5F;
		}
		else if(getDirectionFacing() == Orientations.XNeg)
		{
			dropOffsetX = 1.5F;
			dropOffsetZ = 0.5F;
		}
		else if(getDirectionFacing() == Orientations.ZNeg)
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
					if(LiquidManager.getFilledItemForLiquid(new LiquidStack(s.itemID, LiquidManager.BUCKET_VOLUME)) != null)
					{
						produceLiquid(s.itemID);
						continue; // abort if we got a liquid block/item - nowhere to put it, it'll just be destroyed
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

    @Override
    public int fill(Orientations from, LiquidStack resource, boolean doFill)
    {
        return 0;
    }

    @Override
    public int fill(int tankIndex, LiquidStack resource, boolean doFill)
    {
        return 0;
    }

    @Override
    public LiquidStack drain(Orientations from, int maxDrain, boolean doDrain)
    {
        return null;
    }

    @Override
    public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
    {
        return null;
    }

    @Override
    public ILiquidTank[] getTanks()
    {
        return null;
    }
    
}
