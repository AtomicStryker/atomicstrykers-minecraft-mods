package atomicstryker.minions.common.network;

import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.client.FMLClientHandler;

public class MinionMountPacket implements IPacket
{
    
    public MinionMountPacket() {}
    
    private int minionID, targetID;
    
    public MinionMountPacket(int a, int b)
    {
        minionID = a;
        targetID = b;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(minionID);
        bytes.writeInt(targetID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        minionID = bytes.readInt();
        targetID = bytes.readInt();
        FMLClientHandler.instance().getClient().addScheduledTask(new ScheduledCode());
    }
    
    class ScheduledCode implements Runnable
    {

        @Override
        public void run()
        {
            MinionsClient.onMinionMountPacket(minionID, targetID);
        }
    }

}
