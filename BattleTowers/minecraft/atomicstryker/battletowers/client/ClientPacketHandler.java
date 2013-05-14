package atomicstryker.battletowers.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.ForgePacketWrapper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{

    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));

        int packetID = ForgePacketWrapper.readPacketID(data);

        if (packetID == 1)
        {
            Class[] decodeAs = { Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);

            AS_BattleTowersCore.towerDestroyerEnabled = (Integer) packetReadout[0];
        }
    }

}
