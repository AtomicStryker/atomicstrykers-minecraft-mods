package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBiped;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityZombie extends NecroEntityBiped
{

    public NecroEntityZombie()
    {
        super("Zombie");
        headItem = new ItemStack(Items.skull, 1, 2);
        torsoItem = ItemBodyPart.getItemStackFromName("Zombie Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Zombie Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Zombie Legs", 1);
        texture = new ResourceLocation("textures/entity/zombie/zombie.png");
        textureHeight = 64;
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.rotten_flesh);
    }

    @Override
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 2.0D, 32.0D, 0D, 0D, 1.0D);
        }
        else if (location == BodyPartLocation.Torso)
        {
            addAttributeMods(minion, "Torso", 12.0D, 0D, 0D, 0D, 0D);
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            addAttributeMods(minion, "ArmL", 2.0D, 0D, 0D, 0D, 1.0D);
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            addAttributeMods(minion, "ArmR", 2.0D, 0D, 0D, 0D, 1.0D);
        }
        else if (location == BodyPartLocation.Legs)
        {
            addAttributeMods(minion, "Legs", 2.0D, 0D, 0D, 0.23D, 0D);
        }
    }
}
