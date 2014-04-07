package atomicstryker.minions.common.entity;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ReportedException;

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
    
    private int storeItemStack(ItemStack par1ItemStack)
    {
        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == par1ItemStack.getItem() && this.mainInventory[i].isStackable() && this.mainInventory[i].stackSize < this.mainInventory[i].getMaxStackSize() && this.mainInventory[i].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[i].getHasSubtypes() || this.mainInventory[i].getItemDamage() == par1ItemStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(this.mainInventory[i], par1ItemStack))
            {
                return i;
            }
        }

        return -1;
    }
    
    private int getFirstEmptyStack()
    {
        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] == null)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     */
    private int storePartialItemStack(ItemStack par1ItemStack)
    {
        Item item = par1ItemStack.getItem();
        int i = par1ItemStack.stackSize;
        int j;

        if (par1ItemStack.getMaxStackSize() == 1)
        {
            j = this.getFirstEmptyStack();

            if (j < 0)
            {
                return i;
            }
            else
            {
                if (this.mainInventory[j] == null)
                {
                    this.mainInventory[j] = ItemStack.copyItemStack(par1ItemStack);
                }

                return 0;
            }
        }
        else
        {
            j = this.storeItemStack(par1ItemStack);

            if (j < 0)
            {
                j = this.getFirstEmptyStack();
            }

            if (j < 0)
            {
                return i;
            }
            else
            {
                if (this.mainInventory[j] == null)
                {
                    this.mainInventory[j] = new ItemStack(item, 0, par1ItemStack.getItemDamage());

                    if (par1ItemStack.hasTagCompound())
                    {
                        this.mainInventory[j].setTagCompound((NBTTagCompound)par1ItemStack.getTagCompound().copy());
                    }
                }

                int k = i;

                if (i > this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].stackSize)
                {
                    k = this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].stackSize;
                }

                if (k > this.getInventoryStackLimit() - this.mainInventory[j].stackSize)
                {
                    k = this.getInventoryStackLimit() - this.mainInventory[j].stackSize;
                }

                if (k == 0)
                {
                    return i;
                }
                else
                {
                    i -= k;
                    this.mainInventory[j].stackSize += k;
                    this.mainInventory[j].animationsToGo = 5;
                    return i;
                }
            }
        }
    }
    
    public boolean addItemStackToInventory(final ItemStack par1ItemStack)
    {
        if (par1ItemStack != null && par1ItemStack.stackSize != 0 && par1ItemStack.getItem() != null)
        {
            try
            {
                int i;

                if (par1ItemStack.isItemDamaged())
                {
                    i = this.getFirstEmptyStack();

                    if (i >= 0)
                    {
                        this.mainInventory[i] = ItemStack.copyItemStack(par1ItemStack);
                        this.mainInventory[i].animationsToGo = 5;
                        par1ItemStack.stackSize = 0;
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
                        i = par1ItemStack.stackSize;
                        par1ItemStack.stackSize = this.storePartialItemStack(par1ItemStack);
                    }
                    while (par1ItemStack.stackSize > 0 && par1ItemStack.stackSize < i);

                    return par1ItemStack.stackSize < i;
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(par1ItemStack.getItem())));
                crashreportcategory.addCrashSection("Item data", Integer.valueOf(par1ItemStack.getItemDamage()));
                throw new ReportedException(crashreport);
            }
        }
        else
        {
            return false;
        }
    }
    
    public NBTTagList writeToNBT(NBTTagList par1NBTTagList)
    {
        NBTTagCompound nbttagcompound;

        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] != null)
            {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                this.mainInventory[i].writeToNBT(nbttagcompound);
                par1NBTTagList.appendTag(nbttagcompound);
            }
        }

        return par1NBTTagList;
    }
    
    public void readFromNBT(NBTTagList par1NBTTagList)
    {
        this.mainInventory = new ItemStack[24];

        for (int i = 0; i < par1NBTTagList.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = par1NBTTagList.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

            if (itemstack != null)
            {
                if (j >= 0 && j < this.mainInventory.length)
                {
                    this.mainInventory[j] = itemstack;
                }
            }
        }
    }
    
    public boolean consumeInventoryItem(Object item)
    {
        int i = this.getFirstSlotWithItem(item);

        if (i < 0)
        {
            return false;
        }
        else
        {
            if (--this.mainInventory[i].stackSize <= 0)
            {
                this.mainInventory[i] = null;
            }

            return true;
        }
    }
    
    private int getFirstSlotWithItem(Object item)
    {
        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] != null)
            {
                if (mainInventory[i] == item || mainInventory[i].getItem() == item)
                {
                    return i;
                }
            }
        }

        return -1;
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

    public boolean containsItems()
    {
        return this.getFirstEmptyStack() != 0;
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
                    || returnChest.adjacentChestZPos != null && addItemStackToInventory(returnChest.adjacentChestZPos, this.mainInventory[var1]))
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
        if (item.isItemStackDamageable() && item.isItemDamaged())
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
    
    private int storePartialItemStackInChest(IInventory inv, ItemStack item)
    {
        Item itemID = item.getItem();
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
    
    private int storeItemStackInInv(IInventory inv, ItemStack item)
    {
        for (int index = 0; index < inv.getSizeInventory(); ++index)
        {
            if (inv.getStackInSlot(index) != null
            && inv.getStackInSlot(index).getItem() == item.getItem()
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
    
    @Override
    public ItemStack decrStackSize(int par1, int par2)
    {
        ItemStack[] aitemstack = this.mainInventory;

        if (par1 >= this.mainInventory.length)
        {
            return null;
        }

        if (aitemstack[par1] != null)
        {
            ItemStack itemstack;

            if (aitemstack[par1].stackSize <= par2)
            {
                itemstack = aitemstack[par1];
                aitemstack[par1] = null;
                return itemstack;
            }
            else
            {
                itemstack = aitemstack[par1].splitStack(par2);

                if (aitemstack[par1].stackSize == 0)
                {
                    aitemstack[par1] = null;
                }

                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public ItemStack getStackInSlotOnClosing(int par1)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        ItemStack[] aitemstack = this.mainInventory;

        if (par1 < aitemstack.length)
        {
            aitemstack[par1] = par2ItemStack;
        }
    }

    @Override
    public int getSizeInventory()
    {
        return this.mainInventory.length + 4;
    }
    
    @Override
    public ItemStack getStackInSlot(int par1)
    {
        return mainInventory[par1];
    }
    
    @Override
    public String getInventoryName()
    {
        return "MinionInventory";
    }
    
    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }
    
    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public void markDirty()
    {
        this.inventoryChanged = true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
        return false;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}
    
    @Override
    public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack)
    {
        return true;
    }
    
}

/*
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

    private int getInventorySlotContainItem(Item var1)
    {
        for (int var2 = 0; var2 < this.mainInventory.length; ++var2)
        {
            if (this.mainInventory[var2] != null && this.mainInventory[var2].getItem() == var1)
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

    public void markDirty()
    {
        this.inventoryChanged = true;
    }

    public boolean isUseableByPlayer(EntityPlayer var1)
    {
        return (var1.getGameProfile().getName().equals(minion.getMasterUserName()) && var1.getDistanceSqToEntity(this.minion) <= 64.0D);
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

    public void openInventory() {}

    public void closeInventory() {}

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
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return true;
    }

}
*/
