package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necromancy.entity.EntityIsaacBody;

public class RenderIsaacMelee extends RenderLiving
{

    public RenderIsaacMelee(ModelBase par1ModelBase, float par2)
    {
        super(par1ModelBase, par2);
    }

    @Override
    public void func_110827_b(EntityLiving el, double d1, double d2, double d3, float f1, float f2)
    {
        renderIsaacNormal((EntityIsaacBody) el, d1, d2, d3, f1, f2);
    }

    @Override
    public void doRender(Entity el, double d1, double d2, double d3, float f1, float f2)
    {
        renderIsaacNormal((EntityIsaacBody) el, d1, d2, d3, f1, f2);
    }

    public void renderIsaacNormal(EntityIsaacBody em, double d1, double d2, double d3, float f1, float f2)
    {
        super.func_110827_b(em, d1, d2, d3, f1, f2);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return new ResourceLocation("necromancy:textures/entities/Isaac.png");
    }
}
