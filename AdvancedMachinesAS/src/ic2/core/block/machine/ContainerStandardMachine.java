package ic2.core.block.machine;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerStandardMachine extends ContainerElectricMachine
{
    
    public final ic2.core.block.machine.tileentity.TileEntityStandardMachine tileEntity;

    public ContainerStandardMachine(net.minecraft.entity.player.EntityPlayer entityPlayer, ic2.core.block.machine.tileentity.TileEntityStandardMachine tileEntity)
    {
        this(entityPlayer, tileEntity, 166, 56, 53, 56, 17, 116, 35, 152, 8);
    }

    public ContainerStandardMachine(net.minecraft.entity.player.EntityPlayer entityPlayer, ic2.core.block.machine.tileentity.TileEntityStandardMachine te, int height, int dischargeX, int dischargeY, int inputX, int inputY, int outputX, int outputY, int upgradeX, int upgradeY)
    {
        super(entityPlayer, te, height, dischargeX, dischargeY);
        tileEntity = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
