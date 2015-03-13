package atomicstryker.kenshiro.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.kenshiro.client.KenshiroClient;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;

public class HandshakePacket implements IPacket
{

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            KenshiroClient.instance().setServerHasKenshiroInstalled(true);
        }
    }
}
