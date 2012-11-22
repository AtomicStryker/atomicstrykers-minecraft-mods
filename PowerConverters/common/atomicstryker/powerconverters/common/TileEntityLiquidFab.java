package atomicstryker.powerconverters.common;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySink;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.api.core.Position;

public abstract class TileEntityLiquidFab extends TileEntityPowerConverter implements IEnergySink, ITankContainer
{
    
	private int storedEnergy = 0;
	private int maxStoredEnergy = 1600;
	private int liquidId;
	private int liquidCost;
	
	protected TileEntityLiquidFab(int maxStoredEnergy, int liquidId, int liquidCost)
	{
		this.maxStoredEnergy = maxStoredEnergy;
		this.liquidId = liquidId;
		this.liquidCost = liquidCost;
	}
	// base methods
	
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
		if (storedEnergy >= liquidCost)
		{
			for (int i = 0; i < 6; ++i)
			{
				Position p = new Position(xCoord, yCoord, zCoord, ForgeDirection.values()[i]);
				p.moveForwards(1);

				TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y,	(int) p.z);

				if(tile instanceof ITankContainer)
				{
					int liquidToProduce = storedEnergy / liquidCost;
					int liquidRemaining = liquidToProduce - ((ITankContainer) tile).fill(p.orientation.getOpposite(), new LiquidStack(liquidId, liquidToProduce), true);
					int liquidUsed = liquidToProduce - liquidRemaining;
					storedEnergy -= liquidUsed * liquidCost;
					
					if(liquidRemaining <= 0)
					{
						break;
					}
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
		int amountToAdd = Math.min(amount, maxStoredEnergy - storedEnergy);
		storedEnergy += amountToAdd;
		return amount - amountToAdd;
	}
	
	// ILiquidContainer methods

	@Override
    public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		return 0;
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
	    return null;
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
	    return null;
	}
	
}