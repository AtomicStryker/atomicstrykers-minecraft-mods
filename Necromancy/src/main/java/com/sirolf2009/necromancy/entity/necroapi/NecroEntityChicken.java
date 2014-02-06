package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityChicken extends NecroEntityBase
{

    public NecroEntityChicken()
    {
        super("Chicken");
        headItem = ItemBodyPart.getItemStackFromName("Chicken Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Chicken Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Chicken Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Chicken Legs", 1);
        texture = new ResourceLocation("textures/entity/chicken.png");
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.chicken);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart head = new BodyPart(this, model, 0, 0);
        head.addBox(-2.0F, -2.0F, -2.0F, 4, 6, 3, 0.0F);
        BodyPart bill = new BodyPart(this, model, 14, 0);
        bill.addBox(-2.0F, 0.0F, -4.0F, 4, 2, 2, 0.0F);
        BodyPart chin = new BodyPart(this, model, 14, 4);
        chin.addBox(-1.0F, 2.0F, -3.0F, 2, 2, 2, 0.0F);
        return new BodyPart[] { head, bill, chin };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] headPos = { 4.0F, 4.0F, -2.0F };
        float[] armLeftPos = { -3F, 6.0F, 2.0F };
        float[] armRightPos = { 7F, 6.0F, 2.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 0, 9);
        torso.addBox(1.0F, -2.0F, -12.0F, 6, 8, 6, 0.0F);
        return new BodyPart[] { torso };
    }

    @Override
    public BodyPart[] initLegs(ModelBase model)
    {
        float[] torsoPos = { -3F, 8F, 0F };
        BodyPart rightLeg = new BodyPart(this, torsoPos, model, 26, 0);
        rightLeg.addBox(-1.5F, -1.0F, -1.0F, 3, 5, 3);
        rightLeg.setRotationPoint(0.0F, 19.0F, 0.0F);
        BodyPart leftLeg = new BodyPart(this, torsoPos, model, 26, 0);
        leftLeg.addBox(0.5F, -1.0F, -1.0F, 3, 5, 3);
        leftLeg.setRotationPoint(-.0F, 19.0F, 0.0F);
        return new BodyPart[] { leftLeg, rightLeg };
    }

    @Override
    public BodyPart[] initArmLeft(ModelBase model)
    {
        BodyPart leftWing = new BodyPart(this, model, 24, 13);
        leftWing.addBox(3.0F, 0.0F, -3.0F, 1, 4, 6);
        return new BodyPart[] { leftWing };
    }

    @Override
    public BodyPart[] initArmRight(ModelBase model)
    {
        BodyPart rightWing = new BodyPart(this, model, 24, 13);
        rightWing.addBox(0.0F, 0.0F, -3.0F, 1, 4, 6);
        return new BodyPart[] { rightWing };
    }

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity, BodyPart[] bodypart, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            bodypart[0].rotateAngleY = par4 / (180F / (float) Math.PI);
            bodypart[0].rotateAngleX = par5 / (180F / (float) Math.PI);
            bodypart[1].rotateAngleY = par4 / (180F / (float) Math.PI);
            bodypart[1].rotateAngleX = par5 / (180F / (float) Math.PI);
            bodypart[2].rotateAngleY = par4 / (180F / (float) Math.PI);
            bodypart[2].rotateAngleX = par5 / (180F / (float) Math.PI);
        }
        if (location == BodyPartLocation.Legs)
        {
            bodypart[0].rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
            bodypart[1].rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float) Math.PI) * 1.4F * par2;
            bodypart[0].rotateAngleY = 0.0F;
            bodypart[1].rotateAngleY = 0.0F;
        }
        if (location == BodyPartLocation.Torso)
        {
            bodypart[0].rotateAngleX = (float) Math.PI / 2F;
        }
    }

    @Override
    public void setAttributes(EntityLivingBase minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(0.5D); // health
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(16.0D); // followrange
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            head[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Torso)
        {
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(2.0D); // health
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                      // res
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            torso[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(0.5D); // health
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                        // res
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            armLeft[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(0.5D); // health
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                         // res
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D); // speed
            armRight[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
        else if (location == BodyPartLocation.Legs)
        {
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.maxHealth).setBaseValue(0.5D); // health
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.followRange).setBaseValue(0.0D); // followrange
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D); // knockback
                                                                                                                     // res
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D); // speed
            legs[0].attributes.getAttributeInstance(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D); // damage
        }
    }
}
