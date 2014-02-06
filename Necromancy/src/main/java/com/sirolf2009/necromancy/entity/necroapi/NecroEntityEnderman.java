package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBiped;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityEnderman extends NecroEntityBiped
{

    public NecroEntityEnderman()
    {
        super("Enderman");
        headItem = ItemBodyPart.getItemStackFromName("Enderman Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Enderman Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Enderman Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Enderman Legs", 1);
        texture = new ResourceLocation("textures/entity/enderman/enderman.png");
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.ender_pearl);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart head = new BodyPart(this, model, 0, 0);
        head.addBox(-4, -7, -4, 8, 8, 8, 0.0F);
        head.setTextureSize(textureWidth, textureHeight);
        BodyPart bipedHeadwear = new BodyPart(this, model, 0, 16);
        bipedHeadwear.addBox(-4.0F, -3.0F, -4.0F, 8, 8, 8, -0.5F);
        return new BodyPart[] { head, bipedHeadwear };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] headPos = { 4.0F, -4.0F, 2.0F };
        float[] armLeftPos = { -4F, 0.0F, 2.0F };
        float[] armRightPos = { 8F, 0.0F, 2.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 32, 16);
        torso.addBox(0.0F, 0.0F, 0.0F, 8, 12, 4, 0.0f);
        return new BodyPart[] { torso };
    }

    @Override
    public BodyPart[] initLegs(ModelBase model)
    {
        float[] torsoPos = { -4F, -18F, 0F };
        BodyPart legRight = new BodyPart(this, torsoPos, model, 56, 0);
        legRight.addBox(-1.0F, -4.0F, 1.0F, 2, 30, 2, 0.0F);
        legRight.setRotationPoint(-2.0F, -2.0F, 0.0F);
        BodyPart legLeft = new BodyPart(this, torsoPos, model, 56, 0);
        legLeft.mirror = true;
        legLeft.addBox(-1.0F, -4.0F, 1.0F, 2, 30, 2, 0.0F);
        legLeft.setRotationPoint(2.0F, -2.0F, 0.0F);
        return new BodyPart[] { legLeft, legRight };
    }

    @Override
    public BodyPart[] initArmLeft(ModelBase model)
    {
        BodyPart bipedLeftArm = new BodyPart(this, model, 56, 0);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(2.0F, 0.0F, -1.0F, 2, 30, 2, 0.0F);
        return new BodyPart[] { bipedLeftArm };
    }

    @Override
    public BodyPart[] initArmRight(ModelBase model)
    {
        BodyPart bipedRightArm = new BodyPart(this, model, 56, 0);
        bipedRightArm.addBox(0.0F, 0.0F, -1.0F, 2, 30, 2, 0.0F);
        return new BodyPart[] { bipedRightArm };
    }

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, BodyPart[] part, BodyPartLocation location)
    {
        super.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, part, location);
        if (location == BodyPartLocation.Head)
        {
            part[0].rotationPointZ = -0.0F;
            part[0].rotationPointY = -0.0F;
            part[1].rotationPointX = part[0].rotationPointX;
            part[1].rotationPointY = part[0].rotationPointY;
            part[1].rotationPointZ = part[0].rotationPointZ;
            part[1].rotateAngleX = part[0].rotateAngleX;
            part[1].rotateAngleY = part[0].rotateAngleY;
            part[1].rotateAngleZ = part[0].rotateAngleZ;
        }
        if (location == BodyPartLocation.Legs)
        {
            part[0].rotateAngleX -= 0.0F;
            part[1].rotateAngleX -= 0.0F;
            part[0].rotateAngleX = (float) (part[0].rotateAngleX * 0.5D);
            part[1].rotateAngleX = (float) (part[1].rotateAngleX * 0.5D);
            if (part[0].rotateAngleX > 0.4F)
            {
                part[0].rotateAngleX = 0.4F;
            }
            if (part[0].rotateAngleX < -0.4F)
            {
                part[0].rotateAngleX = -0.4F;
            }
            if (part[1].rotateAngleX > 0.4F)
            {
                part[1].rotateAngleX = 0.4F;
            }
            if (part[1].rotateAngleX < -0.4F)
            {
                part[1].rotateAngleX = -0.4F;
            }
        }
        if (location == BodyPartLocation.ArmRight)
        {
            part[0].rotateAngleX = (float) (part[0].rotateAngleX * 0.5D);
            if (part[0].rotateAngleX > 0.4F)
            {
                part[0].rotateAngleX = 0.4F;
            }
            if (part[0].rotateAngleX < -0.4F)
            {
                part[0].rotateAngleX = -0.4F;
            }
        }
        if (location == BodyPartLocation.ArmRight)
        {
            part[0].rotateAngleX = (float) (part[0].rotateAngleX * 0.5D);
            if (part[0].rotateAngleX > 0.4F)
            {
                part[0].rotateAngleX = 0.4F;
            }
            if (part[0].rotateAngleX < -0.4F)
            {
                part[0].rotateAngleX = -0.4F;
            }
        }
    }

    @Override
    public void setAttributes(EntityLivingBase minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D); // health
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(16.0D); // followrange
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Torso)
        {
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(24.0D); // health
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                      // res
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D); // health
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                        // res
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(3.5D); // damage
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D); // health
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                         // res
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(3.5D); // damage
        }
        else if (location == BodyPartLocation.Legs)
        {
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D); // health
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D); // speed
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
    }
}
