package ic2.advancedmachines.common;

import java.util.*;
import net.minecraft.src.*;
import ic2.api.*;

public class ContainerCentrifugeExtractor extends Container
{
    public TileEntityCentrifugeExtractor tileentity;
    public int progress = 0;
    public int energy = 0;
    public int speed = 0;

    public ContainerCentrifugeExtractor(InventoryPlayer var1, TileEntityCentrifugeExtractor var2)
    {
        this.tileentity = var2;
        this.addSlotToContainer(new Slot(var2, 0, 56, 17));
        this.addSlotToContainer(new Slot(var2, 1, 56, 53));
        this.addSlotToContainer(new SlotFurnace(var1.player, var2, 2, 115, 17));
        this.addSlotToContainer(new SlotFurnace(var1.player, var2, 3, 115, 35));
        this.addSlotToContainer(new SlotFurnace(var1.player, var2, 4, 115, 53)); //Re-added the third slot.
        this.addSlotToContainer(new Slot(var2, 5, 152, 6));
        this.addSlotToContainer(new Slot(var2, 6, 152, 24));
        this.addSlotToContainer(new Slot(var2, 7, 152, 42));
        this.addSlotToContainer(new Slot(var2, 8, 152, 60));

        int var3;
        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.addSlotToContainer(new Slot(var1, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.addSlotToContainer(new Slot(var1, var3, 8 + var3 * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
    {
        ItemStack tempStack = null;
        Slot localslot = (Slot)this.inventorySlots.get(slot);
        if (localslot != null && localslot.getHasStack())
        {
            ItemStack localstack = localslot.getStack();
            tempStack = localstack.copy();
            if (slot < 9)
            {
                this.mergeItemStack(localstack, 9, 38, false);
            }
            else
            {
            	if(localstack.itemID == AdvancedMachines.overClockerStack.itemID
            	|| localstack.itemID == AdvancedMachines.transformerStack.itemID
            	|| localstack.itemID == AdvancedMachines.energyStorageUpgradeStack.itemID)
            	{
            		this.mergeItemStack(localstack, 5, 8, false);
            	}
            	else
            	{
            		this.mergeItemStack(localstack, 0, 1, false);
            	}
            }

            if (localstack.stackSize == 0)
            {
                localslot.putStack((ItemStack)null);
            }
            else
            {
                localslot.onSlotChanged();
            }

            if (localstack.stackSize == tempStack.stackSize)
            {
                return null;
            }

            localslot.putStack(localstack);
        }

        return tempStack;
    }

    @Override
    public void updateCraftingResults()
    {
        super.updateCraftingResults();

        for (int var1 = 0; var1 < this.crafters.size(); ++var1)
        {
            ICrafting var2 = (ICrafting)this.crafters.get(var1);
            if (this.progress != this.tileentity.progress)
            {
                var2.updateCraftingInventoryInfo(this, 0, this.tileentity.progress);
            }

            if (this.energy != this.tileentity.energy)
            {
                var2.updateCraftingInventoryInfo(this, 1, this.tileentity.energy & '\uffff');
                var2.updateCraftingInventoryInfo(this, 2, this.tileentity.energy >>> 16);
            }

            if (this.speed != this.tileentity.speed)
            {
                var2.updateCraftingInventoryInfo(this, 3, this.tileentity.speed);
            }
        }

        this.progress = this.tileentity.progress;
        this.energy = this.tileentity.energy;
        this.speed = this.tileentity.speed;
    }

    @Override
    public void updateProgressBar(int var1, int var2)
    {
        switch (var1)
        {
            case 0:
                this.tileentity.progress = (short)var2;
                break;
            case 1:
                this.tileentity.energy = this.tileentity.energy & -65536 | var2;
                break;
            case 2:
                this.tileentity.energy = this.tileentity.energy & '\uffff' | var2 << 16;
                break;
            case 3:
                this.tileentity.speed = (short)var2;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1)
    {
        return this.tileentity.isUseableByPlayer(var1);
    }

    /* gone?
    @Override
    public int guiInventorySize()
    {
        return 9;
    }

    @Override
    public int getInput()
    {
        return 0;
    }
    */
}
