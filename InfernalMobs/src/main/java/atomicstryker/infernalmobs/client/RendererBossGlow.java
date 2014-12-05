package atomicstryker.infernalmobs.client;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

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
        EntityLivingBase viewEnt = (EntityLivingBase) mc.getRenderViewEntity();
        Vec3 curPos = viewEnt.getPositionVector();
        
        Frustum f = new Frustum();
        double var7 = viewEnt.lastTickPosX + (viewEnt.posX - viewEnt.lastTickPosX) * (double)renderTick;
        double var9 = viewEnt.lastTickPosY + (viewEnt.posY - viewEnt.lastTickPosY) * (double)renderTick;
        double var11 = viewEnt.lastTickPosZ + (viewEnt.posZ - viewEnt.lastTickPosZ) * (double)renderTick;
        f.setPosition(var7, var9, var11);
        
        Map<EntityLivingBase, MobModifier> mobsmap = InfernalMobsCore.proxy.getRareMobs();
        for (EntityLivingBase ent : mobsmap.keySet())
        {
            if (ent.isInRangeToRenderDist(curPos.squareDistanceTo(ent.getPositionVector()))
            && (ent.ignoreFrustumCheck || f.isBoundingBoxInFrustum(ent.getBoundingBox()))
            && ent.isEntityAlive())
            {
                mc.renderGlobal.spawnParticle(EnumParticleTypes.SPELL_MOB.getParticleID(),
                        EnumParticleTypes.SPELL_MOB.func_179344_e(), ent.posX + (ent.worldObj.rand.nextDouble() - 0.5D) * (double)ent.width,
                        ent.posY + ent.worldObj.rand.nextDouble() * (double)ent.height - 0.25D,
                        ent.posZ + (ent.worldObj.rand.nextDouble() - 0.5D) * (double)ent.width,
                        (ent.worldObj.rand.nextDouble() - 0.5D) * 2.0D,
                        -ent.worldObj.rand.nextDouble(),
                        (ent.worldObj.rand.nextDouble() - 0.5D) * 2.0D, null);
            }
        }
    }
}
