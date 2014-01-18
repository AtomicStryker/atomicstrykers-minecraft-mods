package atomicstryker.ropesplus.common;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemHookShotCartridge extends Item
{

    public ItemHookShotCartridge()
    {
        super();
        setCreativeTab(CreativeTabs.tabTools);
    }
    
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("ropesplus:hscartridge");
    }

}
