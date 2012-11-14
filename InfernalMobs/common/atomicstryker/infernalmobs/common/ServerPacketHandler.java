package atomicstryker.infernalmobs.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.ForgePacketWrapper;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{
    private EntityLiving getEntityFromID(World world, int ID)
    {
        for (Object o : world.loadedEntityList)
        {
            if (o instanceof EntityLiving)
            {
                if (((EntityLiving)o).entityId == ID)
                {
                    return (EntityLiving) o;
                }
            }
        }
        return null;
    }
    
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(data);
        
        if (packetType == 1) // question: Packet ID 1, from client, { entID }
        {
            Class[] decodeAs = {Integer.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            int entID = (Integer) packetReadout[0];
            
            EntityPlayer p = (EntityPlayer) player;
            EntityLiving ent = getEntityFromID(p.worldObj, entID);
            
            if (ent != null)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers(ent);
                if (mod != null)
                {
                    /* answer: Packet ID 1, from server, { int entID, String mods } */
                    Object[] toSend = {entID, mod.getModName()};
                    PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_IM", 1, toSend), player);
                    
                    InfernalMobsCore.instance().sendHealthPacket(ent, mod.getActualHealth());
                }
            }
        }
        else if (packetType == 4) // question: Packet ID 4, from client, { entID }
        {
            Class[] decodeAs = {Integer.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            int entID = (Integer) packetReadout[0];
            
            EntityPlayer p = (EntityPlayer) player;
            EntityLiving ent = getEntityFromID(p.worldObj, entID);
            
            if (ent != null)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers(ent);
                if (mod != null)
                {
                    /* answer: Packet ID 4, from server, { int entityID, int health } */                    
                    InfernalMobsCore.instance().sendHealthPacket(ent, ent.getHealth());
                }
            }
        }
    }
}
