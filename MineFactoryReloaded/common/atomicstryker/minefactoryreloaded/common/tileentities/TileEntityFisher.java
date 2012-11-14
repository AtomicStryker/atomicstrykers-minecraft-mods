package atomicstryker.minefactoryreloaded.common.tileentities;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class TileEntityFisher extends TileEntityFactoryInventory
{
	public TileEntityFisher()
	{
		super(10, 10);
	}

	@Override
	public String getInvName()
	{
		return "Fisher";
	}
	
	public void doWork()
	{
		if(!powerAvailable())
		{
			return;
		}
		
		for(int xOffset = -1; xOffset <= 1; xOffset++)
		{
			for(int zOffset = -1; zOffset <= 1; zOffset++)
			{
				
				if(worldObj.getBlockId(xCoord + xOffset, yCoord - 1, zCoord + zOffset) == Block.waterStill.blockID)
				{
					int bucketIndex = findFirstStack(Item.bucketEmpty.shiftedIndex, 0);
					if(bucketIndex >= 0)
					{
						dropStack(new ItemStack(Item.bucketWater), 0.5F, 1.5F, 0.5F);
						setInventorySlotContents(bucketIndex, null);
						worldObj.setBlockWithNotify(xCoord + xOffset, yCoord - 1, zCoord + zOffset, 0);
						return;
					}
					if(worldObj.rand.nextInt(100) < 1)
					{
						dropStack(new ItemStack(Item.fishRaw), 0.5F, 1.5F, 0.5F);
						return;
					}
				}
			}
		}
	}
	
}
