package atomicstryker.ruins.common;

import net.minecraft.client.Minecraft;

import java.io.File;

public class RuinsClient implements IProxy {
    @Override
    public File getBaseDir() {
        final Minecraft minecraft = Minecraft.getInstance();
        return minecraft.gameDir;
    }
}
