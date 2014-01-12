package atomicstryker.battletowers.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.EnumMap;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

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
    
    /**
     * Creates an instance of the NetworkHelper with included channels for client and server communication.
     * Automatically registers the necessary channels and discriminators for the supplied Packet classes.
     * @param channelName channel name to use, anything but already taken designations goes
     * @param handledPacketClasses provide the IPacket classes you want to use for communication here
     */
    @SafeVarargs
    public NetworkHelper(String channelName, Class<? extends IPacket> ... handledPacketClasses)
    {
        EnumMap<Side, FMLEmbeddedChannel> channelPair = NetworkRegistry.INSTANCE.newChannel(channelName, new ChannelHandler(handledPacketClasses));
        clientOutboundChannel = channelPair.get(Side.CLIENT);
        serverOutboundChannel = channelPair.get(Side.SERVER);
    }
    
    /**
     * Packets only need to implement this and offer a constructor with no args,
     * unless you don't have constructors with >0 args. The class MUST also be
     * statically accessible, else you will suffer an InstantiationException!
     * Note Packets don't distinguish between being sent from client to server or
     * the other way around, so be careful using them bidirectional or avoid
     * doing that altogether.
     */
    public static interface IPacket
    {
        
        /**
         * Executed upon sending a Packet away. Put your arbitrary data into the ByteBuffer,
         * and retrieve it on the receiving side when readBytes is executed.
         * @param ctx channel context
         * @param bytes data being sent
         */
        public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes);
        
        /**
         * Executed upon arrival of a Packet at a recipient. Byte order matches writeBytes exactly.
         * @param ctx channel context, you can send answers through here directly
         * @param bytes data being received
         */
        public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes);
    }
    
    /**
     * Sends the supplied Packet from a client to the server
     * @param packet
     */
    public void sendPacketToServer(IPacket packet)
    {
        clientOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        clientOutboundChannel.writeOutbound(packet);
    }
    
    /**
     * Sends the supplied Packet from the server to the chosen Player
     * @param packet
     * @param player
     */
    public void sendPacketToPlayer(IPacket packet, EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
            serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
            serverOutboundChannel.writeOutbound(packet);
        }
    }
    
    /**
     * Sends a packet from the server to all currently connected players
     * @param packet
     */
    public void sendPacketToAllPlayers(IPacket packet)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
            serverOutboundChannel.writeOutbound(packet);
        }
    }
    
    /**
     * Sends a packet from the server to all players in a dimension around a location
     * @param packet
     * @param tp
     */
    public void sendPacketToAllAroundPoint(IPacket packet, TargetPoint tp)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
            serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(tp);
            serverOutboundChannel.writeOutbound(packet);
        }
    }
    
    /**
     * Sends a packet from the server to all players in a dimension
     * @param packet
     * @param dimension
     */
    public void sendPacketToAllInDimension(IPacket packet, int dimension)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
            serverOutboundChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimension);
            serverOutboundChannel.writeOutbound(packet);
        }
    }
    
    /**
     * Internal ChannelHandler, automatic discrimination and data forwarding
     */
    private class ChannelHandler extends FMLIndexedMessageToMessageCodec<IPacket>
    {
        
        @SafeVarargs
        public ChannelHandler(Class<? extends IPacket> ... handledPacketClasses)
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
    
}
