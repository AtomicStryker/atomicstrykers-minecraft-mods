package atomicstryker.kenshiro.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import atomicstryker.kenshiro.client.KenshiroClient;
import atomicstryker.kenshiro.common.KenshiroMod;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;

public class SoundPacket implements IPacket
{
    
    private String sound;
    private int dimension, x, y, z;
    
    public SoundPacket() {}
    
    public SoundPacket(String s, int dim, int xi, int yi, int zi)
    {
        sound = s;
        dimension = dim;
        x = xi;
        y = yi;
        z = zi;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeShort(sound.length());
        for (char c : sound.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(dimension);
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        sound = String.valueOf(chars);
        dimension = bytes.readInt();
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            KenshiroMod.instance().networkHelper.sendPacketToAllAroundPoint(this, new TargetPoint(dimension, x, y, z, 32d));
        }
        else
        {
            KenshiroClient.instance().playSound(x, y, z, "kenshiro:"+sound);
        }
    }

}
