package atomicstryker.multimine.common.network;

import net.minecraft.util.BlockPos;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;

public class PartialBlockRemovalPacket implements IPacket
{

    private BlockPos pos;

    public PartialBlockRemovalPacket()
    {
    }

    public PartialBlockRemovalPacket(BlockPos p)
    {
    	pos = p;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(pos.getX());
        bytes.writeInt(pos.getY());
        bytes.writeInt(pos.getZ());
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
    	pos = new BlockPos(bytes.readInt(), bytes.readInt(), bytes.readInt());
        MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(pos);
    }

}
