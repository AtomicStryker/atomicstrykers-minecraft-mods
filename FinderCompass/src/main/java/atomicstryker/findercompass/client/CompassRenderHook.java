package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayer;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map.Entry;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FinderCompassMod.MOD_ID)
public class CompassRenderHook implements ForgeLayer {

    private final int[] strongholdNeedlecolor = {102, 0, 153};
    private Minecraft mc = null;
    private final ItemStack compassStack = new ItemStack(Items.COMPASS);

    private double onScreenPositionWidth;
    private double onScreenPositionHeight;
    private double needleWidthOfScreenWidth;
    private double needleHeightOfScreenHeight;
    private Boolean mustHoldCompassInHandToBeActive = null;

    record Point(int x, int y) {
    }

    /**
     * copy over config values once
     */
    private void updateConfigValues() {
        if (mustHoldCompassInHandToBeActive == null) {
            // when connecting a client to a non-Findercompass server, this will be the first call to the config
            FinderCompassMod.instance.initIfNeeded();
            onScreenPositionWidth = FinderCompassMod.instance.compassConfig.getOnScreenPositionWidth();
            onScreenPositionHeight = FinderCompassMod.instance.compassConfig.getOnScreenPositionHeight();
            needleWidthOfScreenWidth = FinderCompassMod.instance.compassConfig.getNeedleWidthOfScreenWidth();
            needleHeightOfScreenHeight = FinderCompassMod.instance.compassConfig.getNeedleHeightOfScreenHeight();
            mustHoldCompassInHandToBeActive = FinderCompassMod.instance.compassConfig.isMustHoldCompassInHandToBeActive();
        }
    }

    private boolean playerHasCompass() {
        if (mc.player != null) {
            if (mustHoldCompassInHandToBeActive) {
                if (mc.player.getMainHandItem().getItem() == Items.COMPASS || mc.player.getOffhandItem().getItem() == Items.COMPASS) {
                    return true;
                }
            } else {
                int compassSlot = mc.player.getInventory().findSlotMatchingItem(compassStack);
                return Inventory.isHotbarSlot(compassSlot);
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void registerLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().addAbove(
                ResourceLocation.fromNamespaceAndPath(FinderCompassMod.MOD_ID, FinderCompassMod.MOD_ID),
                ForgeLayeredDraw.SLEEP_OVERLAY,
                new CompassRenderHook());
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (mc == null) {
            mc = Minecraft.getInstance();
        }
        updateConfigValues();

        if (playerHasCompass()) {
            renderCompassNeedles(guiGraphics);
        }
    }

    private void renderCompassNeedles(GuiGraphics guiGraphics) {

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();

        int widthNeedle = (int) Math.rint(screenWidth * (needleWidthOfScreenWidth / 2));
        int heightNeedle = (int) Math.rint(screenHeight * (needleHeightOfScreenHeight / 2));

        int originPointX = (int) Math.rint(screenWidth * onScreenPositionWidth);
        int originPointY = (int) Math.rint(screenHeight * onScreenPositionHeight);

        // shift the origin x by half needle width so it rotates cleanly around the center
        originPointX -= widthNeedle / 2;

        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet()) {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            drawNeedle(guiGraphics, widthNeedle, heightNeedle, originPointX, originPointY,
                    configInts[0], configInts[1], configInts[2], computeNeedleHeading(entryTarget.getValue()));
        }

        if (css.getFeatureNeedle() != null && FinderCompassLogic.hasFeature) {
            drawNeedle(guiGraphics, widthNeedle, heightNeedle, originPointX, originPointY,
                    strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2],
                    computeNeedleHeading(FinderCompassLogic.featureCoords));
        }
    }

    private void drawNeedle(GuiGraphics guiGraphics, int widthNeedle, int heightNeedle,
                            int originPointX, int originPointY, int r, int g, int b, float angleDegrees) {

        // convert angleDegrees to radians
        float angleRadian = (float) (Math.toRadians(angleDegrees));

        // construct a color integer by bit shifting rgb together
        int color = 0xff000000;
        color |= b;
        color |= g << 8;
        color |= r << 16;

        // push pose to not mess up other renderers
        guiGraphics.pose().pushMatrix();

        // move our draw starting point to the needle center
        guiGraphics.pose().translate(originPointX, originPointY);
        // rotate our draw by the needle rotation
        guiGraphics.pose().rotate(angleRadian);

        // dont start in the center so we dont overlap with the crosshair so much
        int startHeight = heightNeedle / 2;
        int halfWidth = widthNeedle / 2;

        // ask guiGraphics to draw us a filled rectangle in the rotated view
        // note we dont start drawing at zero but negative half width
        // so the needles are centered properly
        guiGraphics.fill(startHeight, -halfWidth, heightNeedle, widthNeedle, color);

        // pop pose to reset rendering to where it was before we started drawing
        guiGraphics.pose().popMatrix();
    }

    /**
     * this was klepped from MC source code many years ago and i make random changes until it works
     */
    private float computeNeedleHeading(BlockPos coords) {
        double angleDegrees = 0.0D;
        if (mc.level != null && mc.player != null) {
            double playerX = mc.player.getX();
            double playerZ = mc.player.getZ();
            // int block coordinates are for their starting corners, add .5 to get center coords
            double blockX = coords.getX() + 0.5D;
            double blockZ = coords.getZ() + 0.5D;
            double xDiff = playerX - blockX;
            double zDiff = playerZ - blockZ;
            angleDegrees = mc.player.getYRot() * Math.PI / 180.0D - Math.atan2(zDiff, xDiff);
        }

        return (float) -(angleDegrees * 180f / Math.PI);
    }
}
