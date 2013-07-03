package ic2.advancedmachines.common;

import ic2.api.item.IElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

public class ContainerSingularityCompressor extends Container
{
    public TileEntitySingularityCompressor tileentity;
    public int progress = 0;
    public int energy = 0;
    public int PSI = 0;

    public ContainerSingularityCompressor(InventoryPlayer var1, TileEntitySingularityCompressor var2)
    {
        this.tileentity = var2;
        this.addSlotToContainer(new Slot(var2, 0, 56, 53));
        this.addSlotToContainer(new Slot(var2, 1, 56, 17));
        this.addSlotToContainer(new SlotFurnace(var1.player, var2, 2, 115, 35));
        this.addSlotToContainer(new Slot(var2, 3, 152, 6));
        this.addSlotToContainer(new Slot(var2, 4, 152, 24));
        this.addSlotToContainer(new Slot(var2, 5, 152, 42));
        this.addSlotToContainer(new Slot(var2, 6, 152, 60));

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
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int var1 = 0; var1 < this.crafters.size(); ++var1)
        {
            ICrafting var2 = (ICrafting)this.crafters.get(var1);
            if (this.progress != this.tileentity.progress)
            {
                var2.sendProgressBarUpdate(this, 0, this.tileentity.progress);
            }

            if (this.energy != this.tileentity.energy)
            {
                var2.sendProgressBarUpdate(this, 1, this.tileentity.energy & '\uffff');
                var2.sendProgressBarUpdate(this, 2, this.tileentity.energy >>> 16);
            }

            if (this.PSI != this.tileentity.speed)
            {
                var2.sendProgressBarUpdate(this, 3, this.tileentity.speed);
            }
        }

        this.progress = this.tileentity.progress;
        this.energy = this.tileentity.energy;
        this.PSI = this.tileentity.speed;
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
            		this.mergeItemStack(localstack, 3, 6, false);
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
    
}
