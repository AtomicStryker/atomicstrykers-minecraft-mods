package atomicstryker.ropesplus.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import atomicstryker.ropesplus.client.RopesPlusClient;
import atomicstryker.ropesplus.common.EntityFreeFormRope;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;

public class HookshotPacket implements IPacket
{
    
    private int ropeEntID, x, y, z;
    
    public HookshotPacket() {}
    
    public HookshotPacket(int entid, int ix, int iy, int iz)
    {
        ropeEntID = entid;
        x = ix;
        y = iy;
        z = iz;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        bytes.writeInt(ropeEntID);
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ropeEntID = bytes.readInt();
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        
        if (ropeEntID < 0)
        {
            RopesPlusCore.proxy.setHasClientRopeOut(false);
            RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
            RopesPlusCore.proxy.setShouldHookShotPull(0f);
            
            EntityPlayer p = Minecraft.getMinecraft().thePlayer;
            for (Object o : p.worldObj.loadedEntityList)
            {
                if (o instanceof EntityFreeFormRope)
                {
                    EntityFreeFormRope rope = (EntityFreeFormRope) o;
                    if (rope.getShooter() != null && rope.getShooter().equals(p))
                    {
                        rope.setDead();
                        break;
                    }
                }
            }
        }
        else
        {
            RopesPlusCore.proxy.setHasClientRopeOut(true);
            RopesPlusCore.proxy.setShouldHookShotDisconnect(false);
            RopesPlusCore.proxy.setShouldHookShotPull(0f);
            RopesPlusClient.onAffixedToHookShotRope(ropeEntID);
            Minecraft.getMinecraft().theWorld.spawnParticle("largeexplode", x+0.5D, y, z+0.5D, 1.0D, 0.0D, 0.0D);
        }
    }

}
