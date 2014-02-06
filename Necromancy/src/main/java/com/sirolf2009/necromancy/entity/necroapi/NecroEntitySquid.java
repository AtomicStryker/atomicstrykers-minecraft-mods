package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntitySquid extends NecroEntityBase
{

    public NecroEntitySquid()
    {
        super("Squid");
        headItem = ItemBodyPart.getItemStackFromName("Squid Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Squid Torso", 1);
        legItem = ItemBodyPart.getItemStackFromName("Squid Legs", 1);
        texture = new ResourceLocation("textures/entity/squid.png");
        hasArms = false;
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(new ItemStack(Items.dye, 1, 0));
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart squidBody = new BodyPart(this, model, 0, 0);
        squidBody.addBox(-6.0F, -20.0F, -6.0F, 12, 16, 12);
        squidBody.rotationPointY += 8F;
        return new BodyPart[] { squidBody };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] armLeftPos = { -6, -4, 0 };
        float[] armRightPos = { 10, -4, 0 };
        float[] headPos = { 4, -8, 0 };
        BodyPart squidBody = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 0, 0);
        squidBody.addBox(-2.0F, -12.0F, -5.0F, 12, 16, 12);
        squidBody.rotationPointY += 8F;
        return new BodyPart[] { squidBody };
    }

    @Override
    public BodyPart[] initLegs(ModelBase model)
    {
        float[] torsoPos = { -4, -7, -3 };
        BodyPart[] bodyParts = new BodyPart[8];
        for (int i = 0; i < bodyParts.length; ++i)
        {
            bodyParts[i] = new BodyPart(this, torsoPos, model, 48, 0);
            double d0 = i * Math.PI * 2.0D / bodyParts.length;
            float f = (float) Math.cos(d0) * 5.0F;
            float f1 = (float) Math.sin(d0) * 5.0F;
            bodyParts[i].addBox(-1.0F, -10.0F, -1.0F, 2, 18, 2);
            bodyParts[i].rotationPointX = f;
            bodyParts[i].rotationPointZ = f1;
            bodyParts[i].rotationPointY = 31 + -16;
            d0 = i * Math.PI * -2.0D / bodyParts.length + Math.PI / 2D;
            bodyParts[i].rotateAngleY = (float) d0;
        }
        return bodyParts;
    }

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, BodyPart[] part, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Legs)
        {
            for (BodyPart parts : part)
            {
                parts.rotateAngleX = par3;
            }
        }
    }

    @Override
    public void setAttributes(EntityLivingBase minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(2.0D); // health
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(16.0D); // followrange
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Torso)
        {
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(6.0D); // health
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                      // res
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Legs)
        {
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(2.0D); // health
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.7D); // speed
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
    }
}
