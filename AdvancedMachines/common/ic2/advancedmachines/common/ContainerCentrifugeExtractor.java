package ic2.advancedmachines.common;

import ic2.api.IElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

public class ContainerCentrifugeExtractor extends Container
{
    public TileEntityCentrifugeExtractor tileentity;
    public int progress = 0;
    public int energy = 0;
    public int speed = 0;

    public ContainerCentrifugeExtractor(InventoryPlayer inv, TileEntityCentrifugeExtractor tE)
    {
        this.tileentity = tE;
        this.addSlotToContainer(new Slot(tE, 0, 56, 53));
        this.addSlotToContainer(new Slot(tE, 1, 56, 17));
        this.addSlotToContainer(new SlotFurnace(inv.player, tE, 2, 115, 17));
        this.addSlotToContainer(new SlotFurnace(inv.player, tE, 3, 115, 35));
        this.addSlotToContainer(new SlotFurnace(inv.player, tE, 4, 115, 53)); //Re-added the third slot.
        this.addSlotToContainer(new Slot(tE, 5, 152, 6));
        this.addSlotToContainer(new Slot(tE, 6, 152, 24));
        this.addSlotToContainer(new Slot(tE, 7, 152, 42));
        this.addSlotToContainer(new Slot(tE, 8, 152, 60));

        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(inv, i, 8 + i * 18, 142));
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
                else if (localstack.getItem() instanceof IElectricItem)
                {
                    if (((Slot) inventorySlots.get(0)).getStack() == null)
                    {
                        ((Slot) inventorySlots.get(0)).putStack(localstack);
                        localslot.putStack((ItemStack)null);
                    }
                }
            	else
            	{
            		this.mergeItemStack(localstack, 1, 2, false);
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

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting var2 = (ICrafting)this.crafters.get(i);
            if (this.progress != this.tileentity.progress)
            {
                var2.sendProgressBarUpdate(this, 0, this.tileentity.progress);
            }

            if (this.energy != this.tileentity.energy)
            {
                var2.sendProgressBarUpdate(this, 1, this.tileentity.energy & '\uffff');
                var2.sendProgressBarUpdate(this, 2, this.tileentity.energy >>> 16);
            }

            if (this.speed != this.tileentity.speed)
            {
                var2.sendProgressBarUpdate(this, 3, this.tileentity.speed);
            }
        }

        this.progress = this.tileentity.progress;
        this.energy = this.tileentity.energy;
        this.speed = this.tileentity.speed;
    }

    @Override
    public void updateProgressBar(int key, int value)
    {
        switch (key)
        {
            case 0:
                this.tileentity.progress = (short)value;
                break;
            case 1:
                this.tileentity.energy = this.tileentity.energy & -65536 | value;
                break;
            case 2:
                this.tileentity.energy = this.tileentity.energy & '\uffff' | value << 16;
                break;
            case 3:
                this.tileentity.speed = (short)value;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1)
    {
        return this.tileentity.isUseableByPlayer(var1);
    }
}
