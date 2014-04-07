package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityQuadruped;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntitySheep extends NecroEntityQuadruped
{

    public NecroEntitySheep()
    {
        super("Sheep", 12);
        headItem = ItemBodyPart.getItemStackFromName("Sheep Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Sheep Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Sheep Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Sheep Legs", 1);
        texture = new ResourceLocation("textures/entity/sheep/sheep.png");
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Blocks.wool);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart head = new BodyPart(this, model, 0, 0);
        head.addBox(-4.0F, -4.0F, -4.0F, 6, 6, 8, 0.0F);
        head.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { head };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] headPos = { 4.0F, 12 - size, -14.0F };
        float[] armLeftPos = { -1.0F, 6.0F, -10.0F };
        float[] armRightPos = { 5F, 6.0F, -10.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 28, 8);
        torso.addBox(0.0F, -10.0F, -6.0F, 8, 16, 6, 0.0F);
        torso.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { torso };
    }

    @Override
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 0.5D, 1D, 0D, 0D, 0D);
        }
        else if (location == BodyPartLocation.Torso)
        {
            addAttributeMods(minion, "Torso", 1D, 0D, 0D, 0D, 0D);
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            addAttributeMods(minion, "ArmL", 0.25D, 0D, 0D, 0D, 0.25D);
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            addAttributeMods(minion, "ArmL", 0.25D, 0D, 0D, 0D, 0.25D);
        }
        else if (location == BodyPartLocation.Legs)
        {
            addAttributeMods(minion, "Legs", 0.25D, 0D, 1D, 3D, 0D);
        }
    }
}
