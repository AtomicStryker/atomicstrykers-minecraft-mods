package atomicstryker.magicyarn.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import atomicstryker.PacketWrapper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = PacketWrapper.readPacketID(data);
        
        if (packetType == 1)
        {
            PacketDispatcher.sendPacketToPlayer(PacketWrapper.createPacket("MagicYarn", 1, null), player);
        }
        else if (packetType == 2)
        {
            PacketDispatcher.sendPacketToAllInDimension(packet, ((EntityPlayer)player).dimension);
        }
        else if (packetType == 3)
        {
            PacketDispatcher.sendPacketToAllPlayers(packet);
        }
    }

}
