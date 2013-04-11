package atomicstryker.ropesplus.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemRope extends Item
{
    
    public ItemRope(int i)
    {
        super(i);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("ropesplus:itemRope");
    }
    
}
