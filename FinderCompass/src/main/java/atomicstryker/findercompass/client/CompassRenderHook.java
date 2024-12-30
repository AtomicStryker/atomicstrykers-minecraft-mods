package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.Map.Entry;

@SuppressWarnings("unused")
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = FinderCompassMod.MOD_ID)
public class CompassRenderHook {

    private static final int[] strongholdNeedlecolor = {102, 0, 153};
    private static Minecraft mc = null;
    private static final ItemStack compassStack = new ItemStack(Items.COMPASS);

    private static double onScreenPositionWidth;
    private static double onScreenPositionHeight;
    private static double needleWidthOfScreenWidth;
    private static double needleHeightOfScreenHeight;
    private static Boolean mustHoldCompassInHandToBeActive = null;

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        Minecraft mc = Minecraft.getInstance();
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(FinderCompassMod.MOD_ID, "findercompassrenderer"),
                new FinderCompassGuiOverlay());
    }

    public static class FinderCompassGuiOverlay implements LayeredDraw.Layer {
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, DeltaTracker partialTick) {
            if (mc == null) {
                mc = Minecraft.getInstance();
            }
            updateConfigValues();
            if (playerHasCompass()) {
                renderCompassNeedles(guiGraphics.pose());
                //renderTestQuad(event.getMatrixStack(), 45);
            }
        }
    }

    /**
     * copy over config values once
     */
    private static void updateConfigValues() {
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

    private static boolean playerHasCompass() {
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

    private static void renderCompassNeedles(PoseStack poseStack) {

        poseStack.pushPose();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        // make the needles somewhat transparent
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);

        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();

        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet()) {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            drawNeedle(screenWidth, screenHeight, configInts[0], configInts[1], configInts[2], computeNeedleHeading(entryTarget.getValue()));
        }

        if (css.getFeatureNeedle() != null && FinderCompassLogic.hasFeature) {
            drawNeedle(screenWidth, screenHeight, strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.featureCoords));
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }

    private static void drawNeedle(int screenWidth, int screenHeight, int r, int g, int b, float angle) {

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int halfWidthNeedle = (int) Math.rint(screenWidth * (needleWidthOfScreenWidth / 2));
        int halfHeightNeedle = (int) Math.rint(screenHeight * (needleHeightOfScreenHeight / 2));

        int originPointX = (int) Math.rint(screenWidth * onScreenPositionWidth);
        int originPointY = (int) Math.rint(screenHeight * onScreenPositionHeight);

        // we want the resulting thin, long rectangle to point straight up above the origin point unrotated
        int bottomLeftX = originPointX - halfWidthNeedle;
        int bottomLeftY = originPointY - halfHeightNeedle;

        int bottomRightX = originPointX + halfWidthNeedle;
        int bottomRightY = bottomLeftY;

        int topRightX = bottomRightX;
        int topRightY = bottomLeftY - (2 * halfHeightNeedle);

        int topLeftX = bottomLeftX;
        int topLeftY = topRightY;

        // now do some "rotate point around another point" math
        // im sure this is inefficient and terrible. PR me an improvement.
        double angleRadian = Math.toRadians(angle);
        Point rotatedBottomLeft = rotateAroundPointByAngle(new Point(bottomLeftX, bottomLeftY), new Point(originPointX, originPointY), angleRadian);
        Point rotatedBottomRight = rotateAroundPointByAngle(new Point(bottomRightX, bottomRightY), new Point(originPointX, originPointY), angleRadian);
        Point rotatedTopRight = rotateAroundPointByAngle(new Point(topRightX, topRightY), new Point(originPointX, originPointY), angleRadian);
        Point rotatedTopLeft = rotateAroundPointByAngle(new Point(topLeftX, topLeftY), new Point(originPointX, originPointY), angleRadian);

        // buttom left corner
        bufferbuilder.addVertex(rotatedBottomLeft.x, rotatedBottomLeft.y, -90.0F).setColor(r, g, b, 120);
        // bottom right corner
        bufferbuilder.addVertex(rotatedBottomRight.x, rotatedBottomRight.y, -90.0F).setColor(r, g, b, 120);
        // top right corner
        bufferbuilder.addVertex(rotatedTopRight.x, rotatedTopRight.y, -90.0F).setColor(r, g, b, 120);
        // top left corner
        bufferbuilder.addVertex(rotatedTopLeft.x, rotatedTopLeft.y, -90.0F).setColor(r, g, b, 120);

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    private static float computeNeedleHeading(BlockPos coords) {
        double angleRadian = 0.0D;
        if (mc.level != null && mc.player != null) {
            double xdiff = mc.player.getX() - (coords.getX() + 0.5D);
            double zdiff = mc.player.getZ() - (coords.getZ() + 0.5D);
            angleRadian = (mc.player.getYRot() - 90.0F) * Math.PI / 180.0D - Math.atan2(zdiff, xdiff);
        }

        return (float) -(angleRadian * 180f / Math.PI);
    }

    record Point(int x, int y) {
    }

    private static Point rotateAroundPointByAngle(Point toRotate, Point toRotateAround, double angleRadian) {
        double xRotated = Math.cos(angleRadian) * (toRotate.x - toRotateAround.x) - Math.sin(angleRadian) * (toRotate.y - toRotateAround.y) + toRotateAround.x;
        double yRotated = Math.sin(angleRadian) * (toRotate.x - toRotateAround.x) + Math.cos(angleRadian) * (toRotate.y - toRotateAround.y) + toRotateAround.y;
        return new Point((int) Math.rint(xRotated), (int) Math.rint(yRotated));
    }

    private static void oldCode(float r, float g, float b, float angle) {
        // save modelview matrix for later restoration
        GL11.glPushMatrix();
        // make the needle cover roughly the same elliptical shape as the default pixelled one
        GL11.glScalef(1.7875f, 0.8125f, 1f);

        GL11.glRotatef(-angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation

        // make the vertex much bigger for debugging - where did the damn thing go
        double sizeMultiplier = 100D;

        // alternative native ogl code
        GL11.glBegin(GL11.GL_QUADS); // set ogl mode, need quads
        GL11.glColor4f(r, g, b, 0.85F); // set color

        // now draw each glorious needle as single quad
        GL11.glVertex3d(-0.03D * sizeMultiplier, -0.04D * sizeMultiplier, 0.0D); // lower left
        GL11.glVertex3d(0.03D * sizeMultiplier, -0.04D * sizeMultiplier, 0.0D); // lower right
        GL11.glVertex3d(0.03D * sizeMultiplier, 0.2D * sizeMultiplier, 0.0D); // upper right
        GL11.glVertex3d(-0.03D * sizeMultiplier, 0.2D * sizeMultiplier, 0.0D); // upper left

        GL11.glEnd(); // let ogl draw it

        // restore modelview matrix
        GL11.glPopMatrix();
    }
}
