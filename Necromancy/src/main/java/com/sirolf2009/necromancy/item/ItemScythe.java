package com.sirolf2009.necromancy.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;

import com.sirolf2009.necromancy.Necromancy;

public class ItemScythe extends ItemSword
{

    public static ToolMaterial toolScythe = EnumHelper.addToolMaterial("BLOODSCYTHE", 0, 666, 7F, 2, 9);
    public static ToolMaterial toolScytheBone = EnumHelper.addToolMaterial("BLOODSCYTHEBONE", 0, 666, 7F, 4, 9);

    public ItemScythe(ToolMaterial material)
    {
        super(material);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase target, EntityLivingBase player)
    {
        par1ItemStack.damageItem(1, player);
        if (target.getHealth() <= 0)
            if (((EntityPlayer) player).inventory.consumeInventoryItem(Items.glass_bottle))
            {
                ((EntityPlayer) player).inventory.addItemStackToInventory(ItemGeneric.getItemStackFromName("Soul in a Jar"));
                if (target.worldObj.isRemote)
                {
                    for (int i = 0; i < 30; i++)
                    {
                        Necromancy.proxy.spawnParticle("skull", target.posX, target.posY, target.posZ, target.getRNG().nextDouble() / 360 * 10,
                                target.getRNG().nextDouble() / 360 * 10, target.getRNG().nextDouble() / 360 * 10);
                    }
                }
                target.motionY = 10000;
            }
        super.hitEntity(par1ItemStack, target, player);
        return false;
    }
}
