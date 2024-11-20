package atomicstryker.ruins.common;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;

public class RuinsServer implements IProxy {
    @Override
    public File getBaseDir() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getFile("").toFile();
    }
}
