package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;

public class SoundPacket implements IPacket
{
    private String sound;
    private int dimension, entID;

    public SoundPacket() {}

    public SoundPacket(String soundfile, int dim, int entityID)
    {
        sound = soundfile;
        dimension = dim;
        entID = entityID;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, sound);
        bytes.writeInt(dimension);
        bytes.writeInt(entID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        sound = ByteBufUtils.readUTF8String(bytes);
        dimension = bytes.readInt();
        entID = bytes.readInt();
        
        Entity e = MinecraftServer.getServer().worldServerForDimension(dimension).getEntityByID(entID);
        if (e != null)
        {
            MinionsCore.instance.sendSoundToClients(e, sound);
        }
    }
}
