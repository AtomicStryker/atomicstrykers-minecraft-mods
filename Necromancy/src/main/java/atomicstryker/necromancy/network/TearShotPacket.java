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
    private double sx, sy, sz, ex, ey, ez;

    public TearShotPacket()
    {
    }

    public TearShotPacket(String username, boolean blo, double a, double b, double c, double d, double e, double f)
    {
        user = username;
        blood = blo;
        sx = a;
        sy = b;
        sz = c;
        ex = d;
        ey = e;
        ez = f;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeBoolean(blood);
        bytes.writeDouble(sx);
        bytes.writeDouble(sy);
        bytes.writeDouble(sz);
        bytes.writeDouble(ex);
        bytes.writeDouble(ey);
        bytes.writeDouble(ez);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        blood = bytes.readBoolean();
        sx = bytes.readDouble();
        sy = bytes.readDouble();
        sz = bytes.readDouble();
        ex = bytes.readDouble();
        ey = bytes.readDouble();
        ez = bytes.readDouble();
        
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(user);
        if (player != null)
        {
            EntityTear tearNormal = blood ? new EntityTearBlood(player.worldObj, player, 2) : new EntityTear(player.worldObj, player, 2);
            tearNormal.setPosition(sx, sy, sz);
            tearNormal.setVelocity(ex, ey, ez);
            player.worldObj.spawnEntityInWorld(tearNormal);
        }
    }

}
