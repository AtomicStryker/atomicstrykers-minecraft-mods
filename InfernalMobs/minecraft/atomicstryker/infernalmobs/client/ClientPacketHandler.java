package atomicstryker.infernalmobs.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(data);
        
        /* answer: Packet ID 1, from server, { int entID, String mods } */
        if (packetType == 1)
        {
            Class[] decodeAs = {Integer.class, String.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            int entID = (Integer) packetReadout[0];
            String mods = (String) packetReadout[1];
            
            InfernalMobsCore.addRemoteEntityModifiers(FMLClientHandler.instance().getClient().theWorld, entID, mods);
        }
        // addVelocity player: Packet ID 2, from server, { double xVel, double yVel, double zVel }
        else if (packetType == 2)
        {
            Class[] decodeAs = {Double.class, Double.class, Double.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            FMLClientHandler.instance().getClient().thePlayer.addVelocity((Double)packetReadout[0], (Double)packetReadout[1], (Double)packetReadout[2]);
        }
        // knockBack player: Packet ID 3, from server, { double xVel, double zVel }
        else if (packetType == 3)
        {
            Class[] decodeAs = {Double.class, Double.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            MM_Gravity.knockBack(FMLClientHandler.instance().getClient().thePlayer, (Double)packetReadout[0], (Double)packetReadout[1]);
        }
        // health announcement: Packet ID 4, from server, { int entityID, int health }
        else if (packetType == 4)
        {
            Class[] decodeAs = {Integer.class, Integer.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            Entity ent = FMLClientHandler.instance().getClient().theWorld.getEntityByID((Integer)packetReadout[0]);
            if (ent != null && ent instanceof EntityLiving)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLiving) ent);
                if (mod != null)
                {
                    mod.setActualHealth((Integer)packetReadout[1]);
                }
            }
        }
    }
}
