package atomicstryker.magicyarn.client;


import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.magicyarn.common.network.PacketDispatcher.IPacketHandler;
import atomicstryker.magicyarn.common.network.PacketDispatcher.WrappedPacket;

public class ClientPacketHandler implements IPacketHandler
{
    
    @Override
    public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
    {
        if (packetType == 1)
        {
            MagicYarnClient.instance.onServerAnsweredChallenge();
        }
        else if (packetType == 2)
        {
            MagicYarnClient.instance.onReceivedPathPacket(packet.data);
        }
        else if (packetType == 3)
        {
            MagicYarnClient.instance.onReceivedPathDeletionPacket(packet.data);
        }
    }

}
