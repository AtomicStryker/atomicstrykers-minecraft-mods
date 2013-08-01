package atomicstryker.findercompass.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.findercompass.client.AS_FinderCompass;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFinderCompass extends Item
{

    public ItemFinderCompass(int par1)
    {
        super(par1);
        setCreativeTab(CreativeTabs.tabTools);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister reg)
    {
        AS_FinderCompass tex = new AS_FinderCompass(Minecraft.getMinecraft());
        itemIcon = tex;
        ((TextureMap)reg).setTextureEntry("findercompass:compass"+AS_FinderCompass.tileSizeBase, tex);
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
    {
        if (world.isRemote)
        {
            AS_FinderCompass.switchSetting();
        }
        
        return itemStack;
    }
    
    @Override
    public String getItemDisplayName(ItemStack itemStack)
    {
        return "§E"+super.getItemDisplayName(itemStack);
    }

}
