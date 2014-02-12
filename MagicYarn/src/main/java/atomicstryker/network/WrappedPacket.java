package atomicstryker.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.network.NetworkHelper.IPacket;

public class WrappedPacket implements IPacket
{
    
    private int packetID;
    private Object[] toEncode;
    public ByteBuf data;
    
    public WrappedPacket() {}
    
    public WrappedPacket(int pID, Object[] toSend)
    {
        packetID = pID;
        toEncode = toSend;
    }
    
    public WrappedPacket(int pID, ByteBuf b)
    {
        packetID = pID;
        data = b;
    }
    
    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(packetID);
        
        writeStringToBuf(PacketDispatcher.proxy.getSenderName(), bytes);
        
        if (data != null)
        {
            bytes.writeBytes(data);
        }
        else if (toEncode != null)
        {
            for (Object obj : toEncode)
            {
                writeObjectToStream(obj, bytes);
            }
        }
    }

    private void writeObjectToStream(Object obj, ByteBuf data)
    {
        Class<?> objClass = obj.getClass();

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
            writeStringToBuf((String) obj, data);
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
    
    private void writeStringToBuf(String s, ByteBuf buf)
    {
        buf.writeShort(s.length());
        for (char c : s.toCharArray())
        {
            buf.writeChar(c);
        }
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        int pid = bytes.readInt();
        
        // read out the chars even if the client doesnt need em
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++)
        {
            chars[i] = bytes.readChar();
        }
        
        PacketDispatcher.proxy.onPacketData(pid, new WrappedPacket(pid, bytes), String.valueOf(chars));
    }
}
