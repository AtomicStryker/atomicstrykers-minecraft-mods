package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;

public class DigStairwellPacket implements IPacket
{

    private String user;
    private int x, y, z;

    public DigStairwellPacket()
    {
    }

    public DigStairwellPacket(String username, int a, int b, int c)
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
        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
        if (player != null)
        {
            x = bytes.readInt();
            y = bytes.readInt();
            z = bytes.readInt();
            
            if (MinionsCore.instance.hasPlayerWillPower(player))
            {
                MinionsCore.instance.orderMinionsToDigStairWell(player, x, y, z);
                MinionsCore.instance.exhaustPlayerBig(player);
            }
        }
    }

}
