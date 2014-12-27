package atomicstryker.infernalmobs.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
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
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
    	ByteBufUtils.writeUTF8String(bytes, stringData);
        bytes.writeInt(entID);
        bytes.writeFloat(health);
        bytes.writeFloat(maxhealth);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        stringData = ByteBufUtils.readUTF8String(bytes);
        entID = bytes.readInt();
        health = bytes.readFloat();
        maxhealth = bytes.readFloat();
        
        // client always sends packets with health = maxhealth = 0
        if (maxhealth > 0)
        {
            InfernalMobsCore.proxy.onHealthPacketForClient(stringData, entID, health, maxhealth);
        }
        else
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(stringData);
            if (p != null)
            {
                Entity ent = p.worldObj.getEntityByID(entID);
                if (ent != null && ent instanceof EntityLivingBase)
                {
                    EntityLivingBase e = (EntityLivingBase) ent;
                    MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                    if (mod != null)
                    {
                        health = e.getHealth();
                        maxhealth = e.getMaxHealth();
                        InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new HealthPacket(stringData, entID, health, maxhealth), p);
                    }
                }
            }
        }
    }

}
