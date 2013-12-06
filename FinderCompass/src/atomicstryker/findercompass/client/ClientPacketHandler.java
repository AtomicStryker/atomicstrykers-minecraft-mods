package atomicstryker.findercompass.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChunkCoordinates;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.findercompass.common.FinderCompassMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{
    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(dataIn);
        
        if (packetType == 0)
        {
            FinderCompassLogic.serverHasFinderCompass = true;
            FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage("Server Handshake complete - Finder Compass Stronghold Needle enabled.");            
        }
        else if (packetType == 1)
        {
            Class[] decodeAs = {Integer.class, Integer.class, Integer.class};
            Object[] packetData = ForgePacketWrapper.readPacketData(dataIn, decodeAs);
            
            FinderCompassLogic.strongholdCoords = new ChunkCoordinates((Integer)packetData[0], (Integer)packetData[1], (Integer)packetData[2]);
            //System.out.printf("Finder Compass server sent Stronghold coords: [%d|%d|%d]\n", (Integer)packetData[0], (Integer)packetData[1], (Integer)packetData[2]);
            FinderCompassLogic.hasStronghold = true;
        }
        else if (packetType == 2)
        {
            try
            {
                FinderCompassMod.instance.itemEnabled = (dataIn.readInt() == 1);
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("Finder Compass Item enabled: "+FinderCompassMod.instance.itemEnabled);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            FinderCompassClientTicker.instance.inputOverrideConfig(dataIn);
        }
    }
    
    public static void onClientLoggedInToServer()
    {
        FinderCompassLogic.serverHasFinderCompass = false;
        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("FindrCmps", 0, null));
    }
}
