package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = InfernalMobsCore.MOD_ID)
public class RendererBossGlow {

    protected static long nextParticleTimeMillis;

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ModLoadingContext.get().getActiveNamespace(), InfernalMobsCore.MOD_ID + "_bossglow"), new InfernalMobsBossGlowOverlay());
    }

    public static class InfernalMobsBossGlowOverlay implements LayeredDraw.Layer {
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, DeltaTracker partialTick) {
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
                    false, ent.getX() + (ent.level().random.nextDouble() - 0.5D) * (double) ent.getBbWidth(),
                    ent.getY() + ent.level().random.nextDouble() * (double) ent.getBbHeight() - 0.25D,
                    ent.getZ() + (ent.level().random.nextDouble() - 0.5D) * (double) ent.getBbWidth(),
                    (ent.level().random.nextDouble() - 0.5D) * 2.0D,
                    -ent.level().random.nextDouble(),
                    (ent.level().random.nextDouble() - 0.5D) * 2.0D));
        }
    }
}
