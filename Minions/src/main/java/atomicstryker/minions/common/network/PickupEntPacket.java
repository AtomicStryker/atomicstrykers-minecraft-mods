package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;

public class PickupEntPacket implements IPacket
{
    
    private String user;
    private int targetID;
    
    public PickupEntPacket() {}
    
    public PickupEntPacket(String username, int entID)
    {
        user = username;
        targetID = entID;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeInt(targetID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        targetID = bytes.readInt();
        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
        if (player != null)
        {
            Entity target = player.worldObj.getEntityByID(targetID);
            if (target instanceof EntityAnimal || target instanceof EntityPlayer)
            {
                MinionsCore.instance.orderMinionToPickupEntity(player, (EntityLivingBase) target);
            }
        }
    }

}
