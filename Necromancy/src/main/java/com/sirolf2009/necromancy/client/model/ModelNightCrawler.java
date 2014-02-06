package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

/**
 * @author Fabiulu Permission to use this model has been lend to sirolf2009 for
 *         the necromancy mod
 */
public class ModelNightCrawler extends ModelBase
{

    ModelRenderer midBody;
    ModelRenderer upperBody;
    ModelRenderer neck;
    ModelRenderer lowerBody;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape11;
    ModelRenderer Shape12;
    ModelRenderer Shape13;
    ModelRenderer Shape14;
    ModelRenderer Shape15;
    ModelRenderer headset;
    ModelRenderer leftarmset;
    ModelRenderer rightarmset;

    public ModelNightCrawler()
    {
        textureWidth = 64;
        textureHeight = 32;

        this.setTextureOffset("headset", 0, 0);
        this.setTextureOffset("headset.head", 17, 0);
        this.setTextureOffset("headset.yaw1", 52, 19);
        this.setTextureOffset("headset.yaw2", 52, 19);
        this.setTextureOffset("headset.yaw3", 52, 19);
        this.setTextureOffset("headset.yaw4", 52, 19);
        this.setTextureOffset("headset.mouth", 18, 13);
        this.setTextureOffset("headset.tooth1", 0, 18);
        this.setTextureOffset("headset.tooth2", 0, 18);
        this.setTextureOffset("headset.tooth3", 0, 18);
        this.setTextureOffset("headset.tooth4", 0, 18);
        this.setTextureOffset("headset.tooth5", 0, 18);
        this.setTextureOffset("headset.tooth6", 0, 18);

        this.setTextureOffset("leftarmset", 0, 0);
        this.setTextureOffset("leftarmset.leftarm", 40, 16);
        this.setTextureOffset("leftarmset.nail1", 0, 0);
        this.setTextureOffset("leftarmset.nail2", 0, 0);

        this.setTextureOffset("rightarmset", 0, 0);
        this.setTextureOffset("rightarmset.rightarm", 40, 16);
        this.setTextureOffset("rightarmset.nail3", 0, 0);
        this.setTextureOffset("rightarmset.nail4", 0, 0);

        midBody = new ModelRenderer(this, 49, 9);
        midBody.addBox(-4F, 0F, -2F, 4, 5, 3);
        midBody.setRotationPoint(2F, 14.46667F, -1.133333F);
        midBody.setTextureSize(64, 32);
        midBody.mirror = true;
        setRotation(midBody, 0.1858931F, 0F, 0F);
        upperBody = new ModelRenderer(this, 45, 18);
        upperBody.addBox(0F, 0F, 0F, 6, 4, 3);
        upperBody.setRotationPoint(-3F, 12.46667F, -4.666667F);
        upperBody.setTextureSize(64, 32);
        upperBody.mirror = true;
        setRotation(upperBody, 0.5379539F, 0F, 0F);
        neck = new ModelRenderer(this, 57, 14);
        neck.addBox(0F, 0F, 0F, 2, 1, 1);
        neck.setRotationPoint(-1F, 11.33333F, -4F);
        neck.setTextureSize(64, 32);
        neck.mirror = true;
        setRotation(neck, 0.669215F, 0F, 0F);
        lowerBody = new ModelRenderer(this, 53, 23);
        lowerBody.addBox(0F, 0F, 0F, 2, 5, 3);
        lowerBody.setRotationPoint(-1F, 19F, -2.4F);
        lowerBody.setTextureSize(64, 32);
        lowerBody.mirror = true;
        setRotation(lowerBody, 0F, 0F, 0F);
        Shape1 = new ModelRenderer(this, 54, 27);
        Shape1.addBox(0F, 0F, 0F, 1, 0, 3);
        Shape1.setRotationPoint(0F, 23F, 0F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, -0.2602503F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 54, 28);
        Shape2.addBox(0F, 0F, 0F, 1, 0, 2);
        Shape2.setRotationPoint(-1F, 23F, 0F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, -0.4833219F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 54, 30);
        Shape7.addBox(0F, 0F, 0F, 1, 0, 1);
        Shape7.setRotationPoint(0F, 22F, 0.2666667F);
        Shape7.setTextureSize(64, 32);
        Shape7.mirror = true;
        setRotation(Shape7, -0.9294653F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 54, 27);
        Shape8.addBox(0F, 0F, 0F, 1, 0, 3);
        Shape8.setRotationPoint(1F, 23F, -1.6F);
        Shape8.setTextureSize(64, 32);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0.2230717F, 0.8179294F);
        Shape9 = new ModelRenderer(this, 54, 28);
        Shape9.addBox(0F, 0F, 0F, 1, 0, 2);
        Shape9.setRotationPoint(-1F, 23F, -1.866667F);
        Shape9.setTextureSize(64, 32);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, -0.2230717F, 2.249306F);
        Shape10 = new ModelRenderer(this, 55, 27);
        Shape10.addBox(0F, 0F, 0F, 1, 1, 0);
        Shape10.setRotationPoint(-1F, 21F, -0.5333334F);
        Shape10.setTextureSize(64, 32);
        Shape10.mirror = true;
        setRotation(Shape10, 0F, -2.286485F, 0F);
        Shape11 = new ModelRenderer(this, 54, 28);
        Shape11.addBox(0F, 0F, 0F, 1, 1, 0);
        Shape11.setRotationPoint(1F, 20F, -2F);
        Shape11.setTextureSize(64, 32);
        Shape11.mirror = true;
        setRotation(Shape11, 0F, -0.9294653F, 0F);
        Shape12 = new ModelRenderer(this, 58, 27);
        Shape12.addBox(0F, 0F, 0F, 0, 1, 2);
        Shape12.setRotationPoint(-1F, 23F, -1F);
        Shape12.setTextureSize(64, 32);
        Shape12.mirror = true;
        setRotation(Shape12, 0F, -0.1115358F, 0.1858931F);
        Shape13 = new ModelRenderer(this, 58, 27);
        Shape13.addBox(0F, 0F, 0F, 0, 1, 2);
        Shape13.setRotationPoint(1F, 23F, 0F);
        Shape13.setTextureSize(64, 32);
        Shape13.mirror = true;
        setRotation(Shape13, 0F, 0.1858931F, -0.3717861F);
        Shape14 = new ModelRenderer(this, 56, 28);
        Shape14.addBox(0F, 0F, 0F, 0, 1, 1);
        Shape14.setRotationPoint(1F, 23F, -2F);
        Shape14.setTextureSize(64, 32);
        Shape14.mirror = true;
        setRotation(Shape14, 0F, 0.9294653F, 0F);
        Shape15 = new ModelRenderer(this, 59, 28);
        Shape15.addBox(0F, 0F, 0F, 0, 1, 1);
        Shape15.setRotationPoint(-1F, 23F, -2F);
        Shape15.setTextureSize(64, 32);
        Shape15.mirror = true;
        setRotation(Shape15, 0F, -0.8365188F, 0F);
        headset = new ModelRenderer(this, "headset");
        headset.setRotationPoint(0F, 12.26667F, -4.066667F);
        setRotation(headset, 0F, 0F, 0F);
        headset.mirror = true;
        headset.addBox("head", -2F, -9F, -4F, 4, 5, 5);
        headset.addBox("yaw2", 1F, -4F, 1F, 1, 2, 0);
        headset.addBox("yaw3", -2F, -4F, 1F, 1, 2, 0);
        headset.addBox("yaw1", 2F, -4F, 0F, 0, 2, 1);
        headset.addBox("yaw4", -2F, -4F, 0F, 0, 2, 1);
        headset.addBox("mouth", -2F, -2F, -3F, 4, 1, 4);
        headset.addBox("tooth4", -1.933333F, -2.466667F, -2F, 0, 1, 1);
        headset.addBox("tooth1", 1.866667F, -2.466667F, -2F, 0, 1, 1);
        headset.addBox("tooth2", 0.4666667F, -2.466667F, -2.933333F, 1, 1, 0);
        headset.addBox("tooth3", -1.466667F, -2.466667F, -2.933333F, 1, 1, 0);
        headset.addBox("tooth5", 0.7333333F, -4.8F, -3.533333F, 1, 1, 0);
        headset.addBox("tooth6", -1.666667F, -4.8F, -3.466667F, 1, 1, 0);
        leftarmset = new ModelRenderer(this, "leftarmset");
        leftarmset.setRotationPoint(0F, 2F, 1F);
        setRotation(leftarmset, 0F, 0F, 0F);
        leftarmset.mirror = true;
        leftarmset.addBox("leftarm", 3F, 10F, -4F, 1, 9, 1);
        leftarmset.addBox("nail1", 4F, 19F, -4F, 0, 2, 1);
        leftarmset.addBox("nail2", 3F, 19F, -4F, 0, 2, 1);
        rightarmset = new ModelRenderer(this, "rightarmset");
        rightarmset.setRotationPoint(0F, 2F, 0F);
        setRotation(rightarmset, 0F, 0F, 0F);
        rightarmset.mirror = true;
        rightarmset.addBox("rightarm", -4F, 10F, -3F, 1, 9, 1);
        rightarmset.addBox("nail3", -3F, 19F, -3F, 0, 2, 1);
        rightarmset.addBox("nail4", -4F, 19F, -3F, 0, 2, 1);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        GL11.glPushMatrix();
        GL11.glScalef(1.4F, 1.4F, 1.4F);
        GL11.glTranslatef(0, -.4F, 0);
        midBody.render(f5);
        upperBody.render(f5);
        neck.render(f5);
        lowerBody.render(f5);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape7.render(f5);
        Shape8.render(f5);
        Shape9.render(f5);
        Shape10.render(f5);
        Shape11.render(f5);
        Shape12.render(f5);
        Shape13.render(f5);
        Shape14.render(f5);
        Shape15.render(f5);
        headset.render(f5);
        leftarmset.render(f5);
        rightarmset.render(f5);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

}
