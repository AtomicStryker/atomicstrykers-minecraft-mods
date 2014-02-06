package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelSewing extends ModelBase
{
    // fields
    ModelRenderer Base;
    ModelRenderer Needle;
    ModelRenderer Arm1;
    ModelRenderer Arm3;
    ModelRenderer Arm2;
    ModelRenderer CrankHandle;
    ModelRenderer CrankArm3;
    ModelRenderer CrankArm4;
    ModelRenderer CrankArm2;
    ModelRenderer CrankArm1;
    ModelRenderer CrankBase;

    public ModelSewing()
    {
        textureWidth = 64;
        textureHeight = 32;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 12, 1, 6);
        Base.setRotationPoint(-7F, 23F, -3F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Needle = new ModelRenderer(this, 0, 0);
        Needle.addBox(0F, 0F, 0F, 1, 4, 1);
        Needle.setRotationPoint(-6.5F, 18.3F, -0.5F);
        Needle.setTextureSize(64, 32);
        Needle.mirror = true;
        setRotation(Needle, 0F, 0F, 0F);
        Arm1 = new ModelRenderer(this, 0, 0);
        Arm1.addBox(0F, 0F, 0F, 2, 6, 2);
        Arm1.setRotationPoint(2F, 17F, -1F);
        Arm1.setTextureSize(64, 32);
        Arm1.mirror = true;
        setRotation(Arm1, 0F, 0F, 0F);
        Arm3 = new ModelRenderer(this, 0, 0);
        Arm3.addBox(0F, 0F, 0F, 2, 2, 2);
        Arm3.setRotationPoint(-7F, 17.1F, -1F);
        Arm3.setTextureSize(64, 32);
        Arm3.mirror = true;
        setRotation(Arm3, 0F, 0F, 0F);
        Arm2 = new ModelRenderer(this, 0, 0);
        Arm2.addBox(0F, 0F, 0F, 7, 1, 1);
        Arm2.setRotationPoint(-5F, 17.3F, -0.5F);
        Arm2.setTextureSize(64, 32);
        Arm2.mirror = true;
        setRotation(Arm2, 0F, 0F, 0F);
        CrankHandle = new ModelRenderer(this, 0, 0);
        CrankHandle.addBox(0F, 0F, 0F, 2, 1, 1);
        CrankHandle.setRotationPoint(5.9F, 14F, -0.5F);
        CrankHandle.setTextureSize(64, 32);
        CrankHandle.mirror = true;
        setRotation(CrankHandle, 0F, 0F, 0F);
        CrankArm3 = new ModelRenderer(this, 0, 0);
        CrankArm3.addBox(0F, 0F, 0F, 3, 1, 1);
        CrankArm3.setRotationPoint(5.9F, 18.3F, 0.5F);
        CrankArm3.setTextureSize(64, 32);
        CrankArm3.mirror = true;
        setRotation(CrankArm3, 0F, 1.570796F, 3.141593F);
        CrankArm4 = new ModelRenderer(this, 0, 0);
        CrankArm4.addBox(0F, 0F, 0F, 3, 1, 1);
        CrankArm4.setRotationPoint(5.9F, 21.3F, 0.5F);
        CrankArm4.setTextureSize(64, 32);
        CrankArm4.mirror = true;
        setRotation(CrankArm4, 0F, 1.570796F, -1.570796F);
        CrankArm2 = new ModelRenderer(this, 0, 0);
        CrankArm2.addBox(0F, 0F, 0F, 3, 1, 1);
        CrankArm2.setRotationPoint(5.9F, 14.3F, -0.5F);
        CrankArm2.setTextureSize(64, 32);
        CrankArm2.mirror = true;
        setRotation(CrankArm2, 0F, 1.570796F, 1.570796F);
        CrankArm1 = new ModelRenderer(this, 0, 0);
        CrankArm1.addBox(0F, 0F, 0F, 3, 1, 1);
        CrankArm1.setRotationPoint(5.9F, 17.3F, -0.5F);
        CrankArm1.setTextureSize(64, 32);
        CrankArm1.mirror = true;
        setRotation(CrankArm1, 0F, 1.570796F, 0F);
        CrankBase = new ModelRenderer(this, 0, 0);
        CrankBase.addBox(0F, 0F, 0F, 3, 1, 1);
        CrankBase.setRotationPoint(4F, 17.3F, -0.5F);
        CrankBase.setTextureSize(64, 32);
        CrankBase.mirror = true;
        setRotation(CrankBase, 0F, 0F, 0F);
    }

    public void render()
    {
        float f5 = 0.0625F;
        Base.render(f5);
        Needle.render(f5);
        Arm1.render(f5);
        Arm3.render(f5);
        Arm2.render(f5);
        /*
         * CrankHandle.render(f5); CrankArm3.render(f5); CrankArm4.render(f5);
         * CrankArm2.render(f5); CrankArm1.render(f5); CrankBase.render(f5);
         */
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
