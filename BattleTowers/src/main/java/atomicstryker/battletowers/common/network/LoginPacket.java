package atomicstryker.battletowers.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.network.NetworkHelper.IPacket;

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
        System.out.println("LoginPacket read, side "+FMLCommonHandler.instance().getEffectiveSide());
        AS_BattleTowersCore.instance.towerDestroyerEnabled = bytes.readByte();   
    }
    
}