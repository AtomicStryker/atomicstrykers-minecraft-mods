package atomicstryker.ropesplus.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;

public class SoundPacket implements IPacket
{

    private String user, sound;

    public SoundPacket() {}

    public SoundPacket(String username, String soundfile)
    {
        user = username;
        sound = soundfile;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        ByteBufUtils.writeUTF8String(bytes, sound);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        sound = ByteBufUtils.readUTF8String(bytes);
        
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
        if (player != null)
        {
            player.worldObj.playSoundAtEntity(player, sound, 1f, 1f);
        }
    }

}
