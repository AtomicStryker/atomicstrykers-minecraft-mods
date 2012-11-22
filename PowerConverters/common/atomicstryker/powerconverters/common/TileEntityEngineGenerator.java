package atomicstryker.powerconverters.common;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySource;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.power.PowerProvider;

public class TileEntityEngineGenerator extends TileEntityPowerConverter implements IPowerReceptor, IEnergySource
{
	private IPowerProvider powerProvider;
	private int storedPower;
	private int maxStoredPower;
	private int pulseSize;
	
	public TileEntityEngineGenerator()
	{
		setPowerProvider(PowerFramework.currentFramework.createPowerProvider());
		this.pulseSize = 30;
		this.maxStoredPower = 100;
		setupPowerProvider();
	}
	
	public TileEntityEngineGenerator(int pulseSize, int maxStoredPower)
	{
		setPowerProvider(PowerFramework.currentFramework.createPowerProvider());
		this.pulseSize = pulseSize;
		this.maxStoredPower = maxStoredPower;
		setupPowerProvider();
	}
	
	private void setupPowerProvider()
	{
		getPowerProvider().configure(0, this.pulseSize, this.pulseSize * 5, 25, this.maxStoredPower);
	}
	
	// Base methods
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			return;
		}
		getPowerProvider().update(this);
		if(!isAddedToEnergyNet())
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			isAddedToEnergyNet = true;
		}
		
		int used = pulseSize * PowerConverterCore.bcToICScaleDenominator / PowerConverterCore.bcToICScaleNumerator;
		
		if(used <= storedPower)
		{
			int output = pulseSize;
			storedPower -= used;
			int powerNotTransmitted = EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, output);
			int powerReturned = (powerNotTransmitted * PowerConverterCore.bcToICScaleDenominator / PowerConverterCore.bcToICScaleNumerator);
			storedPower = Math.min(storedPower + powerReturned, maxStoredPower);
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
		super.readFromNBT(nbttagcompound);
		storedPower = nbttagcompound.getInteger("storedPower");
		maxStoredPower = nbttagcompound.getInteger("maxStoredPower");
		pulseSize = nbttagcompound.getInteger("pulseSize");
		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);
		setupPowerProvider();
    }
	
	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("storedPower", storedPower);
		nbttagcompound.setInteger("maxStoredPower", maxStoredPower);
		nbttagcompound.setInteger("pulseSize", pulseSize);
		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);
    }
	
	// IPowerReceptor methods
	
	@Override
	public void setPowerProvider(IPowerProvider powerprovider)
	{
		this.powerProvider = powerprovider;
	}

	@Override
	public IPowerProvider getPowerProvider()
	{
		return powerProvider;
	}

	@Override
	public void doWork()
	{
		if(storedPower < maxStoredPower)
		{
			int energy = Math.round(powerProvider.useEnergy(1, maxStoredPower - storedPower, true));
			storedPower += energy;
		}
	}

	@Override
	public int powerRequest()
	{
		return getPowerProvider().getMaxEnergyReceived();
	}

	// IEnergySource methods
	
	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return true;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return storedPower * PowerConverterCore.bcToICScaleNumerator / PowerConverterCore.bcToICScaleDenominator;
	}
}
