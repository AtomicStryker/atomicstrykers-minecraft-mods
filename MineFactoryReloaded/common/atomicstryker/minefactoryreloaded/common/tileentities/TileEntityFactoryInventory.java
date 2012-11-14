package atomicstryker.minefactoryreloaded.common.tileentities;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.World;

public abstract class TileEntityFactoryInventory extends TileEntityFactoryPowered implements IInventory
{
	protected TileEntityFactoryInventory(int bcEnergyNeededToWork,	int bcEnergyNeededToActivate)
	{
		super(bcEnergyNeededToWork, bcEnergyNeededToActivate);
		inventory = new ItemStack[getSizeInventory()];
	}
	
	// IInventory methods

	protected ItemStack[] inventory;
	
	@Override
	public int getSizeInventory()
	{
		return 27;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return inventory[i];
	}
	
	@Override
	public void openChest()
	{
	}
	
	@Override
	public void closeChest()
	{
	}

	@Override
    public ItemStack decrStackSize(int i, int j)
    {
        if(inventory[i] != null)
        {
            if(inventory[i].stackSize <= j)
            {
                ItemStack itemstack = inventory[i];
                inventory[i] = null;
                return itemstack;
            }
            ItemStack itemstack1 = inventory[i].splitStack(j);
            if(inventory[i].stackSize == 0)
            {
                inventory[i] = null;
            }
            return itemstack1;
        }
        else
        {
            return null;
        }
    }

	@Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        inventory[i] = itemstack;
        if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
        if(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
        {
            return false;
        }
        return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        inventory = new ItemStack[getSizeInventory()];
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xff;
            if(j >= 0 && j < inventory.length)
            {
            	ItemStack s = new ItemStack(0, 0, 0);
            	s.readFromNBT(nbttagcompound1);
                inventory[j] = s;
            }
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();
        for(int i = 0; i < inventory.length; i++)
        {
            if(inventory[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);
    }
	
	public int findFirstStack(int itemId, int itemDamage)
	{
		for(int i = 0; i < getSizeInventory(); i++)
		{
			ItemStack s = getStackInSlot(i);
			if(s != null && s.itemID == itemId && s.getItemDamage() == itemDamage)
			{
				return i;
			}
		}
		return -1;
	}
	
    @Override
    public ItemStack getStackInSlotOnClosing(int var1)
    {
        return null;
    }

}
