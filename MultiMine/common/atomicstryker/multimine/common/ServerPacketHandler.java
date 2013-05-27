package atomicstryker.multimine.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{
    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(data);

        if (packetType == 0) // client informs server he has multi mine installed!
        {
            // answer in kind, to enable clientside functionality
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_MM", 0, null), player);
            
            MultiMineServer.instance().onPlayerLoggedIn(player);
        }
        else if (packetType == 1) // partial block packet! argument ints: x,y,z,dimension
        {
            Class[] decodeAs = { Integer.class, Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

            MultiMineServer.instance().onClientSentPartialBlockPacket((EntityPlayer) player,
                    (Integer) packetReadout[0], (Integer) packetReadout[1], (Integer) packetReadout[2], (Integer) packetReadout[3]);
        }
    }
}
