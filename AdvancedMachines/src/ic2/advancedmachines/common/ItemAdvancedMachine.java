package ic2.advancedmachines.common;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemAdvancedMachine extends ItemBlock
{
    public ItemAdvancedMachine(int var1)
    {
        super(var1);
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public int getMetadata(int var1)
    {
        return var1;
    }
    
    @Override
    public String getItemDisplayName(ItemStack itemStack)
    {
        return EnumChatFormatting.GOLD+super.getItemDisplayName(itemStack);
    }

    @Override
    public String getUnlocalizedName(ItemStack var1)
    {
        int var2 = var1.getItemDamage();
        switch (var2)
        {
            case 0:
                return "item.advancedmachines:rotaryMacerator";
            case 1:
                return "item.advancedmachines:singularityCompressor";
            case 2:
                return "item.advancedmachines:centrifugeExtractor";
            default:
                return null;
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
    }
}
