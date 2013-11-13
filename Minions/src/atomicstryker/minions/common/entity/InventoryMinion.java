package atomicstryker.minions.common.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;

/**
 * Minion Inventory Class, has some standalone extras compared to a player inventory but no armor
 * 
 * 
 * @author AtomicStryker
 */

public class InventoryMinion implements IInventory
{
    public ItemStack[] mainInventory = new ItemStack[24];
    public EntityMinion minion;
    public boolean inventoryChanged = false;

    public InventoryMinion(EntityMinion var1)
    {
        this.minion = var1;
    }
    
    public boolean containsItems()
    {
    	return this.getFirstEmptyStack() != 0;
    }

    private int getInventorySlotContainItem(int var1)
    {
        for (int var2 = 0; var2 < this.mainInventory.length; ++var2)
        {
            if (this.mainInventory[var2] != null && this.mainInventory[var2].itemID == var1)
            {
                return var2;
            }
        }

        return -1;
    }

    private int storeItemStack(ItemStack var1)
    {
        for (int var2 = 0; var2 < this.mainInventory.length; ++var2)
        {
            if (this.mainInventory[var2] != null
            		&& this.mainInventory[var2].itemID == var1.itemID
            		&& this.mainInventory[var2].isStackable()
            		&& this.mainInventory[var2].stackSize < this.mainInventory[var2].getMaxStackSize()
            		&& this.mainInventory[var2].stackSize < this.getInventoryStackLimit()
            		&& (!this.mainInventory[var2].getHasSubtypes() || this.mainInventory[var2].getItemDamage() == var1.getItemDamage()))
            {
                return var2;
            }
        }

        return -1;
    }

    private int getFirstEmptyStack()
    {
        for (int var1 = 0; var1 < this.mainInventory.length; ++var1)
        {
            if (this.mainInventory[var1] == null)
            {
                return var1;
            }
        }

        return -1;
    }

    private int storePartialItemStack(ItemStack var1)
    {
        int var2 = var1.itemID;
        int var3 = var1.stackSize;
        int var4;
        if (var1.getMaxStackSize() == 1)
        {
            var4 = this.getFirstEmptyStack();
            if (var4 < 0)
            {
                return var3;
            }
            else
            {
                if (this.mainInventory[var4] == null)
                {
                    this.mainInventory[var4] = ItemStack.copyItemStack(var1);
                }

                return 0;
            }
        }
        else
        {
            var4 = this.storeItemStack(var1);
            if (var4 < 0)
            {
                var4 = this.getFirstEmptyStack();
            }

            if (var4 < 0)
            {
                return var3;
            }
            else
            {
                if (this.mainInventory[var4] == null)
                {
                    this.mainInventory[var4] = new ItemStack(var2, 0, var1.getItemDamage());
                    if (var1.hasTagCompound())
                    {
                        this.mainInventory[var4].setTagCompound((NBTTagCompound)var1.getTagCompound().copy());
                    }
                }

                int var5 = var3;
                if (var3 > this.mainInventory[var4].getMaxStackSize() - this.mainInventory[var4].stackSize)
                {
                    var5 = this.mainInventory[var4].getMaxStackSize() - this.mainInventory[var4].stackSize;
                }

                if (var5 > this.getInventoryStackLimit() - this.mainInventory[var4].stackSize)
                {
                    var5 = this.getInventoryStackLimit() - this.mainInventory[var4].stackSize;
                }

                if (var5 == 0)
                {
                    return var3;
                }
                else
                {
                    var3 -= var5;
                    this.mainInventory[var4].stackSize += var5;
                    this.mainInventory[var4].animationsToGo = 5;
                    return var3;
                }
            }
        }
    }

    public boolean consumeInventoryItem(int var1)
    {
        int var2 = this.getInventorySlotContainItem(var1);
        if (var2 < 0)
        {
            return false;
        }
        else
        {
            if (--this.mainInventory[var2].stackSize <= 0)
            {
                this.mainInventory[var2] = null;
            }

            return true;
        }
    }

    public boolean hasItem(int var1)
    {
        int var2 = this.getInventorySlotContainItem(var1);
        return var2 >= 0;
    }

    public boolean addItemStackToInventory(ItemStack var1)
    {
        int var2;
        if (var1.itemID > 0 && var1.isItemStackDamageable() && var1.isItemDamaged())
        {
            var2 = this.getFirstEmptyStack();
            if (var2 >= 0)
            {
                this.mainInventory[var2] = ItemStack.copyItemStack(var1);
                this.mainInventory[var2].animationsToGo = 5;
                var1.stackSize = 0;
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            do
            {
                var2 = var1.stackSize;
                var1.stackSize = this.storePartialItemStack(var1);
            }
            while (var1.stackSize > 0 && var1.stackSize < var2);

            return var1.stackSize < var2;
        }
    }

    public ItemStack decrStackSize(int var1, int var2)
    {
        ItemStack[] var3 = this.mainInventory;
        if (var1 >= this.mainInventory.length)
        {
            var1 -= this.mainInventory.length;
        }

        if (var3[var1] != null)
        {
            ItemStack var4;
            if (var3[var1].stackSize <= var2)
            {
                var4 = var3[var1];
                var3[var1] = null;
                return var4;
            }
            else
            {
                var4 = var3[var1].splitStack(var2);
                if (var3[var1].stackSize == 0)
                {
                    var3[var1] = null;
                }

                return var4;
            }
        }
        else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int var1, ItemStack var2)
    {
        ItemStack[] var3 = this.mainInventory;
        if (var1 >= var3.length)
        {
            var1 -= var3.length;
        }

        var3[var1] = var2;
    }

    public NBTTagList writeToNBT(NBTTagList var1)
    {
        int var2;
        NBTTagCompound var3;
        for (var2 = 0; var2 < this.mainInventory.length; ++var2)
        {
            if (this.mainInventory[var2] != null)
            {
                var3 = new NBTTagCompound();
                var3.setByte("Slot", (byte)var2);
                this.mainInventory[var2].writeToNBT(var3);
                var1.appendTag(var3);
            }
        }

        return var1;
    }

    public void readFromNBT(NBTTagList var1)
    {
        this.mainInventory = new ItemStack[36];

        for (int var2 = 0; var2 < var1.tagCount(); ++var2)
        {
            NBTTagCompound var3 = (NBTTagCompound)var1.tagAt(var2);
            int var4 = var3.getByte("Slot") & 255;
            ItemStack var5 = ItemStack.loadItemStackFromNBT(var3);
            if (var5 != null)
            {
                if (var4 >= 0 && var4 < this.mainInventory.length)
                {
                    this.mainInventory[var4] = var5;
                }
            }
        }
    }

    public int getSizeInventory()
    {
        return this.mainInventory.length + 4;
    }

    public ItemStack getStackInSlot(int var1)
    {
        ItemStack[] var2 = this.mainInventory;
        if (var1 >= var2.length)
        {
            var1 -= var2.length;
        }

        return var2[var1];
    }

    public String getInvName()
    {
        return "MinionInventory";
    }

    public int getInventoryStackLimit()
    {
        return 64;
    }

    public void dropAllItems()
    {
        int var1;
        for (var1 = 0; var1 < this.mainInventory.length; ++var1)
        {
            if (this.mainInventory[var1] != null)
            {
                this.minion.dropMinionItemWithRandomChoice(this.mainInventory[var1]);
                this.mainInventory[var1] = null;
            }
        }
        minion.inventoryFull = false;
    }
    
	public void putAllItemsToInventory(IInventory returnChestOrInventory)
	{
        int var1;
        for (var1 = 0; var1 < this.mainInventory.length; ++var1)
        {
            if (this.mainInventory[var1] != null)
            {
            	if (addItemStackToInventory(returnChestOrInventory, this.mainInventory[var1]))
            	{
            		this.mainInventory[var1] = null;
            	}
            	else if (returnChestOrInventory instanceof TileEntityChest)
            	{
            	    TileEntityChest returnChest = (TileEntityChest)returnChestOrInventory;
            	    if (returnChest.adjacentChestXNeg != null && addItemStackToInventory(returnChest.adjacentChestXNeg, this.mainInventory[var1])
                    || returnChest.adjacentChestXPos != null && addItemStackToInventory(returnChest.adjacentChestXPos, this.mainInventory[var1])
                    || returnChest.adjacentChestZNeg != null && addItemStackToInventory(returnChest.adjacentChestZNeg, this.mainInventory[var1])
                    || returnChest.adjacentChestZPosition != null && addItemStackToInventory(returnChest.adjacentChestZPosition, this.mainInventory[var1]))
            	    {
            	        this.mainInventory[var1] = null;
            	    }
            	}
            	else
            	{
            		this.dropAllItems();
            		return;
            	}
            }
        }
        minion.inventoryFull = false;
	}
	
    private boolean addItemStackToInventory(IInventory inv, ItemStack item)
    {
        int index;
        if (item.itemID > 0 && item.isItemStackDamageable() && item.isItemDamaged())
        {
            index = getInvFirstEmptyStack(inv);
            if (index >= 0)
            {
                inv.setInventorySlotContents(index, ItemStack.copyItemStack(item));
                inv.getStackInSlot(index).animationsToGo = 5;
                item.stackSize = 0;
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            do
            {
                index = item.stackSize;
                item.stackSize = storePartialItemStackInChest(inv, item);
            }
            while (item.stackSize > 0 && item.stackSize < index);

            return item.stackSize < index;
        }
    }
    
    private int storeItemStackInInv(IInventory inv, ItemStack item)
    {
        for (int index = 0; index < inv.getSizeInventory(); ++index)
        {
            if (inv.getStackInSlot(index) != null
            && inv.getStackInSlot(index).itemID == item.itemID
            && inv.getStackInSlot(index).isStackable()
            && inv.getStackInSlot(index).stackSize < inv.getStackInSlot(index).getMaxStackSize()
            && inv.getStackInSlot(index).stackSize < inv.getInventoryStackLimit()
            && (!inv.getStackInSlot(index).getHasSubtypes() || inv.getStackInSlot(index).getItemDamage() == item.getItemDamage()))
            {
                return index;
            }
        }

        return -1;
    }
    
    private int getInvFirstEmptyStack(IInventory inv)
    {
        for (int index = 0; index < inv.getSizeInventory(); ++index)
        {
            if (inv.getStackInSlot(index) == null)
            {
                return index;
            }
        }

        return -1;
    }
    
    private int storePartialItemStackInChest(IInventory inv, ItemStack item)
    {
        int itemID = item.itemID;
        int stacksize = item.stackSize;
        int index;
        if (item.getMaxStackSize() == 1)
        {
            index = getInvFirstEmptyStack(inv);
            if (index < 0)
            {
                return stacksize;
            }
            else
            {
                if (inv.getStackInSlot(index) == null)
                {
                    inv.setInventorySlotContents(index, ItemStack.copyItemStack(item));
                }

                return 0;
            }
        }
        else
        {
            index = this.storeItemStackInInv(inv, item);
            if (index < 0)
            {
                index = getInvFirstEmptyStack(inv);
            }

            if (index < 0)
            {
                return stacksize;
            }
            else
            {
                if (inv.getStackInSlot(index) == null)
                {
                    inv.setInventorySlotContents(index, new ItemStack(itemID, 0, item.getItemDamage()));
                    if (item.hasTagCompound())
                    {
                        inv.getStackInSlot(index).setTagCompound((NBTTagCompound)item.getTagCompound().copy());
                    }
                }

                int remainingsize = stacksize;
                if (stacksize > inv.getStackInSlot(index).getMaxStackSize() - inv.getStackInSlot(index).stackSize)
                {
                    remainingsize = inv.getStackInSlot(index).getMaxStackSize() - inv.getStackInSlot(index).stackSize;
                }

                if (remainingsize > inv.getInventoryStackLimit() - inv.getStackInSlot(index).stackSize)
                {
                    remainingsize = inv.getInventoryStackLimit() - inv.getStackInSlot(index).stackSize;
                }

                if (remainingsize == 0)
                {
                    return stacksize;
                }
                else
                {
                    stacksize -= remainingsize;
                    inv.getStackInSlot(index).stackSize += remainingsize;
                    inv.getStackInSlot(index).animationsToGo = 5;
                    return stacksize;
                }
            }
        }
    }

    public void onInventoryChanged()
    {
        this.inventoryChanged = true;
    }

    public boolean isUseableByPlayer(EntityPlayer var1)
    {
        return (var1.username.equals(minion.getMasterUserName()) && var1.getDistanceSqToEntity(this.minion) <= 64.0D);
    }

    public boolean hasItemStack(ItemStack var1)
    {
        int var2;
        for (var2 = 0; var2 < this.mainInventory.length; ++var2)
        {
            if (this.mainInventory[var2] != null && this.mainInventory[var2].isItemEqual(var1))
            {
                return true;
            }
        }

        return false;
    }

    public void openChest() {}

    public void closeChest() {}

    public void copyInventory(InventoryMinion var1)
    {
        int var2;
        for (var2 = 0; var2 < this.mainInventory.length; ++var2)
        {
            this.mainInventory[var2] = ItemStack.copyItemStack(var1.mainInventory[var2]);
        }
    }

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
	}

    @Override
    public boolean isInvNameLocalized()
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return true;
    }
}
