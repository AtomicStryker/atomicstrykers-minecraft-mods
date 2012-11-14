package atomicstryker.minefactoryreloaded.common;

import atomicstryker.minefactoryreloaded.common.core.IMFRProxy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.World;

public class CommonProxy implements IMFRProxy
{
    @Override
    public void load()
    {
        // NOOP
    }

    @Override
    public void movePlayerToCoordinates(EntityPlayer e, double x, double y, double z)
    {
        if (e instanceof EntityPlayerMP)
        {
            ((EntityPlayerMP)e).playerNetServerHandler.setPlayerLocation(x,y,z, e.cameraYaw, e.cameraPitch);
        }
    }

    @Override
    public int getRenderId()
    {
        return 0;
    }
}
