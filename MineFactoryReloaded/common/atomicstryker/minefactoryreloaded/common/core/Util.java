package atomicstryker.minefactoryreloaded.common.core;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.BlockFluid;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryLargeChest;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.Property;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.transport.IPipeEntry;

public class Util
{
	public static int addToInventory(IInventory targetInventory, ItemStack stackToAdd)
	{
		int amountLeftToAdd = stackToAdd.stackSize;
		int stackSizeLimit;
		if(stackToAdd.itemID >= Block.blocksList.length)
		{
			stackSizeLimit = Math.min(targetInventory.getInventoryStackLimit(), Item.itemsList[stackToAdd.itemID].getItemStackLimit());
		}
		else
		{
			stackSizeLimit = targetInventory.getInventoryStackLimit();
		}
		int slotIndex;
		
		while(amountLeftToAdd > 0)
		{
			slotIndex = getAvailableSlot(targetInventory, stackToAdd);
			if(slotIndex < 0)
			{
				break;
			}
			ItemStack targetStack = targetInventory.getStackInSlot(slotIndex);
			if(targetStack == null)
			{
				if(stackToAdd.stackSize <= stackSizeLimit)
				{
					ItemStack s = stackToAdd.copy();
					s.stackSize = amountLeftToAdd;
					targetInventory.setInventorySlotContents(slotIndex, s);
					amountLeftToAdd = 0;
					break;
				}
				else
				{
					ItemStack s = stackToAdd.copy();
					s.stackSize = stackSizeLimit;
					targetInventory.setInventorySlotContents(slotIndex, stackToAdd);
					amountLeftToAdd -= s.stackSize;
				}
			}
			else
			{
				int amountToAdd = Math.min(amountLeftToAdd, stackSizeLimit - targetStack.stackSize);
				targetStack.stackSize += amountToAdd;
				amountLeftToAdd -= amountToAdd;
			}
		}
		
		return amountLeftToAdd;
	}
	
	private static int getAvailableSlot(IInventory inventory, ItemStack stack)
	{
		int stackSizeLimit;
		if(stack.itemID >= Block.blocksList.length)
		{
			stackSizeLimit = Math.min(inventory.getInventoryStackLimit(), Item.itemsList[stack.itemID].getItemStackLimit());
		}
		else
		{
			stackSizeLimit = inventory.getInventoryStackLimit();
		}
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack targetStack = inventory.getStackInSlot(i);
			if(targetStack == null)
			{
				return i;
			}
			if(targetStack.itemID == stack.itemID && targetStack.getItemDamage() == stack.getItemDamage()
					&& targetStack.stackSize < stackSizeLimit)
			{
				return i;
			}
		}
		
		return -1;
	}

	public static List<ForgeDirection> findPipes(World world, int x, int y, int z)
	{
		List<ForgeDirection> pipes = new LinkedList<ForgeDirection>();
		BlockPosition ourpos = new BlockPosition(x, y, z);
		for(ForgeDirection o : ForgeDirection.values())
		{
			BlockPosition bp = new BlockPosition(ourpos);
			bp.orientation = o;
			bp.moveForwards(1);
			TileEntity te = world.getBlockTileEntity(bp.x, bp.y, bp.z);
			if(te instanceof IPipeEntry)
			{
				pipes.add(o);
			}
		}
		
		return pipes;
	}
	
	public static List<IInventory> findChests(World world, int x, int y, int z)
	{
		List<IInventory> chests = new LinkedList<IInventory>();
		BlockPosition ourpos = new BlockPosition(x, y, z);
		
		for(BlockPosition bp : ourpos.getAdjacent(true))
		{
			TileEntity te = world.getBlockTileEntity(bp.x, bp.y, bp.z);
			if(te != null && te instanceof IInventory)
			{
				chests.add(checkForDoubleChest(world, te, bp));
			}
		}
		return chests;
	}
	
	private static IInventory checkForDoubleChest(World world, TileEntity te, BlockPosition chestloc)
	{
		if(world.getBlockId(chestloc.x, chestloc.y, chestloc.z) == Block.chest.blockID)
		{
			for(BlockPosition bp : chestloc.getAdjacent(false))
			{
				if(world.getBlockId(bp.x, bp.y, bp.z) == Block.chest.blockID)
				{
					return new InventoryLargeChest("Large Chest", ((IInventory)te), ((IInventory)world.getBlockTileEntity(bp.x, bp.y, bp.z)));
				}
			}
		}
		return ((IInventory)te);
	}
	
	public static boolean isRedstonePowered(TileEntity te)
	{
		if(te.worldObj.isBlockIndirectlyGettingPowered(te.xCoord, te.yCoord, te.zCoord))
		{
			return true;
		}
		for(BlockPosition bp : new BlockPosition(te).getAdjacent(false))
		{
			int blockId = te.worldObj.getBlockId(bp.x, bp.y, bp.z);
			if(blockId == Block.redstoneWire.blockID && Block.blocksList[blockId].isProvidingStrongPower(te.worldObj, bp.x, bp.y, bp.z, 1))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isHoldingWrench(EntityPlayer player)
	{
		return player.inventory.getCurrentItem() != null && 
			player.inventory.getCurrentItem().itemID == MineFactoryReloadedCore.factoryHammerItem.shiftedIndex;
	}
	
	public static boolean getBool(Property property)
	{
		return Boolean.parseBoolean(property.value);
	}
	
	public static int getInt(Property property)
	{
		return Integer.parseInt(property.value);
	}
	
	public static boolean isBlockUnbreakable(Block b)
	{
		return BuildCraftAPI.unbreakableBlock(b.blockID) || b instanceof BlockFluid;
	}
}
