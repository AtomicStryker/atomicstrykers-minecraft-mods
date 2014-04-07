package com.sirolf2009.necromancy.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;
import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

public class ContainerSewing extends Container
{

    private TileEntitySewing Sewing;
    public InventoryCrafting craftMatrix;
    public IInventory craftResult;
    private World worldObj;
    private int posX;
    private int posY;
    private int posZ;

    public ContainerSewing(InventoryPlayer inventory, TileEntity sewing)
    {
        Sewing = (TileEntitySewing) sewing;
        craftMatrix = new InventoryCrafting(this, 4, 4);
        craftResult = new InventoryCraftResult();
        worldObj = sewing.getWorldObj();
        posX = sewing.xCoord;
        posY = sewing.yCoord;
        posZ = sewing.zCoord;
        int var3;
        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }
        for (var3 = 0; var3 < 9; ++var3)
        {
            this.addSlotToContainer(new Slot(inventory, var3, 8 + var3 * 18, 142));
        }
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 4; y++)
            {
                addSlotToContainer(new Slot(craftMatrix, y + x * 4, 8 + y * 18, 8 + x * 18));
            }
        }
        addSlotToContainer(new SlotSewingRequirements(Sewing, 0, 95, 17, this)); // needle
        addSlotToContainer(new SlotSewingRequirements(Sewing, 1, 95, 54, this)); // string
        addSlotToContainer(new SlotSewing(inventory.player, craftMatrix, craftResult, sewing, this, 0, 145, 35));
        onCraftMatrixChanged(craftMatrix);
    }

    public void onInventoryChanged()
    {
        craftResult.setInventorySlotContents(0, null);
        onCraftMatrixChanged(craftMatrix);
    }

    @Override
    public void onCraftMatrixChanged(IInventory par1IInventory)
    {
        super.onCraftMatrixChanged(par1IInventory);
        if (Sewing.getStackInSlot(0) != null && Sewing.getStackInSlot(0).getItem() == ItemGeneric.getItemStackFromName("Bone Needle").getItem()
                && Sewing.getStackInSlot(1) != null && Sewing.getStackInSlot(1).getItem() == Items.string)
        {
            craftResult.setInventorySlotContents(0, Necromancy.instance.sewingRecipeHandler.findMatchingRecipe(craftMatrix, worldObj));
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
        return worldObj.getBlock(posX, posY, posZ) == RegistryBlocksNecromancy.sewing
                ? par1EntityPlayer.getDistanceSq(posX + 0.5D, posY + 0.5D, posZ + 0.5D) <= 64D : false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int par1)
    {
        ItemStack var2 = null;
        Slot var3 = (Slot) inventorySlots.get(par1);
        if (var3 != null && var3.getHasStack())
        {
            ItemStack var4 = var3.getStack();
            var2 = var4.copy();
            if (par1 >= 36 && par1 <= 54)
            {
                if (!mergeItemStack(var4, 0, 35, false))
                    return null;
            }
            else if (par1 >= 0 && par1 < 36 && !mergeItemStack(var4, 36, 51, false))
                return null;
            if (var4.stackSize == 0)
            {
                var3.putStack((ItemStack) null);
            }
            else
            {
                var3.onSlotChanged();
            }
            if (var4.stackSize == var2.stackSize)
                return null;
            var3.onPickupFromSlot(player, var4);
        }
        return var2;
    }

}
