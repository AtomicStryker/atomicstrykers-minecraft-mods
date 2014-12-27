package atomicstryker.petbat.common;

import net.minecraft.item.ItemStack;

public interface IProxy
{
    public void onModInit();

    public void displayGui(ItemStack itemStack);
}
