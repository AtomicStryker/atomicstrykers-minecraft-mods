package atomicstryker.magicyarn.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import atomicstryker.magicyarn.common.network.PacketDispatcher.WrappedPacket;
import cpw.mods.fml.common.network.ByteBufUtils;

/**
 * @author AtomicStryker
 * 
 * Utility class offering convenience methods to deal with recurring Packet stuff.
 */
public final class PacketWrapper
{
    
    /**
     * Create a new Packet and encode the Objects you provide in it
     * channel String is no longer used
     */
    public static WrappedPacket createPacket(String channel, int packetID, Object[] input)
    {
        return new WrappedPacket(packetID, input);
    }
    
    public static WrappedPacket createPacket(int packetID, Object[] input)
    {
        return new WrappedPacket(packetID, input);
    }

    /**
     * Decodes a (packet's) byte array as Object Array of Class Instances you provide
     */
    public static Object[] readPacketData(ByteBuf data, Class<?>[] packetDataTypes)
    {
        List<Object> result = new ArrayList<Object>();
        for (Class<?> curClass : packetDataTypes)
        {
            result.add(readObjectFromStream(data, curClass));
        }

        return result.toArray();
    }
    
    private static Object readObjectFromStream(ByteBuf data, Class<?> curClass)
    {
        if (curClass.equals(Boolean.class))
        {
            return data.readBoolean();
        }
        else if (curClass.equals(Byte.class))
        {
            return data.readByte();
        }
        else if (curClass.equals(Integer.class))
        {
            return data.readInt();
        }
        else if (curClass.equals(String.class))
        {
            return ByteBufUtils.readUTF8String(data);
        }
        else if (curClass.equals(Double.class))
        {
            return data.readDouble();
        }
        else if (curClass.equals(Float.class))
        {
            return data.readFloat();
        }
        else if (curClass.equals(Long.class))
        {
            return data.readLong();
        }
        else if (curClass.equals(Short.class))
        {
            return data.readShort();
        }

        return null;
    }
}
