package atomicstryker.ropesplus.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import atomicstryker.ropesplus.client.RopesPlusClient;
import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;

public class ZiplinePacket implements IPacket
{
    
    private String username;
    private int ziplineEntID;
    private float relativeDist;
    
    public ZiplinePacket() {}
    
    public ZiplinePacket(String user, int zipEntID, float relDist)
    {
        username = user;
        ziplineEntID = zipEntID;
        relativeDist = relDist;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, username);
        bytes.writeInt(ziplineEntID);
        bytes.writeFloat(relativeDist);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        username = ByteBufUtils.readUTF8String(bytes);
        ziplineEntID = bytes.readInt();
        relativeDist = bytes.readFloat();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            RopesPlusClient.onUsedZipLine(ziplineEntID);
        }
        else
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
            if (p != null)
            {
                Entity target = p.worldObj.getEntityByID(ziplineEntID);
                if (target != null && target instanceof EntityFreeFormRope)
                {
                    double[] coords = ((EntityFreeFormRope) target).getCoordsAtRelativeLength(relativeDist);
                    // System.out.println("server got pos update: "+coords[0]+", "+coords[1]+", "+coords[2]);
                    p.setPositionAndUpdate(coords[0], coords[1] - 2.5D, coords[2]);
                    p.fallDistance = 0;
                }
            }
        }
    }

}
