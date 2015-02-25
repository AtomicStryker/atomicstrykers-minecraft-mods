package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;

public class EvilDeedPacket implements IPacket
{

    private String user;
    private String sound;
    private int soundLength;

    public EvilDeedPacket()
    {
    }

    public EvilDeedPacket(String username, String snd, int len)
    {
        user = username;
        sound = snd;
        soundLength = len;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        ByteBufUtils.writeUTF8String(bytes, sound);
        bytes.writeInt(soundLength);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        sound = ByteBufUtils.readUTF8String(bytes);
        soundLength = bytes.readInt();

        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
        if (player != null)
        {
            if (player.experienceLevel >= MinionsCore.instance.evilDeedXPCost)
            {
                player.addExperienceLevel(-MinionsCore.instance.evilDeedXPCost);
                MinionsCore.instance.onMasterAddedEvil(player, soundLength);
                player.addPotionEffect(new PotionEffect(Potion.blindness.id, soundLength * 30, 0));
                MinionsCore.instance.sendSoundToClients(player, sound);
                MinionsCore.debugPrint("player "+player+" just did evil deed "+sound);
            }
        }
    }

}
