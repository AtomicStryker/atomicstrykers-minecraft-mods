package atomicstryker.multimine.common;

import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;

public class CommonProxy
{
    public void onPreInit()
    {
        new MultiMineServer();
    }

    public void onLoad()
    {
        // NOOP
    }

    public File getConfigFile() {
        return ServerLifecycleHooks.getCurrentServer().getFile("\\config\\multimine.cfg");
    }
}
