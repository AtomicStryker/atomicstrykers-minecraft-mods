package atomicstryker.netherores.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.netherores.common.ForgePacketWrapper;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class NetherOresClient implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetID = ForgePacketWrapper.readPacketID(data);
        
        if (packetID == 1)
        {
            Class[] decodeAs = {Integer.class, Integer.class, Integer.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            int x = (Integer) packetReadout[0];
            int y = (Integer) packetReadout[1];
            int z = (Integer) packetReadout[2];
            
            FMLClientHandler.instance().getClient().theWorld.playSound(x + 0.5, y + 0.5, z + 0.5, "random.fuse", 1.0F, 1.0F);
        }
    }
}
