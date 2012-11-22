package atomicstryker.powerconverters.common;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySource;
import ic2.api.IEnergyTile;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import buildcraft.api.core.BuildCraftAPI;

public abstract class TileEntityLiquidGenerator extends TileEntityPowerConverter implements IEnergySource, ITankContainer
{
	protected int liquidId;
	protected int liquidStored = 0;
	protected int liquidStoredMax;
	protected int liquidConsumedPerAction;
	protected boolean wasActive = false;
	protected int euStored = 0;
	protected int euStoredMax;
	protected int euPulseSize;
	protected int euPerLiquidConsumptionAction;
	
	protected TileEntityLiquidGenerator(int liquidId, int liquidConsumedPerAction, int euPerLiquidConsumptionAction, int euPulseSize)
	{
		this.liquidId = liquidId;
		this.liquidConsumedPerAction = liquidConsumedPerAction;
		this.euPerLiquidConsumptionAction = euPerLiquidConsumptionAction;
		this.euPulseSize = euPulseSize;
		this.liquidStoredMax = LiquidContainerRegistry.BUCKET_VOLUME * 5;
		this.euStoredMax = euPulseSize * 2;
	}
	
	public boolean isActive()
	{
		return liquidStored > 0;
	}
	
	public boolean isConnected(int side)
	{
		BlockPosition p = new BlockPosition(this);
		p.orientation = PowerConverterCore.getOrientationFromSide(side);
		p.moveForwards(1);
		TileEntity te = worldObj.getBlockTileEntity(p.x, p.y, p.z);
		return (te != null && (te instanceof ITankContainer || te instanceof IEnergyTile));
	}
	
	// client, for network sync
	public void setStoredLiquid(int quantity)
	{
		liquidStored = quantity;
	}
	
	// Base methods
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		// bail if client
        if(worldObj.isRemote)
		{
			return;
		}
		// reset energy network
		if(!isAddedToEnergyNet())
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			isAddedToEnergyNet = true;
		}
		// update active state
		if(wasActive != isActive())
		{
			worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
			wasActive = isActive();
			PowerConverterCore.sendTEStoredLiquidPacket(this, liquidStored);
		}
		// consume liquid
		while(liquidStored >= liquidConsumedPerAction && euStored < euPulseSize)
		{
			liquidStored -= liquidConsumedPerAction;
			euStored += euPerLiquidConsumptionAction;
		}
		// send power if we have it
		if(euStored >= euPulseSize)
		{
			int powerNotTransmitted = EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, euPulseSize);
			euStored -= (euPulseSize - powerNotTransmitted);
		}
		euStored = Math.min(euStored, euStoredMax);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
		super.readFromNBT(nbttagcompound);
		liquidStored = nbttagcompound.getInteger("liquidStored");
		euStored = nbttagcompound.getInteger("euStored");
    }
	
	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("liquidStored", liquidStored);
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
		return Math.min(liquidStored, liquidConsumedPerAction) * euPerLiquidConsumptionAction;
	}
	
	// ILiquidContainer methods
	
	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
        if(resource.itemID != liquidId)
        {
            return 0;
        }
        int amountToFill = Math.min(resource.amount, liquidStoredMax - liquidStored);
        if(doFill)
        {
            liquidStored += amountToFill;
        }
        return amountToFill;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
	    return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
	    return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
	    return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction)
	{
	    return new LiquidTank [] { new LiquidTank(liquidId, liquidStored, liquidStoredMax) };
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
	    LiquidTank localTank = new LiquidTank(liquidId, liquidStored, liquidStoredMax);
	    if (type.isLiquidEqual(localTank.getLiquid()))
	    {
	        return localTank;
	    }
	    return null;
	}
}
