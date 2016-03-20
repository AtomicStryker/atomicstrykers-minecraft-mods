package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.MultiMineServer;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PartialBlockPacket implements IPacket
{

    private String user;
    private int x, y, z;
    private float value;

    public PartialBlockPacket()
    {
    }

    public PartialBlockPacket(String username, int ix, int iy, int iz, float val)
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
        bytes.writeFloat(value);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        value = bytes.readFloat();
        if (user.equals("server"))
        {
            FMLClientHandler.instance().getClient().addScheduledTask(new ScheduledCode());
        }
        else
        {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new ScheduledCode());
        }
    }

    class ScheduledCode implements Runnable
    {

        @Override
        public void run()
        {
            if (user.equals("server"))
            {
                MultiMineClient.instance().onServerSentPartialBlockData(x, y, z, value);
            }
            else
            {
                EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(user);
                if (player != null)
                {
                    MultiMineServer.instance().onClientSentPartialBlockPacket(player, x, y, z, value);
                }
            }
        }

    }

}
