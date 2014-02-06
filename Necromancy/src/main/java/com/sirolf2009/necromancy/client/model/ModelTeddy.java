package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelTeddy extends ModelBase
{

    public ModelTeddy()
    {
        textureWidth = 64;
        textureHeight = 32;
        pawFrontRight = new ModelRenderer(this, 0, 5);
        pawFrontRight.addBox(-2F, 0.0F, 0.0F, 2, 3, 2);
        pawFrontRight.setRotationPoint(-1.5F, 16F, 2.5F);
        pawFrontRight.setTextureSize(64, 32);
        pawFrontRight.mirror = true;
        setRotation(pawFrontRight, -1.151917F, 0.0F, 0.0F);
        pawFrontLeft = new ModelRenderer(this, 4, 5);
        pawFrontLeft.addBox(0.0F, 0.0F, 0.0F, 2, 3, 2);
        pawFrontLeft.setRotationPoint(1.5F, 16F, 2.5F);
        pawFrontLeft.setTextureSize(64, 32);
        pawFrontLeft.mirror = true;
        setRotation(pawFrontLeft, -1.151917F, 0.0F, 0.0F);
        pawBackRight = new ModelRenderer(this, 0, 14);
        pawBackRight.addBox(0.0F, 0.0F, 0.0F, 2, 3, 2);
        pawBackRight.setRotationPoint(-2.2F, 21F, 2.0F);
        pawBackRight.setTextureSize(64, 32);
        pawBackRight.mirror = true;
        setRotation(pawBackRight, 0.0F, 0.0F, 0.0F);
        pawBackLeft = new ModelRenderer(this, 0, 9);
        pawBackLeft.addBox(0.0F, 0.0F, 0.0F, 2, 3, 2);
        pawBackLeft.setRotationPoint(0.2F, 21F, 2.0F);
        pawBackLeft.setTextureSize(64, 32);
        pawBackLeft.mirror = true;
        setRotation(pawBackLeft, 0.0F, 0.0F, 0.0F);
        Belly = new ModelRenderer(this, 10, 0);
        Belly.addBox(0.0F, 0.0F, 0.0F, 4, 7, 3);
        Belly.setRotationPoint(-2F, 15F, 1.0F);
        Belly.setTextureSize(64, 32);
        Belly.mirror = true;
        setRotation(Belly, 0.1487144F, 0.0F, 0.0F);
        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-2F, -3F, -1F, 3, 3, 2);
        Head.setRotationPoint(0.5F, 16F, 1.0F);
        Head.setTextureSize(64, 32);
        Head.mirror = true;
        setRotation(Head, 0.0F, 0.0F, 0.0F);
        earRight = new ModelRenderer(this, 8, 10);
        earRight.addBox(-0.5F, -1F, 0.0F, 1, 1, 1);
        earRight.setRotationPoint(-1F, 13.2F, 0.1F);
        earRight.setTextureSize(64, 32);
        earRight.mirror = true;
        setRotation(earRight, 0.1115358F, 0.0F, -0.2230717F);
        earLeft = new ModelRenderer(this, 8, 12);
        earLeft.addBox(-0.5F, -1F, 0.0F, 1, 1, 1);
        earLeft.setRotationPoint(1.0F, 13.2F, 0.1F);
        earLeft.setTextureSize(64, 32);
        earLeft.mirror = true;
        setRotation(earLeft, 0.1115358F, 0.0F, 0.2230705F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(entity, f, f1, f2, f3, f4, f5);
        pawFrontRight.render(f5);
        pawFrontLeft.render(f5);
        pawBackRight.render(f5);
        pawBackLeft.render(f5);
        Belly.render(f5);
        Head.render(f5);
        earRight.render(f5);
        earLeft.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void setRotationAngles(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        float var7 = MathHelper.sin(onGround * 3.141593F);
        float var8 = MathHelper.sin((1.0F - (1.0F - onGround) * (1.0F - onGround)) * 3.141593F);
        pawFrontRight.rotateAngleZ = 0.0F;
        pawFrontLeft.rotateAngleZ = 0.0F;
        pawFrontRight.rotateAngleY = -(0.1F - var7 * 0.6F);
        pawFrontLeft.rotateAngleY = 0.1F - var7 * 0.6F;
        pawFrontRight.rotateAngleX = -1.570796F;
        pawFrontLeft.rotateAngleX = -1.570796F;
        pawFrontRight.rotateAngleX -= var7 * 1.2F - var8 * 0.4F;
        pawFrontLeft.rotateAngleX -= var7 * 1.2F - var8 * 0.4F;
        pawFrontRight.rotateAngleZ += MathHelper.cos(f3 * 0.09F) * 0.05F + 0.05F;
        pawFrontLeft.rotateAngleZ -= MathHelper.cos(f3 * 0.09F) * 0.05F + 0.05F;
        pawFrontRight.rotateAngleX += MathHelper.sin(f3 * 0.067F) * 0.05F;
        pawFrontLeft.rotateAngleX -= MathHelper.sin(f3 * 0.067F) * 0.05F;
        pawBackRight.rotateAngleX = (float) (MathHelper.cos(f) * 0.5D);
        pawBackLeft.rotateAngleX = (float) (MathHelper.cos(f + 3.141593F) * 0.5D);
        pawBackRight.rotateAngleY = 0.0F;
        pawBackLeft.rotateAngleY = 0.0F;
    }

    ModelRenderer pawFrontRight;
    ModelRenderer pawFrontLeft;
    ModelRenderer pawBackRight;
    ModelRenderer pawBackLeft;
    ModelRenderer Belly;
    ModelRenderer Head;
    ModelRenderer earRight;
    ModelRenderer earLeft;
}
