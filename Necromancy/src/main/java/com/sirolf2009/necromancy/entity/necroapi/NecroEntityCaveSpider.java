package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necromancy.item.ItemBodyPart;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NecroEntityCaveSpider extends NecroEntitySpider
{

    public NecroEntityCaveSpider()
    {
        super("Cave Spider");
        headItem = ItemBodyPart.getItemStackFromName("Cave_Spider Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Cave_Spider Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Cave_Spider Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Cave_Spider Legs", 1);
        texture = new ResourceLocation("textures/entity/spider/cave_spider.png");
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.string);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void preRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model)
    {
        GL11.glPushMatrix();
        GL11.glScalef(0.7F, 0.7F, 0.7F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void postRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model)
    {
        GL11.glPopMatrix();
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
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D); // damage
        }
        else if (location == BodyPartLocation.Torso)
        {
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(12.0D); // health
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
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.8D); // speed
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
    }
}
