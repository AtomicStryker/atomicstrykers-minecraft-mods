package atomicstryker.network;

import net.minecraft.server.MinecraftServer;


public class ServerPacketProxy implements PacketProxy
{

    @Override
    public String getSenderName()
    {
        return "Server";
    }

    @Override
    public void onPacketData(int packetType, WrappedPacket packet, String sender)
    {
        PacketDispatcher.serverPacketHandler.onPacketData(packetType, packet, MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(sender));
    }

}
