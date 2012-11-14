package atomicstryker.kenshiro.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet18Animation;
import net.minecraft.src.Packet250CustomPayload;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.FMLCommonHandler;
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
        EntityPlayer playerEnt = (EntityPlayer)player;
        
        if (packetType == PacketType.HANDSHAKE.ordinal())
        {
        	PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("AS_KSM", PacketType.HANDSHAKE.ordinal(), null), player);
        	KenshiroServer.instance().setClientHasKenshiroInstalled(player, false);
        }
        else if (packetType == PacketType.BLOCKPUNCHED.ordinal())
        {
            Class[] decodeAs = { Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
        	KenshiroServer.instance().onClientPunchedBlock(playerEnt, (Integer)packetReadout[0], (Integer)packetReadout[1], (Integer)packetReadout[2]);
        }
        else if (packetType == PacketType.ENTITYPUNCHED.ordinal())
        {
            Class[] decodeAs = { Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            int entityID = (Integer)packetReadout[0];
            KenshiroServer.instance().onClientPunchedEntity(player, FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(playerEnt.dimension), entityID);
        }
        else if (packetType == PacketType.KENSHIROSTARTED.ordinal())
        {
            KenshiroServer.instance().onClientUnleashedKenshiroVolley(playerEnt);
        }
        else if (packetType == PacketType.KENSHIROENDED.ordinal())
        {
            KenshiroServer.instance().onClientFinishedKenshiroVolley(playerEnt);
        }
        else if (packetType == PacketType.ENTITYKICKED.ordinal())
        {
            Class[] decodeAs = { Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            Entity target = KenshiroMod.instance().getEntityByID(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(playerEnt.dimension), (Integer)packetReadout[1]);
            if (target != null
            && target instanceof EntityLiving)
            {
                KenshiroServer.instance().onClientKickedEntity(playerEnt, (EntityLiving) target);
            }
        }
        else if (packetType == PacketType.SOUNDEFFECT.ordinal())
        {
            Class[] decodeAs = { String.class, Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            String sound = (String) packetReadout[0];
            int x = (Integer)packetReadout[1];
            int y = (Integer)packetReadout[2];
            int z = (Integer)packetReadout[3];
            
            Object[] toSend = {sound, x, y, z};
            Packet250CustomPayload packetNew = ForgePacketWrapper.createPacket("AS_KSM", PacketType.SOUNDEFFECT.ordinal(), toSend);
            
            PacketDispatcher.sendPacketToAllAround(x, y, z, 30D, playerEnt.dimension, packetNew);
        }
        else if (packetType == PacketType.ANIMATION.ordinal())
        {
            Class[] decodeAs = { Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            Packet18Animation animpacket = new Packet18Animation(playerEnt, (Integer)packetReadout[0]);
            PacketDispatcher.sendPacketToAllAround(playerEnt.posX, playerEnt.posY, playerEnt.posZ, 30D, playerEnt.dimension, animpacket);
        }
    }

}
