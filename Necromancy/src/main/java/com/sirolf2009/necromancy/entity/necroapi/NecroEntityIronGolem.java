package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necromancy.item.ItemBodyPart;

public class NecroEntityIronGolem extends NecroEntityBase
{

    public NecroEntityIronGolem()
    {
        super("IronGolem");
        headItem = ItemBodyPart.getItemStackFromName("IronGolem Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("IronGolem Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("IronGolem Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("IronGolem Legs", 1);
        texture = new ResourceLocation("textures/entity/iron_golem.png");
        textureHeight = 128;
        textureWidth = 128;
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Blocks.pumpkin, Blocks.iron_block, Blocks.iron_block, Blocks.iron_block);
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart ironGolemHead = new BodyPart(this, model, 0, 0);
        ironGolemHead.setRotationPoint(0.0F, 0.0F, -2.0F);
        ironGolemHead.setTextureOffset(0, 0).addBox(-4.0F, -6.0F, -5.5F, 8, 10, 8, 0F);
        ironGolemHead.setTextureOffset(24, 0).addBox(-1.0F, 1.0F, -7.5F, 2, 4, 2, 0F);
        return new BodyPart[] { ironGolemHead };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] headPos = { 8.0F, -7.0F, 2.0F };
        float[] armLeftPos = { -9F, 0.0F, 0.0F };
        float[] armRightPos = { 13F, 0.0F, 0.0F };
        BodyPart ironGolemBody = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 0, 0);
        ironGolemBody.setRotationPoint(0.0F, -7.0F, 0.0F);
        ironGolemBody.setTextureOffset(0, 40).addBox(-5.0F, 4.0F, -6.0F, 18, 12, 11, 0F);
        ironGolemBody.setTextureOffset(0, 70).addBox(-0.5F, 16.0F, -3.0F, 9, 5, 6, 0.5F);
        return new BodyPart[] { ironGolemBody };
    }

    @Override
    public BodyPart[] initLegs(ModelBase model)
    {
        float[] torsoPos = { -4F, -4F, 0F };
        BodyPart ironGolemLeftLeg = new BodyPart(this, torsoPos, model, 0, 22);
        ironGolemLeftLeg.setRotationPoint(-4.0F, 11.0F, 0.0F);
        ironGolemLeftLeg.setTextureOffset(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, 0F);
        BodyPart ironGolemRightLeg = new BodyPart(this, torsoPos, model, 0, 22);
        ironGolemRightLeg.mirror = true;
        ironGolemRightLeg.setTextureOffset(60, 0).setRotationPoint(5.0F, 11.0F, 0.0F);
        ;
        ironGolemRightLeg.addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, 0F);
        return new BodyPart[] { ironGolemLeftLeg, ironGolemRightLeg };
    }

    @Override
    public BodyPart[] initArmLeft(ModelBase model)
    {
        BodyPart ironGolemLeftArm = new BodyPart(this, model, 0, 0);
        ironGolemLeftArm.mirror = true;
        ironGolemLeftArm.setRotationPoint(0.0F, -7.0F, 0.0F);
        ironGolemLeftArm.setTextureOffset(60, 58).addBox(0F, 2F, -3.0F, 4, 30, 6, 0F);
        return new BodyPart[] { ironGolemLeftArm };
    }

    @Override
    public BodyPart[] initArmRight(ModelBase model)
    {
        BodyPart ironGolemRightArm = new BodyPart(this, model, 0, 0);
        ironGolemRightArm.setRotationPoint(0.0F, -7.0F, 0.0F);
        ironGolemRightArm.setTextureOffset(60, 21).addBox(0F, 2F, -3.0F, 4, 30, 6, 0F);
        return new BodyPart[] { ironGolemRightArm };
    }

    @Override
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 4.0D, 16.0D, 0D, 0D, 0.0D);
        }
        else if (location == BodyPartLocation.Torso)
        {
            addAttributeMods(minion, "Torso", 35.0D, 0D, 0D, 0D, 0D);
        }
        else if (location == BodyPartLocation.ArmLeft)
        {
            addAttributeMods(minion, "ArmL", 4.0D, 0D, 0D, 0D, 4.5D);
        }
        else if (location == BodyPartLocation.ArmRight)
        {
            addAttributeMods(minion, "ArmR", 4.0D, 0D, 0D, 0D, 4.5D);
        }
        else if (location == BodyPartLocation.Legs)
        {
            addAttributeMods(minion, "Legs", 4.0D, 0D, 0D, 0.3D, 0D);
        }
    }
}
