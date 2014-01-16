package atomicstryker.kenshiro.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import atomicstryker.kenshiro.client.KenshiroClient;
import atomicstryker.kenshiro.common.KenshiroServer;
import atomicstryker.kenshiro.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class EntityKickedPacket implements IPacket
{
    
    private int dimension, playerID, entID;
    
    public EntityKickedPacket() {}
    
    public EntityKickedPacket(int a, int b, int c)
    {
        dimension = a;
        playerID = b;
        entID = c;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(dimension);
        bytes.writeInt(playerID);
        bytes.writeInt(entID);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        dimension = bytes.readInt();
        playerID = bytes.readInt();
        entID = bytes.readInt();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            World w = FMLClientHandler.instance().getClient().theWorld;
            Entity ep = w.getEntityByID(playerID);
            Entity ek = w.getEntityByID(entID);
            if (ep != null && ep instanceof EntityPlayer && ek != null && ek instanceof EntityLivingBase)
            {
                KenshiroClient.instance().onEntityKicked((EntityPlayer)ep, (EntityLivingBase)ek);
            }
        }
        else
        {
            World w = MinecraftServer.getServer().worldServerForDimension(dimension);
            if (w != null)
            {
                Entity ep = w.getEntityByID(playerID);
                Entity ek = w.getEntityByID(entID);
                if (ep != null && ep instanceof EntityPlayer && ek != null && ek instanceof EntityLivingBase)
                {
                    KenshiroServer.instance().onClientKickedEntity((EntityPlayer) ep, (EntityLivingBase) ek);
                }
            }
        }
    }

}