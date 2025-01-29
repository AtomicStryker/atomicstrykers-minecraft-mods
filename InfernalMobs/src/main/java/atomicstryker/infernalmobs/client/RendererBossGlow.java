package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.SidedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = InfernalMobsCore.MOD_ID)
public class RendererBossGlow {

    @SubscribeEvent
    public static void onRegisterGuis(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(InfernalMobsCore.MOD_ID + "_bossglow", new InfernalMobsBossGlowOverlay());
    }

    public static class InfernalMobsBossGlowOverlay implements IGuiOverlay {
        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
            Minecraft mc = Minecraft.getInstance();
            Entity viewEnt = mc.getCameraEntity();
            if (mc.isPaused() || viewEnt == null) {
                return;
            }
            Vec3 curPos = viewEnt.position();
            Map<LivingEntity, MobModifier> mobsmap = SidedCache.getInfernalMobs(viewEnt.level());
            mobsmap.keySet().stream().filter(entity -> entity.shouldRenderAtSqrDistance(curPos.distanceToSqr(entity.position()))
                    && entity.isAlive()).forEach(entity -> mc.levelRenderer.addParticle(ParticleTypes.WITCH,
                    false, entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getBbWidth(),
                    entity.getY() + entity.getRandom().nextDouble() * (double) entity.getBbHeight() - 0.25D,
                    entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getBbWidth(),
                    (entity.getRandom().nextDouble() - 0.5D) * 2.0D,
                    -entity.getRandom().nextDouble(),
                    (entity.getRandom().nextDouble() - 0.5D) * 2.0D));
        }
    }
}
