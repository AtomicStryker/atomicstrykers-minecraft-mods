package atomicstryker.findercompass.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import atomicstryker.findercompass.client.FinderCompassClientTicker;

public class ItemFinderCompass extends Item
{

    public ItemFinderCompass()
    {
        super();
        setCreativeTab(CreativeTabs.tabTools);
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
    {
        if (world.isRemote)
        {
            FinderCompassClientTicker.instance.switchSetting();
        }
        
        return itemStack;
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.GOLD+super.getItemStackDisplayName(itemStack);
    }

}
