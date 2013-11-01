package atomicstryker.ic2.advancedmachines;

import ic2.core.block.machine.ContainerStandardMachine;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;

public class ContainerAdvancedMachine extends ContainerStandardMachine
{
    
    private final IAdvancedMachine advM;
    private int speedNetworked;

    public ContainerAdvancedMachine(EntityPlayer entityPlayer, TileEntityStandardMachine tileEntity, IAdvancedMachine advMachine)
    {
        super(entityPlayer, tileEntity);
        advM = advMachine;
        speedNetworked = 0;
        
        // moving already existing slot coords is possible, thank goodness
        if (advMachine.getOutputSlots().size() == 2)
        {
            addSlotToContainer(new SlotInvSlot(advMachine.getOutputSlots().get(1), 0, 115, 53));
        }
        else if (advMachine.getOutputSlots().size() == 3)
        {
            addSlotToContainer(new SlotInvSlot(advMachine.getOutputSlots().get(1), 0, 115, 17));
            addSlotToContainer(new SlotInvSlot(advMachine.getOutputSlots().get(2), 0, 115, 53));
        }
    }
    
    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        
        if (speedNetworked != advM.getSpeed())
        {
            for (int index = 0; index < crafters.size(); ++index)
            {
                ICrafting crafter = (ICrafting)crafters.get(index);
                crafter.sendProgressBarUpdate(this, 3, advM.getSpeed());
            }
            speedNetworked = advM.getSpeed();
        }
    }
    
    @Override
    public void updateProgressBar(int index, int value)
    {
        if (index == 3)
        {
            advM.setClientSpeed(value);
        }
        else
        {
            super.updateProgressBar(index, value);
        }
    }

}
