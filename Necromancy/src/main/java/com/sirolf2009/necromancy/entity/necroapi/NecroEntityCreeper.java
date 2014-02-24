package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityCreeper extends NecroEntityBase
{

    public NecroEntityCreeper()
    {
        super("Creeper");
        headItem = new ItemStack(Items.skull, 1, 4);
        torsoItem = ItemBodyPart.getItemStackFromName("Creeper Torso", 1);
        legItem = ItemBodyPart.getItemStackFromName("Creeper Legs", 1);
        texture = new ResourceLocation("textures/entity/creeper/creeper.png");
        hasArms = false;
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.gunpowder);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart head = new BodyPart(this, model, 0, 0);
        head.addBox(-4, -4, -4, 8, 8, 8, 0.0F);
        head.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { head };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] headPos = { 4.0F, -4.0F, 2.0F };
        float[] armLeftPos = { -4F, 0.0F, 2.0F };
        float[] armRightPos = { 8F, 0.0F, 2.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 16, 16);
        torso.addBox(0.0F, 0.0F, 0.0F, 8, 12, 4, 0.0F);
        torso.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { torso };
    }

    @Override
    public BodyPart[] initLegs(ModelBase model)
    {
        float[] torsoPos = { -4F, 4F, -2F };
        BodyPart leg1 = new BodyPart(this, torsoPos, model, 0, 16);
        leg1.addBox(0.0F, 16.0F, 2.0F, 4, 6, 4, 0.0F);
        BodyPart leg2 = new BodyPart(this, torsoPos, model, 0, 16);
        leg2.addBox(-4.0F, 16.0F, 2.0F, 4, 6, 4, 0.0F);
        BodyPart leg3 = new BodyPart(this, torsoPos, model, 0, 16);
        leg3.addBox(-4.0F, 16.0F, -6.0F, 4, 6, 4, 0.0F);
        BodyPart leg4 = new BodyPart(this, torsoPos, model, 0, 16);
        leg4.addBox(0.0F, 16.0F, -6.0F, 4, 6, 4, 0.0F);
        return new BodyPart[] { leg1, leg2, leg3, leg4 };
    }

    @Override
    public BodyPart[] initArmLeft(ModelBase model)
    {
        return null;
    }

    @Override
    public BodyPart[] initArmRight(ModelBase model)
    {
        return null;
    }

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity, BodyPart[] bodypart, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            bodypart[0].rotateAngleY = par4 / (180F / (float) Math.PI);
            bodypart[0].rotateAngleX = par5 / (180F / (float) Math.PI);
        }
        if (location == BodyPartLocation.Legs)
        {
            bodypart[0].rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
            bodypart[1].rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float) Math.PI) * 1.4F * par2;
            bodypart[0].rotateAngleY = 0.0F;
            bodypart[1].rotateAngleY = 0.0F;
        }
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
        else if (location == BodyPartLocation.Legs)
        {
            addAttributeMods(minion, "Legs", 0.25D, 0D, 3D, 3D, 0D);
        }
    }
}
