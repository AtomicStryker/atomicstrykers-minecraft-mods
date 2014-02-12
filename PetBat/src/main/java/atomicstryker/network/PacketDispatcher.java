package atomicstryker.network;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class PacketDispatcher
{
    
    private static NetworkHelper networkHelper;
    public static IPacketHandler clientPacketHandler;
    public static IPacketHandler serverPacketHandler;
    
    @SidedProxy(clientSide = "atomicstryker.network.ClientPacketProxy", serverSide = "atomicstryker.network.ServerPacketProxy")
    public static PacketProxy proxy;
    
    /**
     * Call this in preInit, provide a channelname and handler instances
     */
    public static void init(String channel, IPacketHandler ch, IPacketHandler sh)
    {
        networkHelper = new NetworkHelper(channel, WrappedPacket.class);
        clientPacketHandler = ch;
        serverPacketHandler = sh;
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
