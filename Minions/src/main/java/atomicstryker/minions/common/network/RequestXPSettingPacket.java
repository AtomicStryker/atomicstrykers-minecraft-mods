package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;

public class RequestXPSettingPacket implements IPacket
{
    
    public RequestXPSettingPacket() {}
    
    private int setting;
    
    public RequestXPSettingPacket(int a)
    {
        setting = a;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(setting);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            setting = bytes.readInt();
            if (MinionsCore.instance.evilDeedXPCost != setting)
            {
                MinionsCore.instance.evilDeedXPCost = setting;
                MinionsClient.onChangedXPSetting();
            }
        }
        else
        {
            setting = MinionsCore.instance.evilDeedXPCost;
            ctx.writeAndFlush(this);
        }
    }

}
