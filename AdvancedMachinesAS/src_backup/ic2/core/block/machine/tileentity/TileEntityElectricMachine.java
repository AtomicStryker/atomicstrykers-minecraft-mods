package ic2.core.block.machine.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.TileEntityInventory;

public abstract class TileEntityElectricMachine extends TileEntityInventory
{
    
    public TileEntityElectricMachine(EntityPlayer entityPlayer, TileEntityElectricMachine base, int height, int dischargeX, int dischargeY)
    {
        super(entityPlayer, base, height, dischargeX, dischargeY);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getInvName()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public boolean isRedstonePowered()
    {
        return false;
    }
    
    public final float getChargeLevel() {
        return 0;
    }

}
