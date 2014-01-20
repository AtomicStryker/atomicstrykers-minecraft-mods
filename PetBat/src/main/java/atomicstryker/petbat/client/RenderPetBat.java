package atomicstryker.petbat.client;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import atomicstryker.petbat.common.EntityPetBat;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPetBat extends RenderLiving
{
    private ModelPetBat renderModel;
    private ResourceLocation tex = new ResourceLocation("petbat", "textures/model/petbat.png");
    private ResourceLocation texGlis = new ResourceLocation("petbat", "textures/model/petbat_glister.png");
    
    public RenderPetBat()
    {
        super(new ModelPetBat(), 0.25F);
        renderModel = new ModelPetBat();
    }
    
    @Override
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
        this.scaleBat((EntityPetBat)par1EntityLivingBase, par2);
    }

    @Override
    protected void rotateCorpse(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4)
    {
        this.rotateRenderedModel((EntityPetBat)par1EntityLivingBase, par2, par3, par4);
    }
    
    @Override
    protected void passSpecialRender(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6)
    {
        String name = ((EntityPetBat)par1EntityLivingBase).getDisplayName();
        if (!name.equals(""))
        {
            func_147906_a(par1EntityLivingBase, name, par2, par4-1D, par6, 64);
        }
        super.passSpecialRender(par1EntityLivingBase, par2, par4, par6);
    }
    
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

    private void scaleBat(EntityPetBat par1EntityPetBat, float par2)
    {
        GL11.glScalef(0.35F, 0.35F, 0.35F);
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
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return ((EntityPetBat)entity).glister ? texGlis : tex;
    }
    
}
