package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

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
        FMLClientHandler.instance().getClient().addScheduledTask(new ScheduledCode());
    }

    class ScheduledCode implements Runnable
    {

        @Override
        public void run()
        {
            MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(pos);
        }
    }

}
