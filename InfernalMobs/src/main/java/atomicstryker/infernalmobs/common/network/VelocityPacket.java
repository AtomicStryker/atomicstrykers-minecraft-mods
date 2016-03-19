package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VelocityPacket implements IPacket
{
    
    private float xv, yv, zv;

    public VelocityPacket() {}
    
    public VelocityPacket(float x, float y, float z)
    {
        xv = x;
        yv = y;
        zv = z;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeFloat(xv);
        bytes.writeFloat(yv);
        bytes.writeFloat(zv);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        xv = bytes.readFloat();
        yv = bytes.readFloat();
        zv = bytes.readFloat();
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new ScheduledCode());
    }
    
    class ScheduledCode implements Runnable
    {
        @Override
        public void run()
        {
            InfernalMobsCore.proxy.onVelocityPacket(xv, yv, zv);
        }
    }

}
