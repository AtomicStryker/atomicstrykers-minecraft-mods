package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.ISaddleAble;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

public class NecroEntitySpider extends NecroEntityBase implements ISaddleAble
{

    public NecroEntitySpider(String name)
    {
        super(name);
        headItem = ItemBodyPart.getItemStackFromName("Spider Head", 1);
        torsoItem = ItemBodyPart.getItemStackFromName("Spider Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Spider Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Spider Legs", 1);
        texture = new ResourceLocation("textures/entity/spider/spider.png");
        hasArms = false;
    }

    @Override
    public void initRecipes()
    {
        initDefaultRecipes(Items.spider_eye);
    }

    public NecroEntitySpider()
    {
        this("Spider");
    }

    @Override
    public BodyPart[] initHead(ModelBase model)
    {
        BodyPart spiderHead = new BodyPart(this, model, 32, 4);
        spiderHead.addBox(-4.0F, -4.0F, -6.0F, 8, 8, 8, 0.0F);
        return new BodyPart[] { spiderHead };
    }

    @Override
    public BodyPart[] initTorso(ModelBase model)
    {
        float[] headPos = { 4.0F, 8, -7.0F };
        float[] armLeftPos = { -1.0F, 10.0F, -6.0F };
        float[] armRightPos = { 5F, 10.0F, -6.0F };
        BodyPart spiderNeck = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 0, 0);
        spiderNeck.addBox(1.0F, 5.0F, -6.0F, 6, 6, 6, 0.0F);
        BodyPart spiderBody = new BodyPart(this, armLeftPos, armRightPos, headPos, model, 0, 12);
        spiderBody.addBox(-1.0F, 4.0F, 0.0F, 10, 8, 12, 0.0F);
        return new BodyPart[] { spiderBody, spiderNeck };
    }

    @Override
    public BodyPart[] initLegs(ModelBase model)
    {
        float[] torsoPos = { -4F, 6F, 3F };
        BodyPart spiderLeg1 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg1.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg1.setRotationPoint(-4.0F, 15.0F, 2.0F);
        BodyPart spiderLeg2 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg2.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg2.setRotationPoint(4.0F, 15.0F, 2.0F);
        BodyPart spiderLeg3 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg3.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg3.setRotationPoint(-4.0F, 15.0F, 1.0F);
        BodyPart spiderLeg4 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg4.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg4.setRotationPoint(4.0F, 15.0F, 1.0F);
        BodyPart spiderLeg5 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg5.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg5.setRotationPoint(-4.0F, 15.0F, 0.0F);
        BodyPart spiderLeg6 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg6.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg6.setRotationPoint(4.0F, 15.0F, 0.0F);
        BodyPart spiderLeg7 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg7.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg7.setRotationPoint(-4.0F, 15.0F, -1.0F);
        BodyPart spiderLeg8 = new BodyPart(this, torsoPos, model, 18, 0);
        spiderLeg8.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        spiderLeg8.setRotationPoint(4.0F, 15.0F, -1.0F);
        return new BodyPart[] { spiderLeg1, spiderLeg2, spiderLeg3, spiderLeg4, spiderLeg5, spiderLeg6, spiderLeg7, spiderLeg8 };
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
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, BodyPart[] part, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Legs)
        {
            float var8 = (float) Math.PI / 4F;
            part[0].rotateAngleZ = -var8;
            part[1].rotateAngleZ = var8;
            part[2].rotateAngleZ = -var8 * 0.74F;
            part[3].rotateAngleZ = var8 * 0.74F;
            part[4].rotateAngleZ = -var8 * 0.74F;
            part[5].rotateAngleZ = var8 * 0.74F;
            part[6].rotateAngleZ = -var8;
            part[7].rotateAngleZ = var8;
            float var9 = -0.0F;
            float var10 = 0.3926991F;
            part[0].rotateAngleY = var10 * 2.0F + var9;
            part[1].rotateAngleY = -var10 * 2.0F - var9;
            part[2].rotateAngleY = var10 * 1.0F + var9;
            part[3].rotateAngleY = -var10 * 1.0F - var9;
            part[4].rotateAngleY = -var10 * 1.0F + var9;
            part[5].rotateAngleY = var10 * 1.0F - var9;
            part[6].rotateAngleY = -var10 * 2.0F + var9;
            part[7].rotateAngleY = var10 * 2.0F - var9;
            float var11 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + 0.0F) * 0.4F) * par2;
            float var12 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * par2;
            float var13 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + (float) Math.PI / 2F) * 0.4F) * par2;
            float var14 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + (float) Math.PI * 3F / 2F) * 0.4F) * par2;
            float var15 = Math.abs(MathHelper.sin(par1 * 0.6662F + 0.0F) * 0.4F) * par2;
            float var16 = Math.abs(MathHelper.sin(par1 * 0.6662F + (float) Math.PI) * 0.4F) * par2;
            float var17 = Math.abs(MathHelper.sin(par1 * 0.6662F + (float) Math.PI / 2F) * 0.4F) * par2;
            float var18 = Math.abs(MathHelper.sin(par1 * 0.6662F + (float) Math.PI * 3F / 2F) * 0.4F) * par2;
            part[0].rotateAngleY += var11;
            part[1].rotateAngleY += -var11;
            part[2].rotateAngleY += var12;
            part[3].rotateAngleY += -var12;
            part[4].rotateAngleY += var13;
            part[5].rotateAngleY += -var13;
            part[6].rotateAngleY += var14;
            part[7].rotateAngleY += -var14;
            part[0].rotateAngleZ += var15;
            part[1].rotateAngleZ += -var15;
            part[2].rotateAngleZ += var16;
            part[3].rotateAngleZ += -var16;
            part[4].rotateAngleZ += var17;
            part[5].rotateAngleZ += -var17;
            part[6].rotateAngleZ += var18;
            part[7].rotateAngleZ += -var18;
        }
    }

    @Override
    public ResourceLocation getSaddleTex()
    {
        return new ResourceLocation(ReferenceNecromancy.LOC_RESOURCES_TEXTURES_ENTITIES + "/spidersaddle.png");
    }

    @Override
    public int riderHeight()
    {
        return 0;
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
