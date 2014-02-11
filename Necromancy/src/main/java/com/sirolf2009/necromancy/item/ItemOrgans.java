package com.sirolf2009.necromancy.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.IIcon;

import com.sirolf2009.necromancy.Necromancy;

public class ItemOrgans extends ItemFood
{
    private IIcon[] icons;
    private final static String names[] = { "Brains", "Heart", "Muscle", "Lungs", "Skin" };

    public ItemOrgans()
    {
        super(2, true);
        setPotionEffect(Potion.hunger.id, 30, 0, 0.8F);
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancy);
        icons = new IIcon[names.length];
    }

    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack)
    {
        if (par1ItemStack.getItemDamage() > names.length)
            return "Invalid Item, please destroy";
        return new StringBuilder().append("").append(names[par1ItemStack.getItemDamageForDisplay()]).toString();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int var4 = 0; var4 < names.length; var4++)
        {
            par3List.add(new ItemStack(par1, 1, var4));
        }
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        for (int index = 0; index < names.length; index++)
        {
            icons[index] = iconRegister.registerIcon("necromancy:" + names[index].toLowerCase());
        }
    }

    @Override
    public IIcon getIconFromDamage(int par1)
    {
        if (par1 < icons.length)
            return icons[par1];
        else
            return null;
    }
}
