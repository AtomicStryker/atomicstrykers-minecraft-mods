package atomicstryker.magicyarn.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.magicyarn.client.MagicYarnClient;
import atomicstryker.magicyarn.common.MagicYarn;
import atomicstryker.magicyarn.common.network.NetworkHelper.IPacket;

public class PathPacket implements IPacket
{
    
    private ByteBuf data;
    
    public PathPacket() {}
    
    public PathPacket(ByteBuf pathData)
    {
        data = pathData;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeBytes(data);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            MagicYarnClient.instance.onReceivedPathPacket(bytes);
        }
        else
        {
            MagicYarn.instance.networkHelper.sendPacketToAllPlayers(new PathPacket(bytes.copy()));
        }
    }
}
