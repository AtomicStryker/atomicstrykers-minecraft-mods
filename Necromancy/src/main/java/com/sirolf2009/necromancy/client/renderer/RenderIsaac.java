package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

public class RenderIsaac extends RenderLiving
{

    public RenderIsaac(ModelBase par1ModelBase, float par2)
    {
        super(par1ModelBase, par2);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return ReferenceNecromancy.TEXTURES_ENTITIES_ISAAC;
    }
}
