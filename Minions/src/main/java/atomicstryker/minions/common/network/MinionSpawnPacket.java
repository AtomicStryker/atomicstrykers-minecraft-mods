package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;

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
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {

        }
        else
        {
            ByteBufUtils.writeUTF8String(bytes, user);
            bytes.writeInt(x);
            bytes.writeInt(y);
            bytes.writeInt(z);
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

}
