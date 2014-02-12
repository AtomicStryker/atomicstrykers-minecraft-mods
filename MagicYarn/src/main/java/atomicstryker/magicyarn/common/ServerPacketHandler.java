package atomicstryker.magicyarn.common;


import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.network.ForgePacketWrapper;
import atomicstryker.network.PacketDispatcher;
import atomicstryker.network.PacketDispatcher.IPacketHandler;
import atomicstryker.network.WrappedPacket;

public class ServerPacketHandler implements IPacketHandler
{
    
    @Override
    public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
    {
        if (packetType == 1)
        {
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("MagicYarn", 1, null), player);
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
