package atomicstryker.minefactoryreloaded.common.tileentities;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;

public class TileEntityWeather extends TileEntityFactoryInventory implements ITankContainer
{
	public TileEntityWeather()
	{
		super(10, 10);
	}

	@Override
	public String getInvName()
	{
		return "WeatherCollector";
	}

	@Override
	public void doWork()
	{
		if(!powerAvailable())
		{
			return;
		}
		
		if(worldObj.getWorldInfo().isRaining() && canSeeSky())
		{
			int bucketIndex = findFirstStack(Item.bucketEmpty.shiftedIndex, 0);
			if(bucketIndex >= 0)
			{
				dropStack(new ItemStack(Item.bucketWater), 0.5F, -0.5F, 0.5F);
				decrStackSize(bucketIndex, 1);
			}
			else
			{
				if(!produceLiquid(LiquidManager.getLiquidIDForFilledItem(new ItemStack(Item.bucketWater))))
				{
					dropStack(new ItemStack(Item.snowball), 0.5F, -0.5F, 0.5F);
				}
			}
		}
	}
	
	private boolean canSeeSky()
	{
		for(int y = yCoord + 1; y <= 128; y++)
		{
			int blockId = worldObj.getBlockId(xCoord, y, zCoord);
			if(Block.blocksList[blockId] != null && !Block.blocksList[blockId].isAirBlock(worldObj, xCoord, y, zCoord))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
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
