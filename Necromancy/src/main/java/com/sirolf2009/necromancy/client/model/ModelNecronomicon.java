package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelNecronomicon extends ModelBook
{

    public ModelNecronomicon()
    {
        int scale = 20;
        textureHeight = 32 * scale;
        textureWidth = 64 * scale;
        coverRight = new ModelRenderer(this).setTextureOffset(0, 0);
        coverRight.addBox(-6.0F * scale, -5.0F * scale, 0.0F, 6 * scale, 10 * scale, 0);
        coverLeft = new ModelRenderer(this).setTextureOffset(16 * scale, 0);
        coverLeft.addBox(0.0F, -5.0F * scale, 0.0F, 6 * scale, 10 * scale, 0);
        pagesRight = new ModelRenderer(this).setTextureOffset(0, 10 * scale);
        pagesRight.addBox(0.0F, -4.0F * scale, -0.99F * scale, 5 * scale, 8 * scale, 1 * scale);
        pagesLeft = new ModelRenderer(this).setTextureOffset(12 * scale, 10 * scale);
        pagesLeft.addBox(0.0F, -4.0F * scale, -0.01F * scale, 5 * scale, 8 * scale, 1 * scale);
        flippingPageRight = new ModelRenderer(this).setTextureOffset(24 * scale, 10 * scale);
        flippingPageRight.addBox(0.0F, -4.0F * scale, 0.0F, 5 * scale, 8 * scale, 0);
        flippingPageLeft = new ModelRenderer(this).setTextureOffset(24 * scale, 10 * scale);
        flippingPageLeft.addBox(0.0F, -4.0F * scale, 0.0F, 5 * scale, 8 * scale, 0);
        bookSpine = new ModelRenderer(this).setTextureOffset(12 * scale, 0);
        bookSpine.addBox(-1.0F * scale, -5.0F * scale, 0.0F, 2 * scale, 10 * scale, 0);
        coverRight.setRotationPoint(0.0F, 0.0F, -1.0F * scale);
        coverLeft.setRotationPoint(0.0F, 0.0F, 1.0F * scale);
        bookSpine.rotateAngleY = (float) Math.PI / 2F;
    }

    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);
        coverRight.render(par7);
        coverLeft.render(par7);
        bookSpine.render(par7);
        pagesRight.render(par7);
        pagesLeft.render(par7);
        // this.flippingPageRight.render(par7);
        // this.flippingPageLeft.render(par7);
    }

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
    {
        float var8 = (MathHelper.sin(par1 * 0.02F) * 0.1F + 1.25F) * par4;
        coverRight.rotateAngleY = (float) Math.PI + var8;
        coverLeft.rotateAngleY = 0;
        pagesRight.rotateAngleY = var8;
        pagesLeft.rotateAngleY = 0;
        flippingPageRight.rotateAngleY = var8 - var8 * 2.0F * par2;
        flippingPageLeft.rotateAngleY = var8 - var8 * 2.0F * par3;
        pagesRight.rotationPointX = MathHelper.sin(var8);
        pagesLeft.rotationPointX = MathHelper.sin(var8);
        flippingPageRight.rotationPointX = MathHelper.sin(var8);
        flippingPageLeft.rotationPointX = MathHelper.sin(var8);
        pagesRight.rotationPointX = 17;
        pagesLeft.rotationPointX = 17;
    }
}
