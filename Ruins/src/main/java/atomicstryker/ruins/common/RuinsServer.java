package atomicstryker.ruins.common;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.io.File;

public class RuinsServer implements IProxy {
    @Override
    public File getBaseDir() {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getFile("");
    }
}
