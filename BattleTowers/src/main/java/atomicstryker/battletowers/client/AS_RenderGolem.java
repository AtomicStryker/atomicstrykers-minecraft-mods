package atomicstryker.battletowers.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import atomicstryker.battletowers.common.AS_EntityGolem;

public class AS_RenderGolem extends RenderBiped
{
    private static ResourceLocation texSleep = new ResourceLocation("battletowers", "textures/model/golemdormant.png");
    private static ResourceLocation texAwake = new ResourceLocation("battletowers", "textures/model/golem.png");

    public AS_RenderGolem(RenderManager rm)
    {
        super(rm, new ModelBiped(), 1.0F);
    }

    protected void rescaleGolem()
    {
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }
    
    @Override
    protected void preRenderCallback(EntityLivingBase entityliving, float f)
    {
        rescaleGolem();
    }
    
    @Override
    protected ResourceLocation getEntityTexture(EntityLiving par1EntityLiving)
    {
        boolean awake = !((AS_EntityGolem)par1EntityLiving).getIsDormant();
        return awake ? texAwake : texSleep;
    }
}
