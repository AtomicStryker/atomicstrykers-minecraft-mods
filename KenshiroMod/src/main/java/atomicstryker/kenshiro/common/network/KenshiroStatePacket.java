package atomicstryker.kenshiro.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.kenshiro.common.KenshiroServer;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;

public class KenshiroStatePacket implements IPacket
{
    
    private String username;
    private boolean kenshirorunning;
    
    public KenshiroStatePacket() {}

    public KenshiroStatePacket(String s, boolean b)
    {
        username = s;
        kenshirorunning = b;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeShort(username.length());
        for (char c : username.toCharArray()) bytes.writeChar(c);
        bytes.writeBoolean(kenshirorunning);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        username = String.valueOf(chars);
        kenshirorunning = bytes.readBoolean();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
            if (p != null)
            {
                if (kenshirorunning)
                {
                    KenshiroServer.instance().onClientUnleashedKenshiroVolley(p);
                }
                else
                {
                    KenshiroServer.instance().onClientFinishedKenshiroVolley(p);
                }
            }
        }
    }

}