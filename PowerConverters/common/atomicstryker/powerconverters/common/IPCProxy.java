package atomicstryker.powerconverters.common;

import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public interface IPCProxy
{
	public String getConfigPath();
	
	public void sendPacketToAll(Packet packet);
	
	public boolean isClient(World world);
	
	public boolean isServer();
	
	public Packet getTileEntityPacket(TileEntity te, int liqInt);
	
	public void sendPacket(Packet packet);
}
