package atomicstryker.infernalmobs.common.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;

public class KnockBackPacket implements IPacket
{
    
    private float xv, zv;
    
    public KnockBackPacket() {}
    
    public KnockBackPacket(float x, float z)
    {
        xv = x;
        zv = z;
    }

    @Override
    public void writeBytes(ByteBuf bytes)
    {
        bytes.writeFloat(xv);
        bytes.writeFloat(zv);
    }

    @Override
    public void readBytes(ByteBuf bytes)
    {
        xv = bytes.readFloat();
        zv = bytes.readFloat();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            MM_Gravity.knockBack(FMLClientHandler.instance().getClient().thePlayer, xv, zv);
        }
    }

}
