package atomicstryker.minefactoryreloaded.common.tileentities;

import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore.PowerSystem;
import atomicstryker.minefactoryreloaded.common.core.Util;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;

public abstract class TileEntityFactoryPowered extends TileEntityFactory implements IPowerReceptor
{
	private boolean lastRedstonePowerState = false;
	private boolean redstonePowerAvailable = false;
	
	private IPowerProvider powerProvider;
	private int powerNeeded;
	
	protected TileEntityFactoryPowered(int bcEnergyNeededToWork, int bcEnergyNeededToActivate)
	{
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerNeeded = bcEnergyNeededToWork;
			powerProvider.configure(25, powerNeeded, powerNeeded, bcEnergyNeededToActivate, powerNeeded);
		}
	}
	
	public void neighborBlockChanged()
	{
		boolean isPowered = Util.isRedstonePowered(this);
		if(isPowered && !lastRedstonePowerState && MineFactoryReloadedCore.powerSystem == MineFactoryReloadedCore.PowerSystem.Redstone)
		{
			lastRedstonePowerState = isPowered;
			redstonePowerAvailable = true;
			doWork();
			redstonePowerAvailable = false;
		}
		else
		{
			lastRedstonePowerState = isPowered;
		}
	}
	
	protected boolean powerAvailable()
	{
		if(MineFactoryReloadedCore.powerSystem == PowerSystem.Redstone)
		{
			return redstonePowerAvailable;
		}
		else if(MineFactoryReloadedCore.powerSystem == PowerSystem.BuildCraft)
		{
			if(powerProvider.useEnergy(powerNeeded, powerNeeded, false) >= powerNeeded)
			{
				powerProvider.useEnergy(powerNeeded, powerNeeded, true);
				return true;
			}
			return false;
		}
		return false;
	}
	
	// base methods
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if(MineFactoryReloadedCore.powerSystem == PowerSystem.BuildCraft)
		{
			getPowerProvider().update(this);
		}
	}
	
	// IPowerReceptor methods

	@Override
	public void setPowerProvider(IPowerProvider provider)
	{
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider()
	{
		return powerProvider;
	}

	@Override
	public int powerRequest()
	{
		return powerNeeded;
	}
}
