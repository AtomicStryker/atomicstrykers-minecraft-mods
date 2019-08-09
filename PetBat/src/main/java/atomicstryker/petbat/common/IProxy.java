package atomicstryker.petbat.common;

import atomicstryker.petbat.common.network.BatNamePacket;
import net.minecraft.item.ItemStack;

import java.io.File;

public interface IProxy {

    void onModPreInit();

    void onClientInit();

    void displayGui(ItemStack itemStack);

    void onBatNamePacket(BatNamePacket packet);

    File getMcFolder();
}
