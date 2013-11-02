package ic2.core.block.machine;

import ic2.core.ContainerFullInv;
import net.minecraft.entity.player.EntityPlayer;

public abstract class ContainerElectricMachine extends ContainerFullInv
{

    public ContainerElectricMachine(EntityPlayer entityPlayer, ic2.core.block.machine.tileentity.TileEntityElectricMachine base, int height, int dischargeX, int dischargeY)
    {
      super(entityPlayer, base, height);
        // TODO Auto-generated constructor stub
    }

}
