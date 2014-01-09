package atomicstryker.battletowers.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.network.NetworkHelper.IPacket;

public class LoginPacket implements IPacket
{

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeByte(AS_BattleTowersCore.towerDestroyerEnabled);
    }
    
    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        AS_BattleTowersCore.towerDestroyerEnabled = bytes.readByte();   
    }
    
}