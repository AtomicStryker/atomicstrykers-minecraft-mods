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
    public void func_94581_a(IconRegister iconRegister)
    {
        iconIndex = iconRegister.func_94245_a("advancedmachines:refinedIronDust");
    }
    
}
