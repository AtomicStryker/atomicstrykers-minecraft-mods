package atomicstryker.petbat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import atomicstryker.petbat.common.EntityPetBat;

public class RenderPetBat extends RenderLiving
{
    private ModelPetBat renderModel;
    private ResourceLocation tex = new ResourceLocation("petbat", "textures/model/petbat.png");
    private ResourceLocation texGlis = new ResourceLocation("petbat", "textures/model/petbat_glister.png");
    
    @SuppressWarnings("unchecked")
	public RenderPetBat()
    {
        super(Minecraft.getMinecraft().getRenderManager(), new ModelPetBat(), 0.25F);
        renderModel = new ModelPetBat();
        layerRenderers.add(renderModel);
    }
    
    @Override
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
    	GL11.glScalef(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void rotateCorpse(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4)
    {
        this.rotateRenderedModel((EntityPetBat)par1EntityLivingBase, par2, par3, par4);
    }
    
    private void rotateRenderedModel(EntityPetBat par1EntityPetBat, float par2, float par3, float par4)
    {
        if (!par1EntityPetBat.getIsBatHanging())
        {
            GL11.glTranslatef(0.0F, MathHelper.cos(par2 * 0.3F) * 0.1F, 0.0F);
        }
        else
        {
            GL11.glTranslatef(0.0F, -0.1F, 0.0F);
        }

        super.rotateCorpse(par1EntityPetBat, par2, par3, par4);
    }
    
    @Override
	public void passSpecialRender(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6)
    {
        String name = ((EntityPetBat)par1EntityLivingBase).getCommandSenderName();
        if (!name.equals(""))
        {
        	renderLivingLabel(par1EntityLivingBase, name, par2, par4-1D, par6, 64);
        }
        super.passSpecialRender(par1EntityLivingBase, par2, par4, par6);
    }
    
    /*
     * should maybe already work by layerRenderers.add in constructor
     * 
    @Override
    protected int shouldRenderPass(EntityLivingBase par1EntityLivingBase, int par2, float par3)
    {
        if (par2 == 2 && ((EntityPetBat)par1EntityLivingBase).getBatLevel() > 5)
        {
            setRenderPassModel(renderModel);
            return 15;
        }
        
        return -1;
    }
    */

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return ((EntityPetBat)entity).glister ? texGlis : tex;
    }
    
}
