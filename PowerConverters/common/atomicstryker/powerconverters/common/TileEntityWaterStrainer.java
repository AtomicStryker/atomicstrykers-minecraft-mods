package atomicstryker.powerconverters.common;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

public class TileEntityWaterStrainer extends TileEntityLiquidGenerator
{
	public TileEntityWaterStrainer()
	{
		super(Block.waterStill.blockID, PowerConverterCore.waterConsumedPerOutput, PowerConverterCore.euProducedPerWaterUnit, PowerConverterCore.euPerSecondWater);
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		if(resource.itemID == Block.waterStill.blockID)
		{
			int amountToFill;
			if(isRedstonePowered())
			{
				amountToFill = resource.amount;
			}
			else
			{
				amountToFill = Math.min(resource.amount, liquidStoredMax - liquidStored);
			}
			if(doFill)
			{
				liquidStored += amountToFill;
			}
			return amountToFill;
		}
		else
		{
			BlockPosition p = new BlockPosition(this);
			p.orientation = from.getOpposite();
			p.moveForwards(1);
			TileEntity te = worldObj.getBlockTileEntity(p.x, p.y, p.z);
			if(te != null && te instanceof ITankContainer)
			{
				return ((ITankContainer)te).fill(from, resource, doFill);
			}
			return 0;
		}
	}
}
