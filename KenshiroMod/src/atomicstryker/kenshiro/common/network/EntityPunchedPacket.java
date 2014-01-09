package atomicstryker.kenshiro.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.kenshiro.client.KenshiroClient;
import atomicstryker.kenshiro.common.KenshiroServer;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class EntityPunchedPacket implements IPacket
{
    
    private String username;
    private int entID;
    
    public EntityPunchedPacket() {}
    
    public EntityPunchedPacket(int i)
    {
        username = "";
        entID = i;
    }
    
    public EntityPunchedPacket(String u, int i)
    {
        username = u;
        entID = i;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeShort(username.length());
        for (char c : username.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(entID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        username = String.valueOf(chars);
        entID = bytes.readInt();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            Entity e = FMLClientHandler.instance().getClient().theWorld.getEntityByID(entID);
            if (e != null)
            {
                KenshiroClient.instance().onEntityPunched((EntityLivingBase) e);
            }
        }
        else
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
            if (p != null)
            {
                KenshiroServer.instance().onClientPunchedEntity(p, p.worldObj, entID);
            }
        }
    }

}