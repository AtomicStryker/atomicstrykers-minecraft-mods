package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

import org.lwjgl.opengl.GL11;

public class ModelScentBurner extends ModelBase
{

    private final ModelRenderer ropeFrontLeft;
    private final ModelRenderer base;
    private final ModelRenderer burner;
    private final ModelRenderer legFrontRight;
    private final ModelRenderer armFrontRight;
    private final ModelRenderer armFrontLeft;
    private final ModelRenderer armBackLeft;
    private final ModelRenderer legFrontLeft;
    private final ModelRenderer legBackRight;
    private final ModelRenderer legBackLeft;
    private final ModelRenderer armBackRight;
    private final ModelRenderer ropeFrontRight;
    private final ModelRenderer ropeBackLeft;
    private final ModelRenderer ropeBackRight;

    public ModelScentBurner()
    {
        textureWidth = 64;
        textureHeight = 32;
        ropeFrontLeft = new ModelRenderer(this, 32, 0);
        ropeFrontLeft.addBox(-0.5F, 0F, -0.5F, 1, 4, 1);
        ropeFrontLeft.setRotationPoint(-3.5F, 14.5F, -3.5F);
        ropeFrontLeft.setTextureSize(64, 32);
        ropeFrontLeft.mirror = true;
        setRotation(ropeFrontLeft, 0F, 2.356194F, 1.134464F);
        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 8, 1, 8);
        base.setRotationPoint(-4F, 22F, -4F);
        base.setTextureSize(64, 32);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        burner = new ModelRenderer(this, 24, 9);
        burner.addBox(0F, 0F, 0F, 2, 2, 2);
        burner.setRotationPoint(-1F, 16F, -1F);
        burner.setTextureSize(64, 32);
        burner.mirror = true;
        setRotation(burner, 0F, 0F, 0F);
        legFrontRight = new ModelRenderer(this, 0, 12);
        legFrontRight.addBox(0F, 0F, 0F, 2, 1, 2);
        legFrontRight.setRotationPoint(3F, 23F, -5F);
        legFrontRight.setTextureSize(64, 32);
        legFrontRight.mirror = true;
        setRotation(legFrontRight, 0F, 0F, 0F);
        armFrontRight = new ModelRenderer(this, 12, 9);
        armFrontRight.addBox(0F, 0F, 0F, 1, 8, 1);
        armFrontRight.setRotationPoint(3F, 14F, -4F);
        armFrontRight.setTextureSize(64, 32);
        armFrontRight.mirror = true;
        setRotation(armFrontRight, 0F, 0F, 0F);
        armFrontLeft = new ModelRenderer(this, 8, 9);
        armFrontLeft.addBox(0F, 0F, 0F, 1, 8, 1);
        armFrontLeft.setRotationPoint(-4F, 14F, -4F);
        armFrontLeft.setTextureSize(64, 32);
        armFrontLeft.mirror = true;
        setRotation(armFrontLeft, 0F, 0F, 0F);
        armBackLeft = new ModelRenderer(this, 16, 9);
        armBackLeft.addBox(0F, 0F, 0F, 1, 8, 1);
        armBackLeft.setRotationPoint(-4F, 14F, 3F);
        armBackLeft.setTextureSize(64, 32);
        armBackLeft.mirror = true;
        setRotation(armBackLeft, 0F, 0F, 0F);
        legFrontLeft = new ModelRenderer(this, 0, 9);
        legFrontLeft.addBox(0F, 0F, 0F, 2, 1, 2);
        legFrontLeft.setRotationPoint(-5F, 23F, -5F);
        legFrontLeft.setTextureSize(64, 32);
        legFrontLeft.mirror = true;
        setRotation(legFrontLeft, 0F, 0F, 0F);
        legBackRight = new ModelRenderer(this, 0, 18);
        legBackRight.addBox(0F, 0F, 0F, 2, 1, 2);
        legBackRight.setRotationPoint(3F, 23F, 3F);
        legBackRight.setTextureSize(64, 32);
        legBackRight.mirror = true;
        setRotation(legBackRight, 0F, 0F, 0F);
        legBackLeft = new ModelRenderer(this, 0, 15);
        legBackLeft.addBox(0F, 0F, 0F, 2, 1, 2);
        legBackLeft.setRotationPoint(-5F, 23F, 3F);
        legBackLeft.setTextureSize(64, 32);
        legBackLeft.mirror = true;
        setRotation(legBackLeft, 0F, 0F, 0F);
        armBackRight = new ModelRenderer(this, 20, 9);
        armBackRight.addBox(0F, 0F, 0F, 1, 8, 1);
        armBackRight.setRotationPoint(3F, 14F, 3F);
        armBackRight.setTextureSize(64, 32);
        armBackRight.mirror = true;
        setRotation(armBackRight, 0F, 0F, 0F);
        ropeFrontRight = new ModelRenderer(this, 36, 0);
        ropeFrontRight.addBox(-0.5F, 0F, -0.5F, 1, 4, 1);
        ropeFrontRight.setRotationPoint(3.5F, 14.5F, -3.5F);
        ropeFrontRight.setTextureSize(64, 32);
        ropeFrontRight.mirror = true;
        setRotation(ropeFrontRight, 0F, 0.7853982F, 1.134464F);
        ropeBackLeft = new ModelRenderer(this, 40, 0);
        ropeBackLeft.addBox(-0.5F, 0F, -0.5F, 1, 4, 1);
        ropeBackLeft.setRotationPoint(-3.5F, 14.5F, 3.5F);
        ropeBackLeft.setTextureSize(64, 32);
        ropeBackLeft.mirror = true;
        setRotation(ropeBackLeft, 0F, -2.356194F, 1.134464F);
        ropeBackRight = new ModelRenderer(this, 44, 0);
        ropeBackRight.addBox(-0.5F, 0F, -0.5F, 1, 4, 1);
        ropeBackRight.setRotationPoint(3.5F, 14.5F, 3.5F);
        ropeBackRight.setTextureSize(64, 32);
        ropeBackRight.mirror = true;
        setRotation(ropeBackRight, 0F, -0.7853982F, 1.134464F);
    }
    
    /**
     * Called by TileEntityScentBurnerRenderer
     */
    public void render()
    {
        float f5 = 0.0625F;
        base.render(f5);
        burner.render(f5);
        legFrontRight.render(f5);
        armFrontRight.render(f5);
        armFrontLeft.render(f5);
        armBackLeft.render(f5);
        legFrontLeft.render(f5);
        legBackRight.render(f5);
        legBackLeft.render(f5);
        armBackRight.render(f5);
        GL11.glPushMatrix();
        ropeFrontRight.render(f5);
        setRotation(ropeFrontRight, 1F, 0.0F, 2F);
        ropeBackLeft.render(f5);
        ropeBackRight.render(f5);
        ropeFrontLeft.render(f5);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
