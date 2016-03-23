package atomicstryker.petbat.common.network;

import atomicstryker.petbat.common.ItemPocketedPetBat;
import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;

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
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new ScheduledCode());
    }
    
    class ScheduledCode implements Runnable
    {
        @Override
        public void run()
        {
            EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(user);
            if (p != null)
            {
                if (p.getHeldItemMainhand() != null && p.getHeldItemMainhand().getItem() == PetBatMod.instance().itemPocketedBat)
                {
                    ItemPocketedPetBat.writeBatNameToItemStack(p.getHeldItemMainhand(), batName);
                }
            }
        }
    }

}
