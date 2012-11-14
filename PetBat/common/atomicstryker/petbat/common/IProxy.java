package atomicstryker.petbat.common;

import net.minecraft.src.ItemStack;

public interface IProxy
{
    public void onModLoad();

    public void displayGui(ItemStack itemStack);
}
