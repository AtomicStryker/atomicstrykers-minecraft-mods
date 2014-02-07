package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
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
        super("CaveSpider");
        headItem = ItemBodyPart.getItemStackFromName("CaveSpider Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("CaveSpider Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("CaveSpider Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("CaveSpider Legs", 1);
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
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 2.0D, 16.0D, 0D, 0D, 2.0D);
        }
        else if (location == BodyPartLocation.Torso)
        {
            addAttributeMods(minion, "Torso", 12.0D, 0D, 0D, 0D, 0D);
        }
        else if (location == BodyPartLocation.Legs)
        {
            addAttributeMods(minion, "Legs", 2.0D, 0D, 0D, 0.8D, 0D);
        }
    }


}
