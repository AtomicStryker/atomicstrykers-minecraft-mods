package atomicstryker.minions.common.network;

import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class ChopTreesPacket implements IPacket
{

    private String user;
    private int x, y, z;

    public ChopTreesPacket()
    {
    }

    public ChopTreesPacket(String username, int a, int b, int c)
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
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        MinecraftServer.getServer().addScheduledTask(new ScheduledCode());
    }
    
    class ScheduledCode implements Runnable
    {

        @Override
        public void run()
        {
            EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
            if (player != null)
            {            
                if (MinionsCore.instance.hasPlayerWillPower(player))
                {
                    MinionsCore.instance.orderMinionsToChopTrees(player, x, y, z);
                    MinionsCore.instance.exhaustPlayerBig(player);
                }
            }
        }
    }

}
