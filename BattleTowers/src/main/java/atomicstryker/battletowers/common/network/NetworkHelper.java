package atomicstryker.battletowers.common.network;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 * Helper class to wrap the new 1.7 Netty channels and packets into something
 * resembling the older packet system. Create one instance of this for a Mod,
 * then use the helper methods to send Packets. Packet Handling is done inside
 * the packet classes themselves.
 * 
 * @author AtomicStryker
 *
 */
public class NetworkHelper
{
    
    private final FMLEmbeddedChannel clientOutboundChannel;
    private final FMLEmbeddedChannel serverOutboundChannel;
    
    private final HashSet<Class<? extends IPacket>> registeredClasses;
    
    /**
     * Set true when helper is about to send a packet, remains true until packet is out
     */
    private boolean isCurrentlySendingSemaphor;
    
    /**
     * Creates an instance of the NetworkHelper with included channels for client and server communication.
     * Automatically registers the necessary channels and discriminators for the supplied Packet classes.
     * @param channelName channel name to use, anything but already taken designations goes
     * @param handledPacketClasses provide the IPacket classes you want to use for communication here
     */
    @SafeVarargs
    public NetworkHelper(String channelName, Class<? extends IPacket> ... handledPacketClasses)
    {
        EnumMap<Side, FMLEmbeddedChannel> channelPair = NetworkRegistry.INSTANCE.newChannel(channelName, new ChannelCodec(handledPacketClasses), new ChannelHandler());
        clientOutboundChannel = channelPair.get(Side.CLIENT);
        serverOutboundChannel = channelPair.get(Side.SERVER);
        
        registeredClasses = new HashSet<Class<? extends IPacket>>(handledPacketClasses.length);
        Collections.addAll(registeredClasses, handledPacketClasses);
    }
    
    /**
     * Packets only need to implement this and offer a constructor with no args,
     * unless you don't have constructors with >0 args. The class MUST also be
     * statically accessible, else you will suffer an InstantiationException!
     * Note Packets don't distinguish between being sent from client to server or
     * the other way around, so be careful using them bidirectional or avoid
     * doing that altogether.
     */
    public interface IPacket
    {
        
        /**
         * Executed upon sending a Packet away. Put your arbitrary data into the ByteBuffer,
         * and retrieve it on the receiving side when readBytes is executed.
         * @param ctx channel context
         * @param bytes data being sent
         */
        void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes);
        
        /**
         * Executed upon arrival of a Packet at a recipient. Byte order matches writeBytes exactly.
         * @param ctx channel context, you can send answers through here directly
         * @param bytes data being received
         */
        void readBytes(ChannelHandlerContext ctx, ByteBuf bytes);
    }
    
    /**
     * Sends the supplied Packet from a client to the server
     * @param packet to send
     */
    public void sendPacketToServer(IPacket packet)
    {
        checkClassAndSync(packet.getClass());
        clientOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        clientOutboundChannel.writeOutbound(packet);
        isCurrentlySendingSemaphor = false;
    }

    /**
     * Sends the supplied Packet from the server to the chosen Player
     * @param packet to send
     * @param player to send to
     */
    public void sendPacketToPlayer(IPacket packet, EntityPlayerMP player)
    {
        checkClassAndSync(packet.getClass());
        serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        serverOutboundChannel.writeOutbound(packet);
        isCurrentlySendingSemaphor = false;
    }
    
    /**
     * Sends a packet from the server to all currently connected players
     * @param packet to send
     */
    public void sendPacketToAllPlayers(IPacket packet)
    {
        checkClassAndSync(packet.getClass());
        serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        serverOutboundChannel.writeOutbound(packet);
        isCurrentlySendingSemaphor = false;
    }
    
    /**
     * Sends a packet from the server to all players in a dimension around a location
     * @param packet to send
     * @param tp targetpoint instance to pass, cannot be null
     */
    public void sendPacketToAllAroundPoint(IPacket packet, TargetPoint tp)
    {
        checkClassAndSync(packet.getClass());
        serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(tp);
        serverOutboundChannel.writeOutbound(packet);
        isCurrentlySendingSemaphor = false;
    }
    
    /**
     * Sends a packet from the server to all players in a dimension
     * @param packet to send
     * @param dimension serverside dim id to use
     */
    public void sendPacketToAllInDimension(IPacket packet, int dimension)
    {
        checkClassAndSync(packet.getClass());
        serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimension);
        serverOutboundChannel.writeOutbound(packet);
        isCurrentlySendingSemaphor = false;
    }
    
    /**
     * Since the crash that happens if we dont do this is complete garbage
     */
    private void checkClassAndSync(Class<? extends IPacket> clazz)
    {
        if (!registeredClasses.contains(clazz))
        {
            throw new RuntimeException("NetworkHelper got unknown Packet type "+clazz+" to send, critical error");
        }
        
        // prevent concurrent packet sending
        while (isCurrentlySendingSemaphor)
        {
            Thread.yield();
        }
        isCurrentlySendingSemaphor = true;
    }
    
    /**
     * Internal Channel Codec, automatic discrimination and data forwarding
     */
    private class ChannelCodec extends FMLIndexedMessageToMessageCodec<IPacket>
    {
        
        @SafeVarargs
        public ChannelCodec(Class<? extends IPacket> ... handledPacketClasses)
        {
            for (int i = 0; i < handledPacketClasses.length; i++)
            {
                addDiscriminator(i, handledPacketClasses[i]);
            }
        }

        @Override
        public void encodeInto(ChannelHandlerContext ctx, IPacket msg, ByteBuf bytes) throws Exception
        {
            msg.writeBytes(ctx, bytes);
        }

        @Override
        public void decodeInto(ChannelHandlerContext ctx, ByteBuf bytes, IPacket msg)
        {
            msg.readBytes(ctx, bytes);
        }
        
    }
    
    @Sharable
    public class ChannelHandler extends SimpleChannelInboundHandler<IPacket>
    {
        public ChannelHandler() {}
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, IPacket msg) throws Exception
        {
            // NOOP, just to prevent memory leaks
        }
    }
    
}
