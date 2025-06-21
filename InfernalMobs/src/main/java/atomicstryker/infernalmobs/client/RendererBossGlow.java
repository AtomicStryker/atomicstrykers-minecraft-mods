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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = InfernalMobsCore.MOD_ID)
public class RendererBossGlow {

    protected static long nextParticleTimeMillis;

    @SubscribeEvent
    public static void onRenderTickPost(TickEvent.RenderTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Entity viewEnt = mc.getCameraEntity();
        if (mc.isPaused() || viewEnt == null) {
            return;
        }
        if (System.currentTimeMillis() < nextParticleTimeMillis) {
            return;
        }
        nextParticleTimeMillis = System.currentTimeMillis() + 100L;
        Vec3 curPos = viewEnt.position();
        Map<LivingEntity, MobModifier> mobsmap = SidedCache.getInfernalMobs(viewEnt.level());
        mobsmap.keySet().stream().filter(ent -> ent.shouldRenderAtSqrDistance(curPos.distanceToSqr(ent.position()))
                && ent.isAlive()).forEach(ent -> mc.levelRenderer.addParticle(ParticleTypes.WITCH,
                false, ent.getX() + (ent.getRandom().nextDouble() - 0.5D) * (double) ent.getBbWidth(),
                ent.getY() + ent.getRandom().nextDouble() * (double) ent.getBbHeight() - 0.25D,
                ent.getZ() + (ent.getRandom().nextDouble() - 0.5D) * (double) ent.getBbWidth(),
                (ent.getRandom().nextDouble() - 0.5D) * 2.0D,
                -ent.getRandom().nextDouble(),
                (ent.getRandom().nextDouble() - 0.5D) * 2.0D));
    }
}
