package ic2.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public abstract class ContainerBase extends Container
{

    public ContainerBase(IInventory base)
    {
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void detectAndSendChanges()
    {
        // TODO Auto-generated method stub
        super.detectAndSendChanges();
    }

}
