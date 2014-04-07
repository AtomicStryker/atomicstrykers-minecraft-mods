package com.sirolf2009.necromancy.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;
import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;

public class ContainerAltar extends Container
{
    
    private TileEntityAltar altar;
    private World worldObj;
    private int posX;
    private int posY;
    private int posZ;

    public ContainerAltar(InventoryPlayer inventory, com.sirolf2009.necromancy.tileentity.TileEntityAltar tileEntityAltar)
    {
        worldObj = tileEntityAltar.getWorldObj();
        posX = tileEntityAltar.xCoord;
        posY = tileEntityAltar.yCoord;
        posZ = tileEntityAltar.zCoord;
        altar = tileEntityAltar;
        addSlotToContainer(new Slot(altar, 0, 26, 40)); // blood
        addSlotToContainer(new Slot(altar, 1, 134, 39)); // soul
        addSlotToContainer(new Slot(altar, 2, 80, 19)); // head
        addSlotToContainer(new Slot(altar, 3, 80, 36)); // body
        addSlotToContainer(new Slot(altar, 4, 80, 53)); // legs
        addSlotToContainer(new Slot(altar, 5, 63, 36)); // right-arm
        addSlotToContainer(new Slot(altar, 6, 97, 36)); // left-arm
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
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return worldObj.getBlock(posX, posY, posZ) == RegistryBlocksNecromancy.altar
                ? par1EntityPlayer.getDistanceSq(posX + 0.5D, posY + 0.5D, posZ + 0.5D) <= 64D : false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1)
    {
        ItemStack var2 = null;
        Slot var3 = (Slot) inventorySlots.get(par1);
        if (var3 != null && var3.getHasStack())
        {
            ItemStack var4 = var3.getStack();
            var2 = var4.copy();
            if (par1 <= 6)
            {
                if (!mergeItemStack(var4, 7, 43, false))
                    return null;
            }
            else if (var4.getItem() instanceof ItemGeneric && var4.getItemDamage() == 5)
            {
                if (!mergeItemStack(var4, 1, 2, true))
                    return null;
            }
            else if (var4.getItem() instanceof ItemGeneric && var4.getItemDamage() == 6)
                if (!mergeItemStack(var4, 0, 1, true))
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
            var3.onPickupFromSlot(par1EntityPlayer, var4);
        }
        return var2;
    }
    
}
