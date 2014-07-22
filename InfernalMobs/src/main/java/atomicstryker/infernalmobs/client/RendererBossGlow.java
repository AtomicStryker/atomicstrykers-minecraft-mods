package atomicstryker.infernalmobs.client;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class RendererBossGlow
{
    private static long lastRender = 0L;
    
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        if (System.currentTimeMillis() > lastRender+10L)
        {
            lastRender = System.currentTimeMillis();
            
            renderBossGlow(event.partialTicks);
        }
    }
    
    private void renderBossGlow(float renderTick)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase viewEnt = mc.renderViewEntity;
        Vec3 curPos = viewEnt.getPosition(renderTick);
        
        Frustrum f = new Frustrum();
        double var7 = viewEnt.lastTickPosX + (viewEnt.posX - viewEnt.lastTickPosX) * (double)renderTick;
        double var9 = viewEnt.lastTickPosY + (viewEnt.posY - viewEnt.lastTickPosY) * (double)renderTick;
        double var11 = viewEnt.lastTickPosZ + (viewEnt.posZ - viewEnt.lastTickPosZ) * (double)renderTick;
        f.setPosition(var7, var9, var11);
        
        Map<EntityLivingBase, MobModifier> mobsmap = InfernalMobsCore.proxy.getRareMobs();
        for (EntityLivingBase ent : mobsmap.keySet())
        {
            if (ent.isInRangeToRenderDist(curPos.distanceTo(ent.getPosition(1.0f)))
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
