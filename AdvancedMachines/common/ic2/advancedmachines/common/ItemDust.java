package ic2.advancedmachines.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemDust extends Item
{
    
    public ItemDust(int index)
    {
        super(index);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("advancedmachines:refinedIronDust");
    }
    
}
