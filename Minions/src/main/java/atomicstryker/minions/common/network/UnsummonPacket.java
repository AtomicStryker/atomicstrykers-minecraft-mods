package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;

public class UnsummonPacket implements IPacket
{
    
    private String user;
    
    public UnsummonPacket() {}
    
    public UnsummonPacket(String username)
    {
        user = username;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
        if (player != null)
        {
            MinionsCore.instance.unSummonPlayersMinions(player);
        }
    }

}
