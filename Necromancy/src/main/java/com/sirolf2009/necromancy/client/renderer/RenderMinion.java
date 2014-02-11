package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necromancy.client.model.ModelMinion;

public class RenderMinion extends RenderLiving
{
    
    public RenderMinion()
    {
        super(new ModelMinion(), 1.0F);
        ((ModelMinion)mainModel).renderer = this;
        setRenderPassModel(mainModel);
    }

    @Override
    public void bindTexture(ResourceLocation par1ResourceLocation)
    {
        this.renderManager.renderEngine.bindTexture(par1ResourceLocation);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return new ResourceLocation("textures/entity/zombie/zombie.png");
    }
}
