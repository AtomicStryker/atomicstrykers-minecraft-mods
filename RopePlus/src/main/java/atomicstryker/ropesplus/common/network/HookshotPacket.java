package atomicstryker.ropesplus.common.network;

import atomicstryker.ropesplus.client.RopesPlusClient;
import atomicstryker.ropesplus.common.RopesPlusCore;
import atomicstryker.ropesplus.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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
        //System.out.printf("HookshotPacket [%d|%d|%d], ropeID %d\n", x, y, z, ropeEntID);
        
        if (ropeEntID < 0)
        {
            RopesPlusCore.proxy.setHasClientRopeOut(false);
            RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
            RopesPlusCore.proxy.setShouldRopeChangeState(0f);
            
            RopesPlusClient.onReleasedHookshot();
        }
        else
        {
            RopesPlusCore.proxy.setHasClientRopeOut(true);
            RopesPlusCore.proxy.setShouldHookShotDisconnect(false);
            RopesPlusCore.proxy.setShouldRopeChangeState(0f);
            RopesPlusClient.onAffixedToHookShotRope(ropeEntID);
            RopesPlusClient.onHookshotHit(x, y, z);
        }
    }

}
