package atomicstryker.updatecheck.common;

import cpw.mods.fml.common.FMLCommonHandler;

public class UpdateCheckServer implements IProxy
{

    @Override
    public void announce(String announcement)
    {
        FMLCommonHandler.instance().getMinecraftServerInstance().logInfo(announcement);
    }

}
