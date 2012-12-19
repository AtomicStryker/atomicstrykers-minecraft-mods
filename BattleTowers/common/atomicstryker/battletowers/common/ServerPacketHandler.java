package atomicstryker.battletowers.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        
        int packetID = ForgePacketWrapper.readPacketID(data);
        
        if (packetID == 2)
        {
            System.out.println("Server received packet: Client is hammering away at a BattleTower chest!");
            // client to server: client is hacking away at a chest with a Golem nearby
            
            Class[] decodeAs = {Integer.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            int golemID = (Integer) packetReadout[0];
            Entity golem = null;
            World world = ((EntityPlayer)player).worldObj;
            for (Object ent : world.loadedEntityList)
            {
                if (((Entity)ent).entityId == golemID)
                {
                    golem = (Entity)ent;
                    break;
                }
            }
            
            if (golem != null
            && golem instanceof AS_EntityGolem)
            {
                AS_EntityGolem g = (AS_EntityGolem) golem;
                System.out.println("Found BattleTower chest golem, waking!");
                g.setAwake();
                g.setTarget((EntityPlayer) player);
            }
        }
    }

}
