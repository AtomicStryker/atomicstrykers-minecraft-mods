package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = InfernalMobsCore.MOD_ID)
public class RendererBossGlow {

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        Minecraft mc = Minecraft.getInstance();

        LayeredDraw layers;
        for (Field field : mc.gui.getClass().getDeclaredFields()) {
            if (field.getType().isAssignableFrom(LayeredDraw.class)) {
                field.setAccessible(true);
                try {
                    layers = (LayeredDraw) field.get(mc.gui);
                    layers.add(new InfernalMobsBossGlowOverlay());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class InfernalMobsBossGlowOverlay implements LayeredDraw.Layer {
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            Entity viewEnt = mc.getCameraEntity();
            if (mc.isPaused() || viewEnt == null) {
                return;
            }
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
