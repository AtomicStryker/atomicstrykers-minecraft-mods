package com.sirolf2009.necromancy.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;
import com.sirolf2009.necromancy.tileentity.TileEntityScentBurner;

public class ContainerScentBurner extends Container
{
    
    private TileEntityScentBurner burner;
    public InventoryCrafting craftMatrix;
    public IInventory craftResult;
    private World worldObj;
    private int posX;
    private int posY;
    private int posZ;

    public ContainerScentBurner(InventoryPlayer inventory, TileEntity tileEntityBurner)
    {
        this.burner = (TileEntityScentBurner) tileEntityBurner;
        craftMatrix = new InventoryCrafting(this, 4, 4);
        craftResult = new InventoryCraftResult();
        worldObj = burner.getWorldObj();
        posX = burner.xCoord;
        posY = burner.yCoord;
        posZ = burner.zCoord;
        this.addSlotToContainer(new Slot(burner, 0, 80, 17));
        this.addSlotToContainer(new Slot(burner, 1, 80, 53));
        int i;

        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer)
    {
        super.onContainerClosed(par1EntityPlayer);
        if (!worldObj.isRemote)
        {
            for (int var2 = 0; var2 < 16; var2++)
            {
                ItemStack var3 = craftMatrix.getStackInSlotOnClosing(var2);
                if (var3 != null)
                {
                    par1EntityPlayer.entityDropItem(var3, 0f);
                }
            }

        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return worldObj.getBlock(posX, posY, posZ) == RegistryBlocksNecromancy.scentBurner ? par1EntityPlayer.getDistanceSq(posX + 0.5D, posY + 0.5D,
                posZ + 0.5D) <= 64D : false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (slotIndex == 0 || slotIndex == 1)
            {
                if (!this.mergeItemStack(itemstack1, 2, 38, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (slotIndex != 1 && slotIndex != 0)
            {
                if (FurnaceRecipes.smelting().getSmeltingResult(itemstack1) != null)
                {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false))
                    {
                        return null;
                    }
                }
                else if (TileEntityFurnace.isItemFuel(itemstack1))
                {
                    if (!this.mergeItemStack(itemstack1, 1, 2, false))
                    {
                        return null;
                    }
                }
                else if (slotIndex >= 3 && slotIndex < 30)
                {
                    if (!this.mergeItemStack(itemstack1, 30, 39, false))
                    {
                        return null;
                    }
                }
                else if (slotIndex >= 30 && slotIndex < 39 && !this.mergeItemStack(itemstack1, 3, 30, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 3, 39, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack) null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }
}
