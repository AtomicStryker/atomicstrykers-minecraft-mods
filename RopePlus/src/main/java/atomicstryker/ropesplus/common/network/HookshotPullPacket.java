package atomicstryker.ropesplus.common.network;

import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class HookshotPullPacket implements IPacket
{
    
    private String username;
    private int hookID;
    
    
    public HookshotPullPacket() {}
    
    public HookshotPullPacket(String user, int hookropeid)
    {
        username = user;
        hookID = hookropeid;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, username);
        bytes.writeInt(hookID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        username = ByteBufUtils.readUTF8String(bytes);
        hookID = bytes.readInt();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            RopesPlusCore.proxy.setShouldRopeChangeState(-1f);
        }
        else
        {
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
            if (player != null)
            {
                Entity target = player.worldObj.getEntityByID(hookID);
                if (target != null && target instanceof EntityFreeFormRope)
                {
                    target.setDead();
                }
            }
        }
    }

}
