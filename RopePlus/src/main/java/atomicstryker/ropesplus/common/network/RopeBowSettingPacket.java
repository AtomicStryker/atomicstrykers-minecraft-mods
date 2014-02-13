package atomicstryker.ropesplus.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.ropesplus.common.Settings_RopePlus;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;

public class RopeBowSettingPacket implements IPacket
{
    private boolean disableBowHook;
    
    
    public RopeBowSettingPacket() {}
    
    public RopeBowSettingPacket(boolean b)
    {
        disableBowHook = b;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeBoolean(disableBowHook);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        disableBowHook = bytes.readBoolean();
        Settings_RopePlus.disableBowHook = disableBowHook;
    }

}
