package atomicstryker.minefactoryreloaded.common.blocks;

import atomicstryker.minefactoryreloaded.client.MineFactoryClient;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.core.Util;
import net.minecraft.src.Block;
import net.minecraft.src.BlockRail;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class BlockRailCargoDropoff extends BlockRail
{
	public BlockRailCargoDropoff(int i, int j)
	{
		super(i, j, true);
		setBlockName("cargoDropoffRail");
		setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if(world.isRemote || !(entity instanceof EntityMinecart))
		{
			return;
		}
		EntityMinecart minecart = (EntityMinecart)entity;
		if(minecart.minecartType != 1)
		{
			return;
		}
		
		for(int slotIndex = 0; slotIndex < minecart.getSizeInventory(); slotIndex++)
		{
			ItemStack ourStack = minecart.getStackInSlot(slotIndex);
			if(ourStack == null)
			{
				continue;
			}
			for(IInventory chest : Util.findChests(world, x, y, z))
			{
				ItemStack stackToAdd = ourStack.copy();
				int amountRemaining = Util.addToInventory(chest, stackToAdd);
				if(amountRemaining == 0)
				{
					minecart.setInventorySlotContents(slotIndex, null);
					break;
				}
				else
				{
					ourStack.stackSize = amountRemaining;
				}
			}
		}
	}

	@Override
	public String getTextureFile()
	{
        return MineFactoryReloadedCore.terrainTexture;
	}
}
