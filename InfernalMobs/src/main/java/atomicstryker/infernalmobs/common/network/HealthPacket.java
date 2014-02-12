package atomicstryker.infernalmobs.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
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
        bytes.writeShort(stringData.length());
        for (char c : stringData.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(entID);
        bytes.writeFloat(health);
        bytes.writeFloat(maxhealth);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        stringData = String.valueOf(chars);
        entID = bytes.readInt();
        health = bytes.readFloat();
        maxhealth = bytes.readFloat();
        
        InfernalMobsCore.proxy.onHealthPacket(stringData, entID, health, maxhealth);
    }

}
