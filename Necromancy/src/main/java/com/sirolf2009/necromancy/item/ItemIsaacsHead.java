package com.sirolf2009.necromancy.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.sirolf2009.necromancy.Necromancy;

public class ItemIsaacsHead extends ItemArmor
{

    public ItemIsaacsHead(ArmorMaterial material, int par3, int par4)
    {
        super(material, par3, par4);
        setUnlocalizedName("IsaacsHead");
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
    {
        return "/armor/Isaac_1.png";
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("necromancy:isaacsseveredhead");
    }

}
