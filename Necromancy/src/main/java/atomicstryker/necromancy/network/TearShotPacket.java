package atomicstryker.necromancy.network;

import com.sirolf2009.necromancy.entity.EntityTear;
import com.sirolf2009.necromancy.entity.EntityTearBlood;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.necromancy.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.network.ByteBufUtils;

public class TearShotPacket implements IPacket
{

    private String user;
    private boolean blood;

    public TearShotPacket()
    {
    }

    public TearShotPacket(String username, boolean blo)
    {
        user = username;
        blood = blo;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeBoolean(blood);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        blood = bytes.readBoolean();
        
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(user);
        if (player != null)
        {
            EntityTear tearNormal = blood ? new EntityTearBlood(player.worldObj, player) : new EntityTear(player.worldObj, player);
            player.worldObj.spawnEntityInWorld(tearNormal);
        }
    }

}
