package atomicstryker.petbat.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.network.ForgePacketWrapper;
import atomicstryker.network.PacketDispatcher.IPacketHandler;
import atomicstryker.network.WrappedPacket;

public class ServerPacketHandler implements IPacketHandler
{

    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketData(int packetType, WrappedPacket packet, EntityPlayer player)
    {
        ByteBuf data = packet.data;
        if (packetType == 1)
        {
            Class[] decodeAs = {String.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            String batName = (String) packetReadout[0];
            EntityPlayer p = (EntityPlayer) player;
            
            if (p.getCurrentEquippedItem() != null
            && p.getCurrentEquippedItem().getItem() == PetBatMod.instance().itemPocketedBat)
            {
                ItemPocketedPetBat.writeBatNameToItemStack(p.getCurrentEquippedItem(), batName);
            }
        }
    }

}
