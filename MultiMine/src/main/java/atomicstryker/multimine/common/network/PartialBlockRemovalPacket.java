package atomicstryker.multimine.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;

public class PartialBlockRemovalPacket implements IPacket
{

    private int x, y, z;

    public PartialBlockRemovalPacket()
    {
    }

    public PartialBlockRemovalPacket(int ix, int iy, int iz)
    {
        x = ix;
        y = iy;
        z = iz;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(x, y, z);
    }

}
