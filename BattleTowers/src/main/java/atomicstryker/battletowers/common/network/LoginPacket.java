package atomicstryker.battletowers.common.network;

import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class LoginPacket implements IPacket
{

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeByte(AS_BattleTowersCore.instance.towerDestroyerEnabled);
    }
    
    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        AS_BattleTowersCore.instance.towerDestroyerEnabled = bytes.readByte();   
    }
    
}