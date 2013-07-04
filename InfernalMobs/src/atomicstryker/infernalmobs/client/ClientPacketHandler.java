package atomicstryker.infernalmobs.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import cpw.mods.fml.client.FMLClientHandler;
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
        
        /* answer: Packet ID 1, from server, { int entID, String mods } */
        if (packetType == 1)
        {
            Class[] decodeAs = {Integer.class, String.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            int entID = (Integer) packetReadout[0];
            String mods = (String) packetReadout[1];
            
            InfernalMobsCore.instance().addRemoteEntityModifiers(FMLClientHandler.instance().getClient().theWorld, entID, mods);
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
        // health announcement: Packet ID 4, from server, { int entityID, float health }
        else if (packetType == 4)
        {
            Class[] decodeAs = {Integer.class, Float.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            Entity ent = FMLClientHandler.instance().getClient().theWorld.getEntityByID((Integer)packetReadout[0]);
            if (ent != null && ent instanceof EntityLivingBase)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent);
                if (mod != null)
                {
                    //System.out.println("Client updating health of modMob "+ent+" to: "+(Integer)packetReadout[1]);
                    mod.setActualHealth((Float)packetReadout[1]);
                }
            }
        }
    }
}
