package atomicstryker.battletowers.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.ForgePacketWrapper;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{

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
