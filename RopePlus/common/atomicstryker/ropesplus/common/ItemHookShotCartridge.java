package atomicstryker.ropesplus.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemHookShotCartridge extends Item
{

    public ItemHookShotCartridge(int id)
    {
        super(id);
    }
    
    @Override
    public void func_94581_a(IconRegister iconRegister)
    {
        iconIndex = iconRegister.func_94245_a("ropesplus:hscartridge");
    }

}
