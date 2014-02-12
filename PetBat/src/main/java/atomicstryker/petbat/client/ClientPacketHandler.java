package atomicstryker.petbat.client;

import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.network.PacketDispatcher.IPacketHandler;
import atomicstryker.network.WrappedPacket;

public class ClientPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
    {
        //DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        //int packetType = ForgePacketWrapper.readPacketID(data);
    }

}
