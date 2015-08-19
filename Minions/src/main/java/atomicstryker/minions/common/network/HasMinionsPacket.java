package atomicstryker.minions.common.network;

import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class HasMinionsPacket implements IPacket
{
    
    public HasMinionsPacket() {}
    
    private int hasMinions, hasAllMinions;
    
    public HasMinionsPacket(int a, int b)
    {
        hasMinions = a;
        hasAllMinions = b;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(hasMinions);
        bytes.writeInt(hasAllMinions);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        hasMinions = bytes.readInt();
        hasAllMinions = bytes.readInt();
        MinionsClient.hasMinionsSMPOverride = hasMinions > 0;
        MinionsClient.hasAllMinionsSMPOverride = hasAllMinions > 0;
        MinionsCore.debugPrint("Client got status packet, now: hasMinionsSMPOverride = "+hasMinions+", hasAllMinionsSMPOverride: "+hasAllMinions);
    }

}
