package atomicstryker.battletowers.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.network.NetworkHelper.IPacket;

public class ChestAttackedPacket implements IPacket
{
    
    private String playerName;
    private int golemEntityID;
    
    // if there is a constructor with >0 args, we MUST supply another with no args
    public ChestAttackedPacket()
    {
        playerName = "";
        golemEntityID = 0;
    }
    
    public ChestAttackedPacket(String player, int id)
    {
        playerName = player;
        golemEntityID = id;
    }
    
    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeShort(playerName.length());
        for (char c : playerName.toCharArray()) bytes.writeChar(c);
        bytes.writeInt(golemEntityID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        playerName = String.valueOf(chars);
        golemEntityID = bytes.readInt();
        
        EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(playerName);
        if (p != null)
        {
            Entity e = p.worldObj.getEntityByID(golemEntityID);
            if (e != null && e instanceof AS_EntityGolem)
            {
                AS_EntityGolem golem = (AS_EntityGolem) e;
                golem.setAwake();
                golem.setAttackTarget(p);
            }
        }
    }
    
}