package atomicstryker.kenshiro.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.kenshiro.client.KenshiroClient;
import atomicstryker.kenshiro.common.KenshiroServer;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;

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
            KenshiroClient.instance().onEntityPunched(entID);
        }
        else
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
            if (p != null)
            {
                KenshiroServer.instance().onClientPunchedEntity(p, p.worldObj, entID);
            }
        }
    }

}