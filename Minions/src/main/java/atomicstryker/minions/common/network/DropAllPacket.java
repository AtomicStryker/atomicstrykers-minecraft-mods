package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;

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
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            
        }
        else
        {
            ByteBufUtils.writeUTF8String(bytes, user);
            bytes.writeInt(targetID);
        }
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            
        }
        else
        {
            user = ByteBufUtils.readUTF8String(bytes);
            EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(user);
            if (player != null)
            {
                Entity target = player.worldObj.getEntityByID(targetID);
                if (target instanceof EntityMinion)
                {
                    MinionsCore.instance.orderMinionToDrop(player, (EntityMinion) target);
                }
            }
        }
    }

}
