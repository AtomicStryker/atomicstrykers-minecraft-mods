package atomicstryker.ropesplus.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.ForgePacketWrapper;
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
        
        //System.out.println("client got packet of id "+packetID);
        
        if (packetID == 2) // server tells client he has a grappling hook out
        {
        	RopesPlusClient.grapplingHookOut = true;
        }
        else if (packetID == 3) // server tells client he has a grappling hook out no longer
        {
        	RopesPlusClient.grapplingHookOut = false;
        }
	}

}
