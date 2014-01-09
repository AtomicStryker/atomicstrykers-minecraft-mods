package atomicstryker.infernalmobs.common.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;

public class VelocityPacket implements IPacket
{
    
    private float xv, yv, zv;
    
    public VelocityPacket() {}
    
    public VelocityPacket(float x, float y, float z)
    {
        xv = x;
        yv = y;
        zv = z;
    }

    @Override
    public void writeBytes(ByteBuf bytes)
    {
        bytes.writeFloat(xv);
        bytes.writeFloat(yv);
        bytes.writeFloat(zv);
    }

    @Override
    public void readBytes(ByteBuf bytes)
    {
        xv = bytes.readFloat();
        yv = bytes.readFloat();
        zv = bytes.readFloat();
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            FMLClientHandler.instance().getClient().thePlayer.addVelocity(xv, yv, zv);
        }
    }

}
