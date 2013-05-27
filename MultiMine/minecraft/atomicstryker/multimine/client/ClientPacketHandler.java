package atomicstryker.multimine.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{
    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(data);
        
        if (packetType == 0) // answering packet from server, be assured it has Multi Mine installed aswell
        {
            MultiMineClient.instance().setEnabledOnServer();
        }
        else if (packetType == 1) // partial block packet! argument ints: x,y,z,progress
        {
            Class[] decodeAs = { Integer.class, Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            MultiMineClient.instance().onServerSentPartialBlockData((Integer)packetReadout[0], (Integer)packetReadout[1], (Integer)packetReadout[2], (Integer)packetReadout[3]);
        }
        else if (packetType == 2) // partial block removal packet! argument ints: x,y,z
        {
            Class[] decodeAs = { Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            MultiMineClient.instance().onServerSentPartialBlockDeleteCommand((Integer)packetReadout[0], (Integer)packetReadout[1], (Integer)packetReadout[2]);
        }
        else if (packetType == 3) // excluded Block ID list coming from server! argument String excludedBlocksString
        {
            Class[] decodeAs = { String.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            MultiMineClient.instance().onServerSentExcludedBlocksList((String)packetReadout[0]);
        }
        else if (packetType == 4) // excluded Item ID list coming from server! argument String excludedItemsString
        {
            Class[] decodeAs = { String.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            MultiMineClient.instance().onServerSentExcludedItemsList((String)packetReadout[0]);
        }
    }
}
