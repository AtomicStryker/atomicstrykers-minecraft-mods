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

public class BlockRailCargoPickup extends BlockRail
{
	public BlockRailCargoPickup(int i, int j)
	{
		super(i, j, true);
		setBlockName("cargoPickupRail");
		setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
	}

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
		
		for(IInventory chest : Util.findChests(world, x, y, z))
		{
			for(int slotIndex = 0; slotIndex < chest.getSizeInventory(); slotIndex++)
			{
				ItemStack sourceStack = chest.getStackInSlot(slotIndex);
				if(sourceStack == null)
				{
					continue;
				}
				ItemStack stackToAdd = sourceStack.copy();
				int amountRemaining = Util.addToInventory(minecart, stackToAdd);
				if(amountRemaining == 0)
				{
					chest.setInventorySlotContents(slotIndex, null);
				}
				else
				{
					sourceStack.stackSize = amountRemaining;
					break;
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
