package atomicstryker.magicyarn.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.PacketWrapper;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = PacketWrapper.readPacketID(data);
        if (packetType == 1)
        {
            MagicYarnClient.instance.onServerAnsweredChallenge();
        }
    }

}
