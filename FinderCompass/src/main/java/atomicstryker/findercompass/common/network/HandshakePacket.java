package atomicstryker.findercompass.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import atomicstryker.findercompass.client.FinderCompassClientTicker;
import atomicstryker.findercompass.client.FinderCompassLogic;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;

public class HandshakePacket implements IPacket
{

    private byte[] configByteArray;

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            File config = FinderCompassMod.instance.compassConfig;
            configByteArray = new byte[(int)config.length()];
            try
            {
                FileInputStream fis = new FileInputStream(config);
                fis.read(configByteArray);
                fis.close();
                bytes.writeShort(configByteArray.length);
                bytes.writeBytes(configByteArray);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            FinderCompassLogic.serverHasFinderCompass = true;
            short len = bytes.readShort();
            configByteArray = new byte[len];
            bytes.readBytes(configByteArray);
            FinderCompassClientTicker.instance.inputOverrideConfig(new DataInputStream(new ByteArrayInputStream(configByteArray)));
        }
        else
        {
            File config = FinderCompassMod.instance.compassConfig;
            if (config != null && config.exists())
            {
                ctx.channel().writeAndFlush(this);
            }
        }
    }

}
