package atomicstryker.ropesplus.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.network.ByteBufUtils;

public class ArrowChoicePacket implements IPacket
{
    
    private String username;
    private int arrowSlot;
    
    
    public ArrowChoicePacket() {}
    
    public ArrowChoicePacket(String user, int as)
    {
        username = user;
        arrowSlot = as;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, username);
        bytes.writeInt(arrowSlot);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        username = ByteBufUtils.readUTF8String(bytes);
        arrowSlot = bytes.readInt();
        
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
        if (player != null)
        {
            RopesPlusCore.instance.setselectedSlot(player, arrowSlot);
        }
    }

}
