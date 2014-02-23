package com.sirolf2009.necromancy.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.sirolf2009.necromancy.Necromancy;

public class ItemIsaacsHead extends ItemArmor
{
    
    public static IIcon tearIcon;
    public static IIcon tearBloodIcon;

    public ItemIsaacsHead(ArmorMaterial material, int par3, int par4)
    {
        super(material, par3, par4);
        setUnlocalizedName("IsaacsHead");
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
    {
        return "necromancy:textures/models/armor/isaacarmor.png";
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("necromancy:isaacsseveredhead");
        tearIcon = iconRegister.registerIcon("necromancy:tear");
        tearBloodIcon = iconRegister.registerIcon("necromancy:tearblood");
    }

}
