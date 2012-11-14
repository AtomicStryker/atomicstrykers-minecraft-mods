package atomicstryker.minefactoryreloaded.common.core;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public interface IMFRProxy
{
    public void load();
    
	public void movePlayerToCoordinates(EntityPlayer e, double x, double y, double z);
	
	public int getRenderId();
}
