package atomicstryker.findercompass.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChunkCoordinates;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(dataIn);
        
        if (packetType == 0)
        {
            AS_FinderCompass.serverHasFinderCompass = true;
            FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage("Server Handshake complete - Finder Compass Stronghold Needle enabled.");            
        }
        else if (packetType == 1)
        {
            Class[] decodeAs = {Integer.class, Integer.class, Integer.class};
            Object[] packetData = ForgePacketWrapper.readPacketData(dataIn, decodeAs);
            
            AS_FinderCompass.strongholdCoords = new ChunkCoordinates((Integer)packetData[0], (Integer)packetData[1], (Integer)packetData[2]);
            AS_FinderCompass.hasStronghold = true;
        }
    }
    
    public static void onClientLoggedInToServer()
    {
        AS_FinderCompass.serverHasFinderCompass = false;
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("FindrCmps", 0, null));
    }
}
