package atomicstryker.infernalmobs.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;

public class MobModsPacket implements IPacket
{
    
    private String stringData;
    private int entID;
    private byte sentFromServer;
    
    public MobModsPacket() {}
    
    public MobModsPacket(String str, int i, byte ir)
    {
        stringData = str;
        entID = i;
        sentFromServer = ir;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeByte(sentFromServer);
        bytes.writeShort(stringData.length());
        for (char c : stringData.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(entID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        sentFromServer = bytes.readByte();
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        stringData = String.valueOf(chars);
        entID = bytes.readInt();
        
        if (sentFromServer != 0)
        {
            // so we are on client now
            InfernalMobsCore.proxy.onMobModsPacketToClient(stringData, entID);
        }
        else
        {
            // else we are on serverside
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
                        stringData = mod.getLinkedModNameUntranslated();
                        InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new MobModsPacket(stringData, entID, (byte)1), p);
                        InfernalMobsCore.instance().sendHealthPacket(e, mod.getActualHealth(e));
                    }
                }
            }
        }
    }

}
