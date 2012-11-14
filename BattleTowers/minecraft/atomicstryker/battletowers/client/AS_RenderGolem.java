package atomicstryker.battletowers.client;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.ModelBiped;
import net.minecraft.src.RenderBiped;

import org.lwjgl.opengl.GL11;

import atomicstryker.battletowers.common.AS_EntityGolem;

public class AS_RenderGolem extends RenderBiped
{

    public AS_RenderGolem()
    {
        super(new ModelBiped(), 1.0F);
        setRenderPassModel(new ModelBiped());
    }

    protected void func_15310_scalegolem(AS_EntityGolem entitygolem, float f)
    {
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }

    protected void preRenderCallback(EntityLiving entityliving, float f)
    {
        func_15310_scalegolem((AS_EntityGolem)entityliving, f);
    }
}
