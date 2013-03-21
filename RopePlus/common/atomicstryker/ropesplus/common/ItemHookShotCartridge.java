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
    public void updateIcons(IconRegister iconRegister)
    {
        iconIndex = iconRegister.registerIcon("ropesplus:hscartridge");
    }

}
