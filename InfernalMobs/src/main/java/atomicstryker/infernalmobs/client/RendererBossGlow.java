package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = InfernalMobsCore.MOD_ID)
public class RendererBossGlow {
    private static long lastRender = 0L;

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (System.currentTimeMillis() > lastRender + 10L) {
            lastRender = System.currentTimeMillis();

            renderBossGlow();
        }
    }

    private static void renderBossGlow() {
        Minecraft mc = Minecraft.getInstance();
        Entity viewEnt = mc.getRenderViewEntity();
        if (viewEnt == null) {
            return;
        }
        Vector3d curPos = viewEnt.getPositionVec();
        Map<LivingEntity, MobModifier> mobsmap = SidedCache.getInfernalMobs(viewEnt.world);
        mobsmap.keySet().stream().filter(ent -> ent.isInRangeToRenderDist(curPos.squareDistanceTo(ent.getPositionVec()))
                && ent.isAlive()).forEach(ent -> mc.worldRenderer.addParticle(ParticleTypes.WITCH,
                false, ent.getPosX() + (ent.world.rand.nextDouble() - 0.5D) * (double) ent.getWidth(),
                ent.getPosY() + ent.world.rand.nextDouble() * (double) ent.getHeight() - 0.25D,
                ent.getPosZ() + (ent.world.rand.nextDouble() - 0.5D) * (double) ent.getWidth(),
                (ent.world.rand.nextDouble() - 0.5D) * 2.0D,
                -ent.world.rand.nextDouble(),
                (ent.world.rand.nextDouble() - 0.5D) * 2.0D));
    }
}
