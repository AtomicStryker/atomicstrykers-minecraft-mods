package com.sirolf2009.necromancy.item;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.core.proxy.ClientProxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

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
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase)
    {
        par1ItemStack.damageItem(1, par3EntityLivingBase);
        if (par2EntityLivingBase.getHealth() <= 0)
            if (((EntityPlayer) par3EntityLivingBase).inventory.consumeInventoryItem(Items.glass_bottle))
            {
                ((EntityPlayer) par3EntityLivingBase).inventory.addItemStackToInventory(ItemGeneric.getItemStackFromName("Soul in a Jar"));
                if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
                {
                    Random rand = new Random();
                    for (int i = 0; i < 30; i++)
                    {
                        ClientProxy.spawnParticle("skull", par2EntityLivingBase.posX, par2EntityLivingBase.posY, par2EntityLivingBase.posZ,
                                rand.nextDouble() / 360 * 10, rand.nextDouble() / 360 * 10, rand.nextDouble() / 360 * 10);
                    }
                }
                par2EntityLivingBase.motionY = 10000;
            }
        super.hitEntity(par1ItemStack, par2EntityLivingBase, par3EntityLivingBase);
        return false;
    }
}
