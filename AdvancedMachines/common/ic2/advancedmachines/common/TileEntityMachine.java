package ic2.advancedmachines.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class TileEntityMachine extends TileEntityBlock implements IInventory
{
    public ItemStack[] inventory;

    public TileEntityMachine(int inventorySize)
    {
    	super();
        this.inventory = new ItemStack[inventorySize];
    }

    @Override
    public int getSizeInventory()
    {
        return this.inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int var1)
    {
        return this.inventory[var1];
    }

    @Override
    public ItemStack decrStackSize(int var1, int var2)
    {
        if (this.inventory[var1] != null)
        {
            ItemStack var3;
            if (this.inventory[var1].stackSize <= var2)
            {
                var3 = this.inventory[var1];
                this.inventory[var1] = null;
                return var3;
            }
            else
            {
                var3 = this.inventory[var1].splitStack(var2);
                if (this.inventory[var1].stackSize == 0)
                {
                    this.inventory[var1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int var1, ItemStack var2)
    {
        this.inventory[var1] = var2;
        if (var2 != null && var2.stackSize > this.getInventoryStackLimit())
        {
            var2.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1)
    {
        return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : var1.getDistance((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public abstract String getInvName();

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        NBTTagList var2 = var1.getTagList("Items");
        this.inventory = new ItemStack[this.getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
            byte var5 = var4.getByte("Slot");
            if (var5 >= 0 && var5 < this.inventory.length)
            {
                this.inventory[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
        NBTTagList var2 = new NBTTagList();

        for (int var3 = 0; var3 < this.inventory.length; ++var3)
        {
            if (this.inventory[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                this.inventory[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }

        var1.setTag("Items", var2);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
    }

    @Override
    public void openChest() {}

    @Override
    public void closeChest() {}
    
    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
    	return null;
    }
}
