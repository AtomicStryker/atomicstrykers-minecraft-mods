package atomicstryker.kenshiro.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;

public class AnimationPacket implements IPacket
{
    
    private String username;
    private int animation;
    
    public AnimationPacket() {}

    public AnimationPacket(String s, int anim)
    {
        username = s;
        animation = anim;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeShort(username.length());
        for (char c : username.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(animation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        username = String.valueOf(chars);
        animation = bytes.readInt();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
            if (p != null)
            {
                for (EntityPlayerMP po : (ArrayList<EntityPlayerMP>)MinecraftServer.getServer().getConfigurationManager().playerEntityList)
                {
                    po.playerNetServerHandler.sendPacket(new S0BPacketAnimation(p, animation));
                }
            }
        }
    }

}