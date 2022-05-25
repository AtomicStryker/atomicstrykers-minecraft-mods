package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = InfernalMobsCore.MOD_ID)
public class RendererBossGlow {
    private static long lastRender = 0L;

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelLastEvent event) {
        if (System.currentTimeMillis() > lastRender + 10L) {
            lastRender = System.currentTimeMillis();

            renderBossGlow();
        }
    }

    private static void renderBossGlow() {
        Minecraft mc = Minecraft.getInstance();
        Entity viewEnt = mc.getCameraEntity();
        if (mc.isPaused() || viewEnt == null) {
            return;
        }
        Vec3 curPos = viewEnt.position();
        Map<LivingEntity, MobModifier> mobsmap = SidedCache.getInfernalMobs(viewEnt.level);
        mobsmap.keySet().stream().filter(ent -> ent.shouldRenderAtSqrDistance(curPos.distanceToSqr(ent.position()))
                && ent.isAlive()).forEach(ent -> mc.levelRenderer.addParticle(ParticleTypes.WITCH,
                false, ent.getX() + (ent.level.random.nextDouble() - 0.5D) * (double) ent.getBbWidth(),
                ent.getY() + ent.level.random.nextDouble() * (double) ent.getBbHeight() - 0.25D,
                ent.getZ() + (ent.level.random.nextDouble() - 0.5D) * (double) ent.getBbWidth(),
                (ent.level.random.nextDouble() - 0.5D) * 2.0D,
                -ent.level.random.nextDouble(),
                (ent.level.random.nextDouble() - 0.5D) * 2.0D));
    }
}
