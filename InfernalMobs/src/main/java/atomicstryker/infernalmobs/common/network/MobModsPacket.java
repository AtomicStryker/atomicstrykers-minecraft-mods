package atomicstryker.infernalmobs.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;

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
        
        InfernalMobsCore.proxy.onMobModsPacket(stringData, entID);
    }

}
