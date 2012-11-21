package atomicstryker.minefactoryreloaded.common.tileentities;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.liquids.LiquidContainerRegistry;

public class TileEntityWeather extends TileEntityFactoryInventory
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
				if(!produceLiquid(LiquidContainerRegistry.getLiquidForFilledItem(new ItemStack(Item.bucketWater))))
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

}
