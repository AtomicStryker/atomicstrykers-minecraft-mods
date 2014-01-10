package atomicstryker.magicyarn.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import atomicstryker.magicyarn.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;

public class PacketDispatcher
{
    
    private static NetworkHelper networkHelper;
    private static IPacketHandler clientPacketHandler;
    private static IPacketHandler serverPacketHandler;
    
    /**
     * Call this in preInit, provide a channelname and handler instances
     */
    public static void init(String channel, IPacketHandler ch, IPacketHandler sh)
    {
        networkHelper = new NetworkHelper(channel, PacketDispatcher.WrappedPacket.class);
        clientPacketHandler = ch;
        serverPacketHandler = sh;
    }
    
    public static class WrappedPacket implements IPacket
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
                ByteBufUtils.writeUTF8String(data, (String) obj);
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

        @Override
        public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
        {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                int pid = bytes.readInt();
                clientPacketHandler.onPacketData(pid, new WrappedPacket(pid, bytes), FMLClientHandler.instance().getClientPlayerEntity());
            }
            else
            {
                INetHandler handler = ctx.attr(NetworkRegistry.NET_HANDLER).get();
                if(handler instanceof NetHandlerPlayServer)
                {
                    NetHandlerPlayServer serverHandler = (NetHandlerPlayServer)handler;
                    EntityPlayerMP player = serverHandler.field_147369_b;
                    int pid = bytes.readInt();
                    serverPacketHandler.onPacketData(pid, new WrappedPacket(pid, bytes), player);
                }
            }
        }
    }
    
    public static interface IPacketHandler
    {
        public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player);
    }
    
    public static void sendPacketToServer(WrappedPacket packet)
    {
        networkHelper.sendPacketToServer(packet);
    }

    public static void sendPacketToPlayer(WrappedPacket packet, EntityPlayer player)
    {
        networkHelper.sendPacketToPlayer(packet, player);
    }
    
    public static void sendPacketToAllInDimension(WrappedPacket packet, int dimension)
    {
        networkHelper.sendPacketToAllInDimension(packet, dimension);
    }

    public static void sendPacketToAllPlayers(WrappedPacket packet)
    {
        networkHelper.sendPacketToAllPlayers(packet);
    }

}
