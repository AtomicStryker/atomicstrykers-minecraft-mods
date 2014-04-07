package atomicstryker.minions.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.minions.client.MinionsClient;
import atomicstryker.minions.common.network.NetworkHelper.IPacket;
import cpw.mods.fml.common.FMLCommonHandler;

public class MinionMountPacket implements IPacket
{
    
    public MinionMountPacket() {}
    
    private int minionID, targetID;
    
    public MinionMountPacket(int a, int b)
    {
        minionID = a;
        targetID = b;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            bytes.writeInt(minionID);
            bytes.writeInt(targetID);
        }
        else
        {
            
        }
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            minionID = bytes.readInt();
            targetID = bytes.readInt();
            MinionsClient.onMinionMountPacket(minionID, targetID);
        }
        else
        {

        }
    }

}
