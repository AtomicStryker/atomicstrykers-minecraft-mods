package atomicstryker.minions.common.network;

import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class AssignChestPacket implements IPacket
{

    private String user;
    private int x, y, z;
    private boolean sneak;

    public AssignChestPacket()
    {
    }

    public AssignChestPacket(String username, boolean sneaking, int a, int b, int c)
    {
        user = username;
        sneak = sneaking;
        x = a;
        y = b;
        z = c;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeBoolean(sneak);
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        sneak = bytes.readBoolean();
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new ScheduledCode());
    }
    
    class ScheduledCode implements Runnable
    {

        @Override
        public void run()
        {
            EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(user);
            if (player != null)
            {                
                MinionsCore.instance.orderMinionsToChestBlock(player, sneak, x, y, z);
            }
        }
        
    }

}
