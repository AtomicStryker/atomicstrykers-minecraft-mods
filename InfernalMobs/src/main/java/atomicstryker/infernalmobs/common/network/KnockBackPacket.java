package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.client.FMLClientHandler;

public class KnockBackPacket implements IPacket
{
    
    private float xv, zv;

    public KnockBackPacket() {}
    
    public KnockBackPacket(float x, float z)
    {
        xv = x;
        zv = z;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeFloat(xv);
        bytes.writeFloat(zv);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        xv = bytes.readFloat();
        zv = bytes.readFloat();
        FMLClientHandler.instance().getClient().addScheduledTask(new ScheduledCode());
    }
    
    class ScheduledCode implements Runnable
    {
        @Override
        public void run()
        {
            InfernalMobsCore.proxy.onKnockBackPacket(xv, zv);
        }
    }

}
