package atomicstryker.ropesplus.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.ropesplus.client.RopesPlusClient;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;

public class GrapplingHookPacket implements IPacket
{
    private boolean grapplingHookOut;
    
    
    public GrapplingHookPacket() {}
    
    public GrapplingHookPacket(boolean b)
    {
        grapplingHookOut = b;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeBoolean(grapplingHookOut);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        grapplingHookOut = bytes.readBoolean();
        RopesPlusClient.grapplingHookOut = grapplingHookOut;
    }

}
