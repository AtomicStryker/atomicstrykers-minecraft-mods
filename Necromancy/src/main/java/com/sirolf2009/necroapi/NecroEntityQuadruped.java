package com.sirolf2009.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

/**
 * The base class for all the quadruped necro mobs
 * 
 * @author sirolf2009
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public abstract class NecroEntityQuadruped extends NecroEntityBase
{
    
    /** the height of the mob, default is 12 and relates to cows */
    public int size = 12;

    public NecroEntityQuadruped(String mobName, int size)
    {
        super(mobName);
        this.mobName = mobName;
        this.size = size;
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart head = new BodyPart(this, model, 0, 0);
        head.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, 0.0F);
        head.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { head };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] headPos = { 4.0F, 12 - size, -14.0F };
        float[] armLeftPos = { -1.0F, 12.0F, -10.0F };
        float[] armRightPos = { 5F, 12.0F, -10.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 28, 8);
        torso.addBox(-1.0F, -12.0F, -12.0F, 10, 16, 8, 0.0F);
        torso.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { torso };
    }

    @Override
    public BodyPart[] initArmLeft(ModelBase model)
    {
        BodyPart armLeft = new BodyPart(this, model, 0, 16);
        armLeft.addBox(0.0F, 0.0F, -1.0F, 4, size, 4, 0.0F);
        armLeft.setTextureSize(textureWidth, textureHeight);
        armLeft.mirror = true;
        return new BodyPart[] { armLeft };
    }

    @Override
    public BodyPart[] initArmRight(ModelBase model)
    {
        BodyPart armRight = new BodyPart(this, model, 0, 16);
        armRight.addBox(0.0F, 0.0F, -1.0F, 4, size, 4, 0.0F);
        armRight.setTextureSize(textureWidth, textureHeight);
        return new BodyPart[] { armRight };
    }

    @Override
    public BodyPart[] initLegs(ModelBase model)
    {
        float[] torsoPos = { -4F, 4F, 0F };
        BodyPart legLeft = new BodyPart(this, torsoPos, model, 0, 16);
        legLeft.addBox(-2.0F, 0.0F, -2.0F, 4, size, 4, 0.0F);
        legLeft.setRotationPoint(-3.0F, (float) 22 - size, 3.0F);
        BodyPart legRight = new BodyPart(this, torsoPos, model, 0, 16);
        legRight.addBox(-2.0F, 0.0F, -2.0F, 4, size, 4, 0.0F);
        legRight.setRotationPoint(3.0F, (float) 22 - size, 3.0F);
        legLeft.setTextureSize(textureWidth, textureHeight);
        legRight.setTextureSize(textureWidth, textureHeight);
        legLeft.mirror = true;
        return new BodyPart[] { legLeft, legRight };
    }

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity, BodyPart[] bodypart, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            bodypart[0].rotateAngleX = par5 / (180F / (float) Math.PI);
            bodypart[0].rotateAngleY = par5 / (180F / (float) Math.PI);
        }
        if (location == BodyPartLocation.Torso)
        {
            bodypart[0].rotateAngleX = (float) Math.PI / 2F;
        }
        if (location == BodyPartLocation.ArmLeft)
        {
            bodypart[0].rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float) Math.PI) * 1.4F * par2;
            bodypart[0].rotateAngleZ = 0.0F;
        }
        if (location == BodyPartLocation.ArmRight)
        {
            bodypart[0].rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
            bodypart[0].rotateAngleZ = 0.0F;
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
    public void preRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model)
    {
        if ((entity.getDataWatcher().getWatchableObjectByte(16) & 1) != 0 && location == BodyPartLocation.Torso)
        {
            GL11.glRotatef(-90, 1, 0, 0);
            GL11.glTranslatef(0, -0.5F, 1);
        }
    }
    
}
