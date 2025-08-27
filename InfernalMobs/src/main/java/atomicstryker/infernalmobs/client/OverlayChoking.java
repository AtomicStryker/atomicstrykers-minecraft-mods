package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = InfernalMobsCore.MOD_ID)
public class OverlayChoking implements ForgeLayer {

    private static final OverlayChoking INSTANCE = new OverlayChoking();

    private final ResourceLocation GUI_ICONS_LOCATION = ResourceLocation.parse("textures/gui/icons.png");
    private final ResourceLocation AIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/air");
    private final ResourceLocation AIR_POPPING_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_bursting");

    private Minecraft mc;

    private int airOverrideValue = -999;
    private long airDisplayTimeout;

    public static void onAirPacket(int air) {
        INSTANCE.airOverrideValue = air;
        INSTANCE.airDisplayTimeout = System.currentTimeMillis() + 3000L;
    }

    @SubscribeEvent
    public static void renderEvent(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(ResourceLocation.fromNamespaceAndPath(InfernalMobsCore.MOD_ID, "overlaychoking"), INSTANCE);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (System.currentTimeMillis() > airDisplayTimeout) {
            airOverrideValue = -999;
        }

        // modded Gui.renderPlayerHealth 'air' section
        if (!mc.player.isEyeInFluid(FluidTags.WATER) && airOverrideValue != -999) {

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

    private int getVehicleMaxHearts(LivingEntity livingEntity) {
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

    private int getVisibleVehicleHeartRows(int heartCount) {
        return (int) Math.ceil((double) heartCount / 10.0D);
    }

}
