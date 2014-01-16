package atomicstryker.magicyarn.common;


import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.magicyarn.common.network.PacketDispatcher;
import atomicstryker.magicyarn.common.network.PacketDispatcher.IPacketHandler;
import atomicstryker.magicyarn.common.network.PacketDispatcher.WrappedPacket;
import atomicstryker.magicyarn.common.network.PacketWrapper;

public class ServerPacketHandler implements IPacketHandler
{
    
    @Override
    public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
    {
        if (packetType == 1)
        {
            PacketDispatcher.sendPacketToPlayer(PacketWrapper.createPacket("MagicYarn", 1, null), player);
        }
        else if (packetType == 2)
        {
            PacketDispatcher.sendPacketToAllInDimension(packet, ((EntityPlayer)player).dimension);
        }
        else if (packetType == 3)
        {
            PacketDispatcher.sendPacketToAllPlayers(packet);
        }
    }

}
