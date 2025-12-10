package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.network.AirPacket;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(value = Dist.CLIENT, modid = InfernalMobsCore.MOD_ID)
public class OverlayChoking {

    private static final Identifier GUI_ICONS_LOCATION = Identifier.parse("textures/gui/icons.png");
    private static final Identifier AIR_SPRITE = Identifier.withDefaultNamespace("hud/air");
    private static final Identifier AIR_POPPING_SPRITE = Identifier.withDefaultNamespace("hud/air_bursting");

    private static Minecraft mc;

    private static int airOverrideValue = -999;
    private static long airDisplayTimeout;

    public static void handleAirPacket(final AirPacket airPacket, final IPayloadContext context) {
        airOverrideValue = airPacket.air();
        airDisplayTimeout = System.currentTimeMillis() + 3000L;
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(ModLoadingContext.get().getActiveNamespace(), InfernalMobsCore.MOD_ID + "_choking"), new InfernalMobsChokingGuiOverlay());
        mc = Minecraft.getInstance();
    }

    public static class InfernalMobsChokingGuiOverlay implements GuiLayer {
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, DeltaTracker partialTick) {
            if (System.currentTimeMillis() > airDisplayTimeout) {
                airOverrideValue = -999;
            }

            // modded Gui.renderPlayerHealth 'air' section
            if (!mc.player.isEyeInFluid(FluidTags.WATER) && airOverrideValue != -999) {

                // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                int leftScreenCoordinate = mc.getWindow().getGuiScaledWidth() / 2 + 91;
                int topScreenCoordinate = mc.getWindow().getGuiScaledHeight() - 59;
                int maxHearts = getVehicleMaxHearts(mc.player);
                int maxAir = mc.player.getMaxAirSupply();
                int currentAir = Math.min(airOverrideValue, maxAir);
                int rowCount = getVisibleVehicleHeartRows(maxHearts) - 1;
                topScreenCoordinate = topScreenCoordinate - rowCount * 10;
                int fullBubbles = Mth.ceil((double) (currentAir - 2) * 10.0D / (double) maxAir);
                int partialBubbles = Mth.ceil((double) currentAir * 10.0D / (double) maxAir) - fullBubbles;

                for (int j5 = 0; j5 < fullBubbles + partialBubbles; ++j5) {
                    if (j5 < fullBubbles) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, AIR_SPRITE, leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 9, 9);
                    } else {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, AIR_POPPING_SPRITE, leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 9, 9);
                    }
                }
            }
        }
    }

    private static int getVehicleMaxHearts(LivingEntity livingEntity) {
        if (livingEntity != null && livingEntity.showVehicleHealth()) {
            float maxHealth = livingEntity.getMaxHealth();
            int roundedHalf = (int) (maxHealth + 0.5F) / 2;
            if (roundedHalf > 30) {
                roundedHalf = 30;
            }

            return roundedHalf;
        } else {
            return 0;
        }
    }

    private static int getVisibleVehicleHeartRows(int heartCount) {
        return (int) Math.ceil((double) heartCount / 10.0D);
    }

}
