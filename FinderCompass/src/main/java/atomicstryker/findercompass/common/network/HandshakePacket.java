package atomicstryker.findercompass.common.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import atomicstryker.findercompass.client.FinderCompassClientTicker;
import atomicstryker.findercompass.client.FinderCompassLogic;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class HandshakePacket implements IPacket
{

    private byte[] configByteArray;
    private String username;

    public HandshakePacket() {}
    
    public HandshakePacket(String user)
    {
        username = user;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, username);
        if (username.equals("server"))
        {
            File config = FinderCompassMod.instance.compassConfig;
            configByteArray = new byte[(int)config.length()];
            try
            {
                FileInputStream fis = new FileInputStream(config);
                if (fis.read(configByteArray) < 1)
                {
                    fis.close();
                    throw new Exception("Config does not contain bytes: "+config);
                }
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
        username = ByteBufUtils.readUTF8String(bytes);
        if (username.equals("server"))
        {
            FinderCompassLogic.serverHasFinderCompass = true;
            short len = bytes.readShort();
            configByteArray = new byte[len];
            bytes.readBytes(configByteArray);
            FinderCompassClientTicker.instance.inputOverrideConfig(new DataInputStream(new ByteArrayInputStream(configByteArray)));
        }
    }

}
