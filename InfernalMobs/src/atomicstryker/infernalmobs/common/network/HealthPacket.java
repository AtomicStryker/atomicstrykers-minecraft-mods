package atomicstryker.infernalmobs.common.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;

public class HealthPacket implements IPacket
{
    
    private String stringData;
    private int entID;
    private float health;
    private float maxhealth;
    
    public HealthPacket() {}
    
    public HealthPacket(String u, int i, float f1, float f2)
    {
        stringData = u;
        entID = i;
        health = f1;
        maxhealth = f2;
    }

    @Override
    public void writeBytes(ByteBuf bytes)
    {
        bytes.writeShort(stringData.length());
        for (char c : stringData.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(entID);
        bytes.writeFloat(health);
        bytes.writeFloat(maxhealth);
    }

    @Override
    public void readBytes(ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        stringData = String.valueOf(chars);
        entID = bytes.readInt();
        health = bytes.readFloat();
        maxhealth = bytes.readFloat();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) // client request being parsed
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(stringData);
            if (p != null)
            {
                EntityLivingBase e = (EntityLivingBase) p.worldObj.getEntityByID(entID);
                if (e != null)
                {
                    MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                    if (mod != null)
                    {
                        health = e.getHealth();
                        maxhealth = e.getMaxHealth();
                        InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(this, p);
                    }
                }
            }
        }
        else
        {
            Entity ent = FMLClientHandler.instance().getClient().theWorld.getEntityByID(entID);
            if (ent != null && ent instanceof EntityLivingBase)
            {
                MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent);
                if (mod != null)
                {
                    mod.setActualHealth(health, maxhealth);
                }
            }
        }
    }

}
