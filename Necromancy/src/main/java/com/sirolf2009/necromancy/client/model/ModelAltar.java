package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelAltar extends ModelBase
{

    public ModelAltar()
    {
        textureWidth = 128;
        textureHeight = 64;
        bottomAltar = new ModelRenderer(this, 0, 45);
        bottomAltar.addBox(0.0F, 0.0F, 0.0F, 16, 3, 16);
        bottomAltar.setRotationPoint(-8F, 21F, -8F);
        bottomAltar.setTextureSize(128, 64);
        bottomAltar.mirror = true;
        setRotation(bottomAltar, 0.0F, 0.0F, 0.0F);
        pillarAltar = new ModelRenderer(this, 0, 41);
        pillarAltar.addBox(0.0F, 0.0F, 0.0F, 11, 17, 6);
        pillarAltar.setRotationPoint(-3F, 4F, -3F);
        pillarAltar.setTextureSize(128, 64);
        pillarAltar.mirror = true;
        setRotation(pillarAltar, 0.0174533F, 0.0F, 0.0F);
        baseAltar = new ModelRenderer(this, 64, 46);
        baseAltar.addBox(0.0F, 0.0F, 0.0F, 16, 2, 16);
        baseAltar.setRotationPoint(-8F, 2.0F, -8F);
        baseAltar.setTextureSize(128, 64);
        baseAltar.mirror = true;
        setRotation(baseAltar, 0.0F, 0.0F, 0.0F);
        backpiece1 = new ModelRenderer(this, 0, 46);
        backpiece1.addBox(0.0F, 0.0F, 0.0F, 1, 2, 16);
        backpiece1.setRotationPoint(7F, 0.0F, -8F);
        backpiece1.setTextureSize(128, 64);
        backpiece1.mirror = true;
        setRotation(backpiece1, 0.0F, 0.0F, 0.0F);
        backpiece2 = new ModelRenderer(this, 0, 54);
        backpiece2.addBox(-8F, 0.0F, 0.0F, 8, 2, 1);
        backpiece2.setRotationPoint(7F, 0.0F, 7.1F);
        backpiece2.setTextureSize(128, 64);
        backpiece2.mirror = true;
        setRotation(backpiece2, 0.0F, 0.0F, -0.2617994F);
        backpiece3 = new ModelRenderer(this, 0, 52);
        backpiece3.addBox(-8F, 0.0F, 0.0F, 8, 2, 1);
        backpiece3.setRotationPoint(7F, 0.0F, -8.1F);
        backpiece3.setTextureSize(128, 64);
        backpiece3.mirror = true;
        setRotation(backpiece3, 0.0F, 0.0F, -0.2617994F);
        connection1 = new ModelRenderer(this, 0, 0);
        connection1.addBox(0.0F, 0.0F, 0.0F, 1, 13, 1);
        connection1.setRotationPoint(7F, 8F, 3F);
        connection1.setTextureSize(128, 64);
        connection1.mirror = true;
        setRotation(connection1, 0.0F, 0.0F, 0.0F);
        tableBottom = new ModelRenderer(this, 0, 0);
        tableBottom.addBox(0.0F, 0.0F, 0.0F, 32, 3, 16);
        tableBottom.setRotationPoint(8F, 21F, -8F);
        tableBottom.setTextureSize(128, 64);
        tableBottom.mirror = true;
        setRotation(tableBottom, 0.0F, 0.0F, 0.0F);
        connection2 = new ModelRenderer(this, 0, 0);
        connection2.addBox(0.0F, 0.0F, 0.0F, 1, 13, 1);
        connection2.setRotationPoint(7F, 8F, -4F);
        connection2.setTextureSize(128, 64);
        connection2.mirror = true;
        setRotation(connection2, 0.0F, 0.0F, 0.0F);
        tableMiddle = new ModelRenderer(this, 0, 0);
        tableMiddle.addBox(0.0F, 0.0F, 0.0F, 29, 11, 10);
        tableMiddle.setRotationPoint(8F, 10F, -5F);
        tableMiddle.setTextureSize(128, 64);
        tableMiddle.mirror = true;
        setRotation(tableMiddle, 0.0F, 0.0F, 0.0F);
        tableTop = new ModelRenderer(this, 0, 0);
        tableTop.addBox(0.0F, 0.0F, 0.0F, 32, 2, 16);
        tableTop.setRotationPoint(8F, 8F, -8F);
        tableTop.setTextureSize(128, 64);
        tableTop.mirror = true;
        setRotation(tableTop, 0.0F, 0.0F, 0.0F);
    }

    public void render()
    {
        float f5 = 0.0625F;
        bottomAltar.render(f5);
        pillarAltar.render(f5);
        baseAltar.render(f5);
        backpiece1.render(f5);
        backpiece2.render(f5);
        backpiece3.render(f5);
        connection1.render(f5);
        tableBottom.render(f5);
        connection2.render(f5);
        tableMiddle.render(f5);
        tableTop.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    ModelRenderer bottomAltar;
    ModelRenderer pillarAltar;
    ModelRenderer baseAltar;
    ModelRenderer backpiece1;
    ModelRenderer backpiece2;
    ModelRenderer backpiece3;
    ModelRenderer connection1;
    ModelRenderer tableBottom;
    ModelRenderer connection2;
    ModelRenderer tableMiddle;
    ModelRenderer tableTop;
}
