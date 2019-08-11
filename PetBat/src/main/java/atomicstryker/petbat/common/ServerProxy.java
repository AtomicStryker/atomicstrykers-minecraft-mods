package atomicstryker.petbat.common;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;

public class ServerProxy implements IProxy {

    @Override
    public void onClientInit() {
        // NOOP
    }

    @Override
    public void displayGui(ItemStack itemStack) {
        // NOOP, Proxy only relevant on client
    }

    @Override
    public void onModPreInit() {

    }

    @Override
    public File getMcFolder() {
        return ServerLifecycleHooks.getCurrentServer().getFile("");
    }
}
