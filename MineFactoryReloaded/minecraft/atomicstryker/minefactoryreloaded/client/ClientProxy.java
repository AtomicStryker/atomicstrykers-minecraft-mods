package atomicstryker.minefactoryreloaded.client;

import atomicstryker.minefactoryreloaded.common.core.IMFRProxy;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class ClientProxy implements IMFRProxy
{
    @Override
    public void load()
    {
        new MineFactoryClient();
    }

    @Override
    public void movePlayerToCoordinates(EntityPlayer e, double x, double y, double z)
    {
        e.moveEntity(x, y, z);
    }

    @Override
    public int getRenderId()
    {
        return MineFactoryClient.instance().renderId;
    }
}
