package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityQuadruped;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityPig extends NecroEntityQuadruped
{

    public NecroEntityPig()
    {
        super("Pig", 6);
        headItem = ItemBodyPart.getItemStackFromName("Pig Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Pig Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Pig Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Pig Legs", 1);
        texture = new ResourceLocation("textures/entity/pig/pig.png");
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.porkchop);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart head = new BodyPart(this, model, 0, 0);
        head.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, 0.0F);
        head.setTextureSize(textureWidth, textureHeight);
        BodyPart snout = new BodyPart(this, model, 16, 16);
        snout.addBox(-2.0F, 0.0F, -5.0F, 4, 3, 1, 0.0F);
        snout.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { head, snout };
    }

    @Override
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 1.0D, 16.0D, 0D, 0D, 0.0D);
        }
        else if (location == BodyPartLocation.Torso)
        {
            addAttributeMods(minion, "Torso", 6.0D, 0D, 0D, 0D, 0D);
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            addAttributeMods(minion, "ArmL", 1.0D, 0D, 0D, 0.175D, 0.5D);
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            addAttributeMods(minion, "ArmR", 1.0D, 0D, 0D, 0.175D, 0.5D);
        }
        else if (location == BodyPartLocation.Legs)
        {
            addAttributeMods(minion, "Legs", 1.0D, 0D, 0D, 0.35D, 0D);
        }
    }
}
