package atomicstryker.petbat.common;

import net.minecraft.item.ItemStack;

import java.io.File;

public interface IProxy {

    void onModPreInit();

    void onClientInit();

    void displayGui(ItemStack itemStack);

    File getMcFolder();
}
