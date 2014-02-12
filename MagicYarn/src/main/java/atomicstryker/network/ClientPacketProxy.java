package atomicstryker.network;

import net.minecraft.client.Minecraft;

public class ClientPacketProxy implements PacketProxy
{

    @Override
    public String getSenderName()
    {
        return Minecraft.getMinecraft().thePlayer.getCommandSenderName();
    }

    @Override
    public void onPacketData(int packetType, WrappedPacket packet, String sender)
    {
        PacketDispatcher.clientPacketHandler.onPacketData(packetType, packet, Minecraft.getMinecraft().thePlayer);
    }

}
