package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelIsaacSevered extends ModelIsaacNormal
{
    // fields
    ModelRenderer neck;

    public ModelIsaacSevered()
    {
        super();
        neck = new ModelRenderer(this, 0, 0);
        neck.addBox(0F, 0F, 0F, 2, 1, 2);
        neck.setRotationPoint(-1F, 1F, -1F);
        neck.setTextureSize(64, 32);
        neck.mirror = true;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        // super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        neck.render(f5);
        bipedBody.render(f5);
        bipedRightArm.render(f5);
        bipedLeftArm.render(f5);
        bipedRightLeg.render(f5);
        bipedLeftLeg.render(f5);
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

}
