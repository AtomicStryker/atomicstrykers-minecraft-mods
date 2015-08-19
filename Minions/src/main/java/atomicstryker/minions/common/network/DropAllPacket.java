package atomicstryker.minions.common.network;

import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class DropAllPacket implements IPacket
{
    
    private String user;
    private int targetID;
    
    public DropAllPacket() {}
    
    public DropAllPacket(String username, int entID)
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
        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(user);
        if (player != null)
        {
            Entity target = player.worldObj.getEntityByID(targetID);
            MinionsCore.debugPrint("DropAllPacket readBytes, "+user+", "+target);
            if (target instanceof EntityMinion)
            {
                MinionsCore.instance.orderMinionToDrop((EntityMinion) target);
            }
        }
    }

}
