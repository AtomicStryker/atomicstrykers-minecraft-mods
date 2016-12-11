package atomicstryker.minions.common.network;

import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.entity.EntityMinion;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class DropAllPacket implements IPacket
{
    
    private String user;
    private int targetID;
    
    public DropAllPacket() {}
    
    public DropAllPacket(String username, int entID)
    {
        user = username;
        targetID = entID;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeInt(targetID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        targetID = bytes.readInt();
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
                Entity target = player.world.getEntityByID(targetID);
                MinionsCore.debugPrint("DropAllPacket readBytes, "+user+", "+target);
                if (target instanceof EntityMinion)
                {
                    MinionsCore.instance.orderMinionToDrop(player, (EntityMinion) target);
                }
            }
        }
    }

}
