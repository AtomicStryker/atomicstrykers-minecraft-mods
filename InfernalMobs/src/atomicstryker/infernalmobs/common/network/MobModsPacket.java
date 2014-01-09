package atomicstryker.infernalmobs.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class MobModsPacket implements IPacket
{
    
    private String stringData;
    private int entID;
    
    public MobModsPacket() {}
    
    public MobModsPacket(String u, int i)
    {
        stringData = u;
        entID = i;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeShort(stringData.length());
        for (char c : stringData.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(entID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        stringData = String.valueOf(chars);
        entID = bytes.readInt();
        
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
                        stringData = mod.getLinkedModNameUntranslated();
                        InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(this, p);
                        InfernalMobsCore.instance().sendHealthPacket(e, mod.getActualHealth(e));
                    }
                }
            }
        }
        else // server answer being parsed
        {
            InfernalMobsCore.instance().addRemoteEntityModifiers(FMLClientHandler.instance().getClient().theWorld, entID, stringData);
        }
    }

}
