package atomicstryker.minions.common.network;

import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class HaxPacket implements IPacket
{
    
    private String user;
    
    public HaxPacket() {}
    
    public HaxPacket(String username)
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
        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(user);
        if (player != null)
        {
            player.addExperience(200);
        }
    }

}
