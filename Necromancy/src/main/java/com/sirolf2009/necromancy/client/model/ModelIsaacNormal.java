package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelIsaacNormal extends ModelBiped
{

    public ModelIsaacNormal()
    {
        textureWidth = 64;
        textureHeight = 32;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 10, 9, 8);
        bipedHead.setRotationPoint(-1F, 1F, 0F);
        bipedHead.setTextureSize(64, 32);
        bipedHead.mirror = true;
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);
        bipedHead.render(par7);
        bipedBody.render(par7);
        bipedRightArm.render(par7);
        bipedLeftArm.render(par7);
        bipedRightLeg.render(par7);
        bipedLeftLeg.render(par7);
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

}
