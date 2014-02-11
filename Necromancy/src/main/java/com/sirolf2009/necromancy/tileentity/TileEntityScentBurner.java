package com.sirolf2009.necromancy.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;

public class TileEntityScentBurner extends TileEntity implements IInventory, ISidedInventory
{

    private final ItemStack[] inventory;
    public boolean isBurning;
    private int burnTime;
    private int dyeColor;

    public TileEntityScentBurner()
    {
        inventory = new ItemStack[2];
    }
    
    @Override
    public void updateEntity()
    {
        if (!isBurning)
        {
            if (getStackInSlot(0) != null && getStackInSlot(1) != null)
            {
                if (getStackInSlot(0).getItem() instanceof ItemDye && TileEntityFurnace.isItemFuel(getStackInSlot(1)))
                {
                    isBurning = true;
                    burnTime = TileEntityFurnace.getItemBurnTime(getStackInSlot(1));
                    dyeColor = getStackInSlot(0).getItemDamage();
                    getStackInSlot(0).stackSize--;
                    getStackInSlot(1).stackSize--;
                    if (getStackInSlot(0).stackSize <= 0)
                    {
                        inventory[0] = inventory[0].getItem().getContainerItem(inventory[0]);
                    }
                    if (getStackInSlot(1).stackSize <= 0)
                    {
                        inventory[1] = inventory[1].getItem().getContainerItem(inventory[1]);
                    }
                }
            }
        }
        else
        {
            if (burnTime > 0)
                --burnTime;
            else if (burnTime <= 0)
            {
                isBurning = false;
            }
            if (worldObj.getBlock(xCoord, yCoord + 1, zCoord) != RegistryBlocksNecromancy.scent)
            {
                worldObj.setBlock(xCoord, yCoord + 1, zCoord, RegistryBlocksNecromancy.scent, 0, 3);
                TileEntityScent scent = (TileEntityScent) worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
                scent.addScent(dyeColor, 255);
                scent.setReach(5);
                scent.setAir(100);
                scent.setSourceLocation(new int[] { xCoord, yCoord, zCoord });
            }
            else
            {
                TileEntityScent scent = (TileEntityScent) worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
                scent.addScent(dyeColor, 255);
                scent.setAir(scent.getAir() - 20);
            }
        }
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return inventory[slotIndex];
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount)
    {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack != null)
        {
            if (stack.stackSize <= amount)
            {
                setInventorySlotContents(slotIndex, null);
            }
            else
            {
                stack = stack.splitStack(amount);
                if (stack.stackSize == 0)
                {
                    setInventorySlotContents(slotIndex, null);
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack != null)
        {
            setInventorySlotContents(slotIndex, null);
        }

        return stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        NBTTagList tagList = tagCompound.getTagList("Inventory", 10);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);

            byte slot = tag.getByte("Slot");

            if (slot >= 0 && slot < inventory.length)
            {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);

        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];

            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();

                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }

        tagCompound.setTag("Inventory", itemList);
    }

    @Override
    public String getInventoryName()
    {
        return "TileEntityScentBurner";
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1)
    {
        return null;
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j)
    {
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j)
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return false;
    }
}