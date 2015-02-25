package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;

public class MinionSpawnPacket implements IPacket
{

    private String user;
    private int x, y, z;

    public MinionSpawnPacket()
    {
    }

    public MinionSpawnPacket(String username, int a, int b, int c)
    {
        user = username;
        x = a;
        y = b;
        z = c;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
        if (player != null)
        {
            x = bytes.readInt();
            y = bytes.readInt();
            z = bytes.readInt();

            if (MinionsCore.instance.spawnMinionsForPlayer(player, x, y, z))
            {
                MinionsCore.instance.exhaustPlayerBig(player);
            }
            MinionsCore.instance.networkHelper.sendPacketToPlayer(new HasMinionsPacket(MinionsCore.instance.hasPlayerMinions(player) ? 1 : 0,
                    MinionsCore.instance.hasAllMinions(player) ? 1 : 0), player);
        }
    }

}
