package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;

public class HasMinionsPacket implements IPacket
{
    
    public HasMinionsPacket() {}
    
    private int hasMinions, hasAllMinions;
    private String username;
    
    public HasMinionsPacket(int a, int b)
    {
        hasMinions = a;
        hasAllMinions = b;
    }
    
    public HasMinionsPacket(String s)
    {
        username = s;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            bytes.writeInt(hasMinions);
            bytes.writeInt(hasAllMinions);
        }
        else
        {
            bytes.writeShort(username.length());
            for (char c : username.toCharArray()) bytes.writeChar(c);
        }
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            hasMinions = bytes.readInt();
            hasAllMinions = bytes.readInt();
            MinionsClient.hasMinionsSMPOverride = hasMinions > 0;
            MinionsClient.hasAllMinionsSMPOverride = hasAllMinions > 0;
            MinionsCore.debugPrint("Client got status packet, now: hasMinionsSMPOverride = "+hasMinions+", hasAllMinionsSMPOverride: "+hasAllMinions);
        }
        else
        {
            short len = bytes.readShort();
            char[] chars = new char[len];
            for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
            username = String.valueOf(chars);
            
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
            if (p != null)
            {
                hasMinions = MinionsCore.instance.hasPlayerMinions(p) ? 1 : 0;
                hasAllMinions = MinionsCore.instance.hasAllMinions(p) ? 1 : 0;
                MinionsCore.instance.networkHelper.sendPacketToPlayer(this, p);
            }
        }
    }

}
