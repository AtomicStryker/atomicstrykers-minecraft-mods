package atomicstryker.multimine.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import atomicstryker.network.ForgePacketWrapper;
import atomicstryker.network.PacketDispatcher.IPacketHandler;
import atomicstryker.network.WrappedPacket;

public class ServerPacketHandler implements IPacketHandler
{
    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
    {
        ByteBuf data = packet.data;

        if (packetType == 0) // client informs server he has multi mine installed!
        {
            
        }
        else if (packetType == 1) // partial block packet! argument ints: x,y,z,dimension
        {
            Class[] decodeAs = { Integer.class, Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

            MultiMineServer.instance().onClientSentPartialBlockPacket((EntityPlayerMP) player,
                    (Integer) packetReadout[0], (Integer) packetReadout[1], (Integer) packetReadout[2], (Integer) packetReadout[3]);
        }
    }
}
