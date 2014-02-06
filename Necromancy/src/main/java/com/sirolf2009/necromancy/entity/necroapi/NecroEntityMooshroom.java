package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.item.ItemBodyPart;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NecroEntityMooshroom extends NecroEntityCow
{

    public NecroEntityMooshroom()
    {
        super("Mooshroom", 12);
        headItem = ItemBodyPart.getItemStackFromName("Mooshroom Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Mooshroom Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Mooshroom Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Mooshroom Legs", 1);
        texture = new ResourceLocation("textures/entity/cow/mooshroom.png");
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Blocks.red_mushroom);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void preRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model)
    {
        RenderBlocks renderBlocks = new RenderBlocks();
        ClientProxy.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glEnable(GL11.GL_CULL_FACE);
        if (location == BodyPartLocation.Torso)
        {
            GL11.glPushMatrix();
            GL11.glScalef(1.0F, -1.0F, 1.0F);
            GL11.glTranslatef(0.4F, 0.4F, 0.0F);
            GL11.glRotatef(42.0F, 0.0F, 1.0F, 0.0F);
            renderBlocks.renderBlockAsItem(Blocks.red_mushroom, 0, 1.0F);
            GL11.glTranslatef(0.1F, 0.0F, -0.4F);
            GL11.glRotatef(42.0F, 0.0F, 1.0F, 0.0F);
            renderBlocks.renderBlockAsItem(Blocks.red_mushroom, 0, 1.0F);
            GL11.glPopMatrix();
        }
        if (location == BodyPartLocation.Head)
        {
            GL11.glPushMatrix();
            head[0].postRender(0.0625F);
            GL11.glScalef(1.0F, -1.0F, 1.0F);
            GL11.glTranslatef(0.0F, 0.75F, -0.1F);
            GL11.glRotatef(12.0F, 0.0F, 1.0F, 0.0F);
            renderBlocks.renderBlockAsItem(Blocks.red_mushroom, 0, 1.0F); // head
            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_CULL_FACE);
        ClientProxy.bindTexture(texture);
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
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.175D); // speed
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(1.0D); // health
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                         // res
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.175D); // speed
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Legs)
        {
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(1.0D); // health
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.35D); // speed
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
    }
}
