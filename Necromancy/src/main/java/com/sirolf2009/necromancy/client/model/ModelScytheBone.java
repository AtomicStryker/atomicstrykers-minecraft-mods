package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelScytheBone extends ModelBase
{

    ModelRenderer HandleMiddle;
    ModelRenderer HandleBottom;
    ModelRenderer HandleTop;
    ModelRenderer Joint;
    ModelRenderer Blade;
    ModelRenderer BladeBaseLeft;
    ModelRenderer BladeBaseRight;

    public ModelScytheBone()
    {
        textureWidth = 64;
        textureHeight = 32;

        HandleMiddle = new ModelRenderer(this, 0, 0);
        HandleMiddle.addBox(0F, 0F, 0F, 1, 11, 1);
        HandleMiddle.setRotationPoint(0F, 1.7F, 0F);
        HandleMiddle.setTextureSize(64, 32);
        HandleMiddle.mirror = true;
        setRotation(HandleMiddle, -0.2602503F, 0F, 0F);
        HandleBottom = new ModelRenderer(this, 0, 0);
        HandleBottom.addBox(0F, 0F, 0F, 1, 12, 1);
        HandleBottom.setRotationPoint(0F, 12F, -2.8F);
        HandleBottom.setTextureSize(64, 32);
        HandleBottom.mirror = true;
        setRotation(HandleBottom, 0F, 0F, 0F);
        HandleTop = new ModelRenderer(this, 0, 0);
        HandleTop.addBox(0F, 0F, 0F, 1, 10, 1);
        HandleTop.setRotationPoint(0F, -8F, 0F);
        HandleTop.setTextureSize(64, 32);
        HandleTop.mirror = true;
        setRotation(HandleTop, 0F, 0F, 0F);
        Joint = new ModelRenderer(this, 34, 0);
        Joint.addBox(0F, 0F, 0F, 2, 4, 4);
        Joint.setRotationPoint(-0.5F, -8.1F, -1F);
        Joint.setTextureSize(64, 32);
        Joint.mirror = true;
        setRotation(Joint, 0F, 0F, 0F);
        Blade = new ModelRenderer(this, 0, 15);
        Blade.addBox(-0.5F, -0.5F, 0F, 1, 1, 15);
        Blade.setRotationPoint(0.5F, -7F, 1F);
        Blade.setTextureSize(64, 32);
        Blade.mirror = true;
        setRotation(Blade, -0.1745329F, 0.122173F, 0.7853982F);
        BladeBaseLeft = new ModelRenderer(this, 0, 15);
        BladeBaseLeft.addBox(0F, 0F, 0F, 1, 1, 15);
        BladeBaseLeft.setRotationPoint(0.2F, -8F, 1F);
        BladeBaseLeft.setTextureSize(64, 32);
        BladeBaseLeft.mirror = true;
        setRotation(BladeBaseLeft, -0.1115358F, 0F, 0F);
        BladeBaseRight = new ModelRenderer(this, 0, 15);
        BladeBaseRight.addBox(0F, 0F, 0F, 1, 1, 15);
        BladeBaseRight.setRotationPoint(-0.2F, -8F, 1F);
        BladeBaseRight.setTextureSize(64, 32);
        BladeBaseRight.mirror = true;
        setRotation(BladeBaseRight, -0.1115358F, 0F, 0F);
    }

    public void render()
    {
        float f5 = 0.0625F;
        setRotation(Blade, -.1F, .06F, .7F);
        HandleMiddle.render(f5);
        HandleBottom.render(f5);
        HandleTop.render(f5);
        Joint.render(f5);
        Blade.render(f5);
        BladeBaseLeft.render(f5);
        BladeBaseRight.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
