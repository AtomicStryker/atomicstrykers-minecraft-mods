package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

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
            
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                writeStringToBuf(FMLClientHandler.instance().getClientPlayerEntity().getCommandSenderName(), bytes);
            }
            else
            {
                writeStringToBuf("toServer", bytes);
            }
            
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
            
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                clientPacketHandler.onPacketData(pid, new WrappedPacket(pid, bytes), FMLClientHandler.instance().getClientPlayerEntity());
            }
            else
            {
                EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(String.valueOf(chars));
                if(player != null)
                {
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

    public static void sendToAllNear(double posX, double posY, double posZ, double distance, int dimension, WrappedPacket packet)
    {
        networkHelper.sendPacketToAllAroundPoint(packet, new TargetPoint(dimension, posX, posY, posZ, distance));
    }

}
