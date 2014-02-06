package com.sirolf2009.necromancy.item;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import com.sirolf2009.necroapi.ISkull;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.Necromancy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemNecroSkull extends ItemSkull
{

    public static String[] IIconTextures;
    public static String[] modelTextures;
    public static String[] skullTypes;
    public IIcon[] IIcons;

    public ItemNecroSkull()
    {
        super();
        this.setCreativeTab(Necromancy.tabNecromancy);
    }

    public static void initSkulls()
    {
        Iterator<ISkull> itr = NecroEntityRegistry.registeredSkullEntities.values().iterator();
        IIconTextures = new String[NecroEntityRegistry.registeredSkullEntities.size()];
        modelTextures = new String[NecroEntityRegistry.registeredSkullEntities.size()];
        skullTypes = new String[NecroEntityRegistry.registeredSkullEntities.size()];
        int i = 0;
        while (itr.hasNext())
        {
            ISkull mob = itr.next();
            IIconTextures[i] = mob.getSkullIIconTexture();
            modelTextures[i] = mob.getSkullModelTexture();
            skullTypes[i] = ((NecroEntityBase) mob).mobName;
            // LanguageRegistry.addName(new ItemStack(Necromancy.skull, 1, i),
            // skullTypes[i]+" Skull");
            i++;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int j = 0; j < skullTypes.length; ++j)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

    @Override
    public IIcon getIconFromDamage(int par1)
    {
        if (par1 < 0 || par1 >= skullTypes.length)
        {
            par1 = 0;
        }

        return IIcons[par1];
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        int i = par1ItemStack.getItemDamage();

        if (i < 0 || i >= skullTypes.length)
        {
            i = 0;
        }

        return super.getUnlocalizedName() + "." + skullTypes[i];
    }

    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack)
    {
        return par1ItemStack.getItemDamage() == 3 && par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey("SkullOwner")
                ? StatCollector.translateToLocalFormatted("Items.skull.player.name",
                        new Object[] { par1ItemStack.getTagCompound().getString("SkullOwner") }) : super.getItemStackDisplayName(par1ItemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IIconRegister)
    {
        IIcons = new IIcon[IIconTextures.length];

        for (int i = 0; i < IIconTextures.length; ++i)
        {
            IIcons[i] = par1IIconRegister.registerIcon(IIconTextures[i]);
        }
    }
}
