package atomicstryker.petbat.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.petbat.common.ItemPocketedPetBat;
import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.NetworkHelper.IPacket;

public class BatNamePacket implements IPacket
{

    private String user, batName;

    public BatNamePacket()
    {
    }

    public BatNamePacket(String bdata, String idata)
    {
        user = bdata;
        batName = idata;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        ByteBufUtils.writeUTF8String(bytes, batName);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        batName = ByteBufUtils.readUTF8String(bytes);
        EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
        if (p != null)
        {
            if (p.getCurrentEquippedItem() != null && p.getCurrentEquippedItem().getItem() == PetBatMod.instance().itemPocketedBat)
            {
                ItemPocketedPetBat.writeBatNameToItemStack(p.getCurrentEquippedItem(), batName);
            }
        }
    }

}
