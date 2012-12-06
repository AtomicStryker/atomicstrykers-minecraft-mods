package atomicstryker.powerconverters.common;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySink;
import ic2.api.IEnergyTile;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;

public class TileEntityEnergyLink extends TileEntityPowerConverter implements IEnergySink
{
	private int storedEnergy = 0;
	private int maxStoredEnergy = 2500;
	
	public boolean isConnected(int side)
	{
		BlockPosition p = new BlockPosition(this);
		p.orientation = PowerConverterCore.getOrientationFromSide(side);
		p.moveForwards(1);
		TileEntity te = worldObj.getBlockTileEntity(p.x, p.y, p.z);
		if(te != null && (te instanceof IPowerReceptor && ((IPowerReceptor)te).getPowerProvider() != null) || (te instanceof IEnergyTile))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			return;
		}
		if(!isAddedToEnergyNet())
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			isAddedToEnergyNet = true;
		}
		if(isRedstonePowered())
		{
			return;
		}
		int bcEnergyStored = storedEnergy * PowerConverterCore.icToBCScaleNumerator / PowerConverterCore.icToBCScaleDenominator;
		BlockPosition ourbp = new BlockPosition(this);
		for(int i = 0; i < 6; i++)
		{
			ForgeDirection o = ForgeDirection.values()[i];
			BlockPosition bp = new BlockPosition(ourbp);
			bp.orientation = o;
			bp.moveForwards(1);
			TileEntity te = worldObj.getBlockTileEntity(bp.x, bp.y, bp.z);
			if(te != null && te instanceof IPowerReceptor)
			{
				IPowerProvider pp = ((IPowerReceptor)te).getPowerProvider();
				if(pp != null && pp.preConditions((IPowerReceptor)te) && pp.getMinEnergyReceived() <= bcEnergyStored)
				{
					int energyUsed = Math.min(Math.min(pp.getMaxEnergyReceived(), bcEnergyStored), pp.getMaxEnergyStored() - (int)Math.floor(pp.getEnergyStored()));
					pp.receiveEnergy(energyUsed, o);
					storedEnergy -= energyUsed * PowerConverterCore.icToBCScaleDenominator / PowerConverterCore.icToBCScaleNumerator;
				}
			}
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
		super.readFromNBT(nbttagcompound);
		storedEnergy = nbttagcompound.getInteger("storedEnergy");
    }
	
	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("storedEnergy", storedEnergy);
    }
	
	// IEnergySink methods
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return true;
	}

	@Override
	public boolean demandsEnergy()
	{
		return storedEnergy < maxStoredEnergy;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount)
	{
		int amountToInject = Math.min(amount, maxStoredEnergy - storedEnergy);
		if (amountToInject == 0)
		{
		    storedEnergy += amountToInject; // IC2 API demands we accept an overflow
		    return 0;
		}
		
		storedEnergy += amountToInject;
		return amount - amountToInject;
	}

	/* IMachine methods
	@Override
	public boolean isActive()
	{
		return false;
	}

	@Override
	public boolean manageLiquids()
	{
		return false;
	}

	@Override
	public boolean manageSolids()
	{
		return false;
	}

	@Override
	public boolean allowActions()
	{
		return false;
	}
    */
}
