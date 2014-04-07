package com.sirolf2009.necromancy.creativetab;

import net.minecraft.init.Items;
import net.minecraft.item.Item;

import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

public final class CreativeTabNecro extends net.minecraft.creativetab.CreativeTabs
{

    private int display;

    public CreativeTabNecro(int par1, String par2Str, int item)
    {
        super(par1, par2Str);
        display = item;
    }

    @Override
    public Item getTabIconItem()
    {
        if (display == 1)
            return RegistryNecromancyItems.necronomicon;
        else
            return Items.skull;
    }
}
