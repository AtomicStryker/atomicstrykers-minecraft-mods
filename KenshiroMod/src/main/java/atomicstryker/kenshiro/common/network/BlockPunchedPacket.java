package atomicstryker.kenshiro.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.kenshiro.common.KenshiroServer;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;

public class BlockPunchedPacket implements IPacket
{
    
    private String username;
    private int x, y, z;
    
    public BlockPunchedPacket() {}

    public BlockPunchedPacket(String s, int a, int b, int c)
    {
        username = s;
        x = a;
        y = b;
        z = c;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeShort(username.length());
        for (char c : username.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        username = String.valueOf(chars);
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
            if (p != null)
            {
                KenshiroServer.instance().onClientPunchedBlock(p, x, y, z);
            }
        }
    }

}