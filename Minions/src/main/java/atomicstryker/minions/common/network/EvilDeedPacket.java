package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;

public class EvilDeedPacket implements IPacket
{
    
    private String user;
    
    public EvilDeedPacket() {}
    
    public EvilDeedPacket(String username)
    {
        user = username;
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
                if (player.experienceLevel >= MinionsCore.instance.evilDeedXPCost)
                {
                    player.addExperienceLevel(-MinionsCore.instance.evilDeedXPCost);
                    MinionsCore.instance.onMasterAddedEvil(player);
                }
            }
        }
    }

}
