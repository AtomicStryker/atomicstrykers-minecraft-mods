package atomicstryker.minefactoryreloaded.common;

import java.io.*;
import java.util.*;

import net.minecraft.src.Packet250CustomPayload;

/**
 * @author AtomicStryker
 * 
 * Utility class offering convenience methods to deal with recurring Packet stuff.
 */
public final class PacketWrapper
{
    /**
     * Create a new Packet250CustomPayload and encode the Objects you provide in it
     * 
     * @param channel String name of the packet channel to use
     * @param packetID Integer value to be prefixed to the data array so you can distinguish packets
     * @param input Object Array to be encoded to the packet byte array.
     */
    public static Packet250CustomPayload createPacket(String channel, int packetID, Object[] input)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try
        {
            data.write(packetID);

            if (input != null)
            {
                for (Object obj : input)
                {
                    writeObjectToStream(obj, data);
                }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = channel;
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;

        return packet;
    }

    /**
     * Decodes a (packet's) byte array as Object Array of Class Instances you provide
     * 
     * @param data byte array input
     * @param packetDataTypes array of class types to decode the byte array as
     */
    public static Object[] readPacketData(DataInputStream data, Class[] packetDataTypes)
    {
        List result = new ArrayList<Object>();

        try
        {
            for (Class curClass : packetDataTypes)
            {
                result.add(readObjectFromStream(data, curClass));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result.toArray();
    }

    private static void writeObjectToStream(Object obj, DataOutputStream data) throws IOException
    {
        Class objClass = obj.getClass();

        if (objClass.equals(Boolean.class))
        {
            data.writeBoolean((Boolean) obj);
        }
        else if (objClass.equals(Byte.class))
        {
            data.writeByte((Byte) obj);
        }
        else if (objClass.equals(Integer.class))
        {
            data.writeInt((Integer) obj);
        }
        else if (objClass.equals(String.class))
        {
            data.writeUTF((String) obj);
        }
        else if (objClass.equals(Double.class))
        {
            data.writeDouble((Double) obj);
        }
        else if (objClass.equals(Float.class))
        {
            data.writeFloat((Float) obj);
        }
        else if (objClass.equals(Long.class))
        {
            data.writeLong((Long) obj);
        }
        else if (objClass.equals(Short.class))
        {
            data.writeShort((Short) obj);
        }
    }

    private static Object readObjectFromStream(DataInputStream data, Class curClass) throws IOException
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
            return data.readUTF();
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

    public static int readPacketID(DataInputStream data)
    {
        int result = -1;

        try
        {
            result = data.read();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
