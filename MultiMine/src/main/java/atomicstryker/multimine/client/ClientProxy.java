package atomicstryker.multimine.client;

import atomicstryker.multimine.common.CommonProxy;
import net.minecraft.client.Minecraft;

import java.io.File;

public class ClientProxy extends CommonProxy {
    @Override
    public void onLoad() {
        new MultiMineClient();
    }

    @Override
    public File getConfigFile() {
        return new File(Minecraft.getInstance().gameDir, "\\config\\multimine.cfg");
    }
}
