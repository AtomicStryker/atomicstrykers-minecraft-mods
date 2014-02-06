package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
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
    public void setAttributes(EntityLivingBase minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(1.0D); // health
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
        else if (location == BodyPartLocation.ArmLeft)
        {
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(1.0D); // health
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                        // res
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0625D); // speed
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(1.0D); // health
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                         // res
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0625D); // speed
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Legs)
        {
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(1.0D); // health
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.125D); // speed
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
    }
}
