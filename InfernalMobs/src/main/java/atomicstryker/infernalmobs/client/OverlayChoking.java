package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = InfernalMobsCore.MOD_ID)
public class OverlayChoking {

    private static Minecraft mc;

    private static int airOverrideValue = -999;
    private static long airDisplayTimeout;

    public static void onAirPacket(int air) {
        airOverrideValue = air;
        airDisplayTimeout = System.currentTimeMillis() + 3000L;
    }

    @SubscribeEvent
    public static void onRegisterGuis(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(InfernalMobsCore.MOD_ID + "_choking", new InfernalMobsChokingGuiOverlay());
        mc = Minecraft.getInstance();
    }

    public static class InfernalMobsChokingGuiOverlay implements IGuiOverlay {
        @Override
        public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int width, int height) {
            if (System.currentTimeMillis() > airDisplayTimeout) {
                airOverrideValue = -999;
            }

            // modded Gui.renderPlayerHealth 'air' section
            if (!mc.player.isEyeInFluid(FluidTags.WATER) && airOverrideValue != -999) {

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

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
                        mc.gui.blit(poseStack, leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 16, 18, 9, 9);
                    } else {
                        mc.gui.blit(poseStack, leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 25, 18, 9, 9);
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
