package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
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
        float[] torsoPos = { -4F, 6F, 3F };
        BodyPart leg1 = new BodyPart(this, torsoPos, model, 18, 0);
        leg1.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg1.setRotationPoint(-4.0F, 15.0F, 2.0F);
        BodyPart leg2 = new BodyPart(this, torsoPos, model, 18, 0);
        leg2.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg2.setRotationPoint(4.0F, 15.0F, 2.0F);
        BodyPart leg3 = new BodyPart(this, torsoPos, model, 18, 0);
        leg3.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg3.setRotationPoint(-4.0F, 15.0F, 1.0F);
        BodyPart leg4 = new BodyPart(this, torsoPos, model, 18, 0);
        leg4.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg4.setRotationPoint(4.0F, 15.0F, 1.0F);
        BodyPart leg5 = new BodyPart(this, torsoPos, model, 18, 0);
        leg5.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg5.setRotationPoint(-4.0F, 15.0F, 0.0F);
        BodyPart leg6 = new BodyPart(this, torsoPos, model, 18, 0);
        leg6.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg6.setRotationPoint(4.0F, 15.0F, 0.0F);
        BodyPart leg7 = new BodyPart(this, torsoPos, model, 18, 0);
        leg7.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg7.setRotationPoint(-4.0F, 15.0F, -1.0F);
        BodyPart leg8 = new BodyPart(this, torsoPos, model, 18, 0);
        leg8.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
        leg8.setRotationPoint(4.0F, 15.0F, -1.0F);
        return new BodyPart[] { leg1, leg2, leg3, leg4, leg5, leg6, leg7, leg8 };
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
    public void setAttributes(EntityLiving minion, BodyPartLocation location)
    {
        if (location == BodyPartLocation.Head)
        {
            addAttributeMods(minion, "Head", 2.0D, 16.0D, 0D, 0D, 0.0D);
        }
        else if (location == BodyPartLocation.Torso)
        {
            addAttributeMods(minion, "Torso", 6.0D, 0D, 0D, 0D, 0D);
        }
        else if (location == BodyPartLocation.Legs)
        {
            addAttributeMods(minion, "Legs", 2.0D, 0D, 0D, 0.7D, 3.0D);
        }
    }
}
