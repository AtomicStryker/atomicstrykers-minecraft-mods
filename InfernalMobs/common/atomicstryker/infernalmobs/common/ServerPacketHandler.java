package atomicstryker.infernalmobs.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{    
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
            EntityLiving ent = (EntityLiving) p.worldObj.getEntityByID(entID);
            if (ent != null)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers(ent);
                if (mod != null)
                {
                    /* answer: Packet ID 1, from server, { int entID, String mods } */
                    Object[] toSend = {entID, mod.getLinkedModName()};
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
            EntityLiving ent = (EntityLiving) p.worldObj.getEntityByID(entID);
            if (ent != null)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers(ent);
                if (mod != null)
                {
                    /* answer: Packet ID 4, from server, { int entityID, int health } */
                    Object[] toSend = { ent.entityId, ent.getHealth() };
                    PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_IM", 4, toSend), player);
                }
            }
        }
    }
}
