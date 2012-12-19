package atomicstryker.battletowers.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.opengl.GL11;

import atomicstryker.battletowers.common.AS_EntityGolem;

public class AS_RenderGolem extends RenderBiped
{

    public AS_RenderGolem()
    {
        super(new ModelBiped(), 1.0F);
        setRenderPassModel(new ModelBiped());
    }

    protected void rescaleGolem(AS_EntityGolem entitygolem, float f)
    {
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }

    protected void preRenderCallback(EntityLiving entityliving, float f)
    {
        rescaleGolem((AS_EntityGolem)entityliving, f);
    }
}
