package atomicstryker.infernalmobs.client;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.ModelBase;
import net.minecraft.src.OpenGlHelper;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.RenderLiving;
import net.minecraft.src.RenderManager;

public class RenderLivingGlow extends RenderLiving
{
    public RenderLivingGlow(ModelBase modelBase, float shadowSize)
    {
        super(modelBase, shadowSize);
        this.renderManager = RenderManager.instance;
    }
    
    public static RenderLivingGlow hackRenderLiving(RenderLiving toHack)
    {
        try
        {
            Field[] fields = RenderLiving.class.getDeclaredFields();
            fields[0].setAccessible(true);
            ModelBase mb = (ModelBase) fields[0].get(toHack);
            return new RenderLivingGlow(mb, 0F);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    public void doRenderLiving(EntityLiving ent, double partX, double partY, double partZ, float par8, float partialTick)
    {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        this.mainModel.onGround = this.renderSwingProgress(ent, partialTick);

        if (this.renderPassModel != null)
        {
            this.renderPassModel.onGround = this.mainModel.onGround;
        }

        this.mainModel.isRiding = ent.isRiding();

        if (this.renderPassModel != null)
        {
            this.renderPassModel.isRiding = this.mainModel.isRiding;
        }

        this.mainModel.isChild = ent.isChild();

        if (this.renderPassModel != null)
        {
            this.renderPassModel.isChild = this.mainModel.isChild;
        }

        try
        {
            float var19;
            int var18;
            float var20;
            float var22;
            
            float rotNormalizedOffset = this.normalizeRotation(ent.prevRenderYawOffset, ent.renderYawOffset, partialTick);
            float rotNormalized = this.normalizeRotation(ent.prevRotationYawHead, ent.rotationYawHead, partialTick);
            float partialPitch = ent.prevRotationPitch + (ent.rotationPitch - ent.prevRotationPitch) * partialTick;
            this.renderLivingAt(ent, partX, partY, partZ);
            float var13 = this.handleRotationFloat(ent, partialTick);
            this.rotateCorpse(ent, var13, rotNormalizedOffset, partialTick);
            float var14 = 0.0625F;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glScalef(-1.0F, -1.0F, 1.0F);
            this.preRenderCallback(ent, partialTick);
            GL11.glTranslatef(0.0F, -24.0F * var14 - 0.0078125F, 0.0F);
            float var15 = ent.prevLegYaw + (ent.legYaw - ent.prevLegYaw) * partialTick;
            float var16 = ent.legSwing - ent.legYaw * (1.0F - partialTick);

            if (ent.isChild())
            {
                var16 *= 3.0F;
            }

            if (var15 > 1.0F)
            {
                var15 = 1.0F;
            }

            GL11.glEnable(GL11.GL_ALPHA_TEST);
            this.mainModel.setLivingAnimations(ent, var16, var15, partialTick);
            
            var19 = (float)ent.ticksExisted + partialTick;
            
            if (renderManager.renderEngine == null)
            {
                return;
            }            
            renderManager.renderEngine.bindTexture(renderManager.renderEngine.getTexture("%blur%/misc/glint.png"));
            
            GL11.glEnable(GL11.GL_BLEND);
            var20 = 0.5F;
            GL11.glColor4f(var20, var20, var20, 1.0F);
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDepthMask(false);

            for (int var21 = 0; var21 < 2; ++var21)
            {
                GL11.glDisable(GL11.GL_LIGHTING);
                var22 = 0.76F;
                GL11.glColor4f(0.5F * var22, 0.25F * var22, 0.8F * var22, 1.0F);
                GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                float var23 = var19 * (0.001F + (float)var21 * 0.003F) * 20.0F;
                float var24 = 0.33333334F;
                GL11.glScalef(var24, var24, var24);
                GL11.glRotatef(30.0F - (float)var21 * 60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(0.0F, var23, 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                renderModel(ent, var16, var15, var13, rotNormalized - rotNormalizedOffset, partialPitch, var14);
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glDepthMask(true);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }
        catch (Exception var25)
        {
            var25.printStackTrace();
        }

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
        
        ent.worldObj.spawnParticle("mobSpell",
                ent.posX + (ent.worldObj.rand.nextDouble() - 0.5D) * (double)ent.width,
                ent.posY + ent.worldObj.rand.nextDouble() * (double)ent.height - 0.25D,
                ent.posZ + (ent.worldObj.rand.nextDouble() - 0.5D) * (double)ent.width,
                (ent.worldObj.rand.nextDouble() - 0.5D) * 2.0D,
                -ent.worldObj.rand.nextDouble(),
                (ent.worldObj.rand.nextDouble() - 0.5D) * 2.0D);
    }
    
    private float normalizeRotation(float par1, float par2, float par3)
    {
        float var4;

        for (var4 = par2 - par1; var4 < -180.0F; var4 += 360.0F)
        {
            ;
        }

        while (var4 >= 180.0F)
        {
            var4 -= 360.0F;
        }

        return par1 + par3 * var4;
    }
}
