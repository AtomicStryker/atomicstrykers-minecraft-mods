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
                mc.renderGlobal.spawnParticle("mobSpell",
                        ent.posX + (ent.worldObj.rand.nextDouble() - 0.5D) * (double)ent.width,
                        ent.posY + ent.worldObj.rand.nextDouble() * (double)ent.height - 0.25D,
                        ent.posZ + (ent.worldObj.rand.nextDouble() - 0.5D) * (double)ent.width,
                        (ent.worldObj.rand.nextDouble() - 0.5D) * 2.0D,
                        -ent.worldObj.rand.nextDouble(),
                        (ent.worldObj.rand.nextDouble() - 0.5D) * 2.0D);
            }
        }
    }
}
