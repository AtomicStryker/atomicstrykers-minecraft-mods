package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelScythe extends ModelBase
{
    // fields
    ModelRenderer HandleMiddle;
    ModelRenderer BladeEdge;
    ModelRenderer BladeBase1;
    ModelRenderer HandleBottom;
    ModelRenderer HandleTop;
    ModelRenderer Joint;
    ModelRenderer BladeBase2;

    public ModelScythe()
    {
        textureWidth = 64;
        textureHeight = 32;

        HandleMiddle = new ModelRenderer(this, 0, 0);
        HandleMiddle.addBox(0F, 0F, 0F, 1, 11, 1);
        HandleMiddle.setRotationPoint(0F, 1.7F, 0F);
        HandleMiddle.setTextureSize(64, 32);
        HandleMiddle.mirror = true;
        setRotation(HandleMiddle, -0.2602503F, 0F, 0F);
        BladeEdge = new ModelRenderer(this, 4, 0);
        BladeEdge.addBox(-0.5F, -0.5F, 0F, 1, 1, 10);
        BladeEdge.setRotationPoint(0.5F, -7F, 2F);
        BladeEdge.setTextureSize(64, 32);
        BladeEdge.mirror = true;
        setRotation(BladeEdge, 0F, 0F, 0.7853982F);
        BladeBase1 = new ModelRenderer(this, 40, 0);
        BladeBase1.addBox(0F, 0F, 0F, 1, 1, 11);
        BladeBase1.setRotationPoint(0.2F, -8F, 1F);
        BladeBase1.setTextureSize(64, 32);
        BladeBase1.mirror = true;
        setRotation(BladeBase1, 0F, 0F, 0F);
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
        Joint = new ModelRenderer(this, 0, 13);
        Joint.addBox(0F, 0F, 0F, 2, 2, 2);
        Joint.setRotationPoint(-0.5F, -8.1F, 0F);
        Joint.setTextureSize(64, 32);
        Joint.mirror = true;
        setRotation(Joint, 0F, 0F, 0F);
        BladeBase2 = new ModelRenderer(this, 40, 0);
        BladeBase2.addBox(0F, 0F, 0F, 1, 1, 11);
        BladeBase2.setRotationPoint(-0.2F, -8F, 1F);
        BladeBase2.setTextureSize(64, 32);
        BladeBase2.mirror = true;
        setRotation(BladeBase2, 0F, 0F, 0F);
    }

    public void render()
    {
        float f5 = 0.0625F;
        HandleMiddle.render(f5);
        BladeEdge.render(f5);
        BladeBase1.render(f5);
        HandleBottom.render(f5);
        HandleTop.render(f5);
        Joint.render(f5);
        BladeBase2.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
