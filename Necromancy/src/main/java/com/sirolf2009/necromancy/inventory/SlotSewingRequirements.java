package com.sirolf2009.necromancy.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotSewingRequirements extends Slot
{
    ContainerSewing machine;

    public SlotSewingRequirements(IInventory par1IInventory, int par2, int par3, int par4, Container container)
    {
        super(par1IInventory, par2, par3, par4);
        machine = (ContainerSewing) container;
    }

    @Override
    public void onSlotChanged()
    {
        super.onSlotChanged();
        machine.onInventoryChanged();
    }
}
