package atomicstryker.multimine.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.MultiMineServer;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;

public class PartialBlockPacket implements IPacket
{

    private String user;
    private int x, y, z, value;
    

    public PartialBlockPacket()
    {
    }

    public PartialBlockPacket(String username, int ix, int iy, int iz, int val)
    {
        user = username;
        x = ix;
        y = iy;
        z = iz;
        value = val;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
        bytes.writeInt(value);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        value = bytes.readInt();
        
        if (user.equals("server"))
        {
            MultiMineClient.instance().onServerSentPartialBlockData(x, y, z, value);
        }
        else
        {
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(user);
            if (player != null)
            {
                MultiMineServer.instance().onClientSentPartialBlockPacket(player, x, y, z, value);
            }
        }
    }

}
