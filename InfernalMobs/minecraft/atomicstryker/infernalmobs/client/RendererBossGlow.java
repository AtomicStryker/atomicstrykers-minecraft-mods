package atomicstryker.infernalmobs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class RendererBossGlow
{
    private static long lastRender = 0L;
    
    @ForgeSubscribe
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        if (System.currentTimeMillis() > lastRender+10L)
        {
            lastRender = System.currentTimeMillis();
            renderBossGlow(event.partialTicks, event.context.mc);
        }
    }
    
    private void renderBossGlow(float renderTick, Minecraft mc)
    {
        EntityLiving viewEnt = mc.renderViewEntity;
        Vec3 curPos = viewEnt.getPosition(renderTick);
        
        Frustrum f = new Frustrum();
        double var7 = viewEnt.lastTickPosX + (viewEnt.posX - viewEnt.lastTickPosX) * (double)renderTick;
        double var9 = viewEnt.lastTickPosY + (viewEnt.posY - viewEnt.lastTickPosY) * (double)renderTick;
        double var11 = viewEnt.lastTickPosZ + (viewEnt.posZ - viewEnt.lastTickPosZ) * (double)renderTick;
        f.setPosition(var7, var9, var11);
        
        for (EntityLiving ent : InfernalMobsCore.getRareMobs().keySet())
        {
            if (ent.isInRangeToRenderVec3D(curPos)
            && (ent.ignoreFrustumCheck || f.isBoundingBoxInFrustum(ent.boundingBox))
            && ent.isEntityAlive())
            {
                //RenderManager.instance.renderEntity(ent, renderTick);
                renderEntityGlowing(ent, renderTick, RenderManager.instance);
            }
        }
    }

    private void renderEntityGlowing(EntityLiving ent, float renderTick, RenderManager rendermanager)
    {
        double xPart = ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * (double)renderTick;
        double yPart = ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * (double)renderTick;
        double zPart = ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * (double)renderTick;
        float rotPart = ent.prevRotationYaw + (ent.rotationYaw - ent.prevRotationYaw) * renderTick;
        int brightness = ent.getBrightnessForRender(renderTick);

        if (ent.isBurning())
        {
            brightness = 15728880;
        }

        int brightnessOver = brightness % 65536;
        int brightnessMain = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)brightnessOver / 1.0F, (float)brightnessMain / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        renderEntityWithPosYaw(rendermanager, ent, xPart - rendermanager.renderPosX, yPart - rendermanager.renderPosY, zPart - rendermanager.renderPosZ, rotPart, renderTick);
    }
    
    private void renderEntityWithPosYaw(RenderManager rendermanager, EntityLiving ent, double d, double e, double f, float var9, float renderTick)
    {
        Render renderObject = rendermanager.getEntityRenderObject(ent);
        if (renderObject != null && renderObject instanceof RenderLiving)
        {
            try
            {
                RenderLivingGlow r = RenderLivingGlow.hackRenderLiving((RenderLiving) renderObject);
                r.doRenderLiving(ent, d, e, f, var9, renderTick);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }
}
