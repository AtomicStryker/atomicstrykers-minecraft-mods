package atomicstryker.ropesplus.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

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
    
    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.YELLOW+super.getItemStackDisplayName(itemStack);
    }
}
