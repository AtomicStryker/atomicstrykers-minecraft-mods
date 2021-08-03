package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Map.Entry;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FinderCompassMod.MOD_ID)
public class CompassRenderHook {

    private static final float[] strongholdNeedlecolor = {0.4f, 0f, 0.6f};
    private static Minecraft mc = null;
    private static final ItemStack compassStack = new ItemStack(Items.COMPASS);

    @SubscribeEvent
    public static void onTick(RenderGameOverlayEvent.Post event) {
        // Post and ALL is after the forge ingame gui has finished rendering, we draw ontop
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (mc == null) {
                mc = Minecraft.getInstance();
            }

            if (playerHasCompass()) {
                renderCompassNeedles(ItemTransforms.TransformType.GUI, event.getMatrixStack());
            }
        }
    }

    private static boolean playerHasCompass() {
        if (mc.player != null) {
            return mc.player.getInventory().contains(compassStack);
        }
        return false;
    }

//    public static void renderItemHook(ItemTransforms.TransformType transformType, MatrixStack matrixStackIn) {
//        if (FinderCompassClientTicker.instance != null) {
//            if (mc == null) {
//                mc = Minecraft.getInstance();
//            }
//            if (transformType == GUI) {
//                renderCompassNeedles(transformType, matrixStackIn);
//            } else if (transformType == FIRST_PERSON_RIGHT_HAND || transformType == FIRST_PERSON_LEFT_HAND) {
//                // disabled. the transforms fight me too much.
//                // renderCompassNeedles(transformType, matrixStackIn);
//            }
//        }
//    }

    // these values are here for on-the-fly adjusting in a debugger...
    private static float lefthandX = 0.3F;
    private static float lefthandY = 0.3F;
    private static float lefthandZ = 0F;

    private static float righthandX = -0.2F;
    private static float righthandY = 0.3F;
    private static float righthandZ = 0F;

    private static double handMultiplier = 0.5D;
    private static double debugMultiplier = 10000D;

    private static void renderCompassNeedles(ItemTransforms.TransformType transformType, PoseStack poseStack) {

        // save current ogl state for later
//        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

        // switch off ogl stuff that breaks our rendering needs
//        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // back-face culling can save some (tiny amount of) performance in third person perspective
        // GL11.glEnable(GL11.GL_CULL_FACE);
//        GL11.glDisable(GL11.GL_BLEND);

        // save modelview matrix for later restoration
//        GL11.glPushMatrix();

//        if (transformType == GUI) {
        // translate directly above the normal compass render and center a bit
//        GL11.glTranslatef(0.025f, 0.025f, 1.001f);
//        } else {
//            // apply finished vanilla matrix to move with the compass
//            Vector4f pos = new Vector4f(0, 0, 0, 1.0F);
//            Matrix4f matrix = matrixStackIn.getLast().getMatrix();
//            pos.transform(matrix);
//            GL11.glTranslatef(pos.getX(), pos.getY(), pos.getZ());
//            // then try to fix the needle positions, still broken
//            // needs rotation of some kind aswell because the compass is held at a stronger angle
//            if (transformType == FIRST_PERSON_LEFT_HAND) {
//                GL11.glTranslatef(lefthandX, lefthandY, lefthandZ);
//            } else {
//                GL11.glTranslatef(righthandX, righthandY, righthandZ);
//            }
//        }

        Camera camera = mc.gameRenderer.getMainCamera();
        poseStack.pushPose();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        RenderSystem.enableBlend();

        poseStack.translate(mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2, mc.gui.getBlitOffset());
        poseStack.mulPose(Vector3f.XN.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot()));
        poseStack.scale(-1.0F, -1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();

        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet()) {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            // GL11.glTranslatef(0, 0, 0.001f); // elevating the needles outside of drawNeedle() is better blackboxing
            poseStack.translate(0, 0, 0.001D);
            drawNeedle(poseStack, transformType, (float) configInts[0] / 255f, (float) configInts[1] / 255f, (float) configInts[2] / 255f, computeNeedleHeading(entryTarget.getValue()));
        }

        if (css.getFeatureNeedle() != null && FinderCompassLogic.hasFeature) {
            // GL11.glTranslatef(0, 0, 0.001f); // elevating the needles outside of drawNeedle() is better blackboxing
            poseStack.translate(0, 0, 0.001D);
            drawNeedle(poseStack, transformType, strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.featureCoords));
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();

//        // restore modelview matrix
//        GL11.glPopMatrix();
//
//        // restore ogl state
//        GL11.glPopAttrib();
    }

    private static void drawNeedle(PoseStack poseStack, ItemTransforms.TransformType transformType, float r, float g, float b, float angle) {
        // oldCode(transformType, r, g, b, angle);

        double sizeMultiplier = debugMultiplier;

        poseStack.pushPose();
        GlStateManager._disableTexture();
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        poseStack.scale(1.7875f, 0.8125f, 1f);

        /*
        for doing the same as glRotateF around the Z axis for angle A
        https://stackoverflow.com/questions/24585454/how-to-convert-glrotatef-to-multiplication-matrice-for-glmultmatrixd

             |  cos(A)  -sin(A)   0   0 |
         M = |  sin(A)   cos(A)   0   0 |
             |  0        0        1   0 |
             |  0        0        0   1 |

         */
        float a = -angle;
        float[] matrixForRotationAroundZAxis =
                {(float) Math.cos(a), (float) -Math.sin(a), 0, 0,
                        (float) Math.sin(a), (float) Math.cos(a), 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1};
        // poseStack.mulPoseMatrix(new Matrix4f(matrixForRotationAroundZAxis));

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(-0.03D * sizeMultiplier, -0.04D * sizeMultiplier, 0.0D).color(r, g, b, 0.85F).endVertex();
        bufferbuilder.vertex(0.03D * sizeMultiplier, -0.04D * sizeMultiplier, 0.0D).color(r, g, b, 0.85F).endVertex();
        bufferbuilder.vertex(0.03D * sizeMultiplier, 0.2D * sizeMultiplier, 0.0D).color(r, g, b, 0.85F).endVertex();
        bufferbuilder.vertex(-0.03D * sizeMultiplier, 0.2D * sizeMultiplier, 0.0D).color(r, g, b, 0.85F).endVertex();

        tesselator.end();
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
        GlStateManager._enableTexture();
        poseStack.popPose();
    }

    private static void oldCode(ItemTransforms.TransformType transformType, float r, float g, float b, float angle) {
        // save modelview matrix for later restoration
        GL11.glPushMatrix();
        // make the needle cover roughly the same elliptical shape as the default pixelled one
        GL11.glScalef(1.7875f, 0.8125f, 1f);

        GL11.glRotatef(-angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation

        // make the vertex much bigger for debugging - where did the damn thing go
        double sizeMultiplier = debugMultiplier;

        if (transformType != ItemTransforms.TransformType.GUI) {
            sizeMultiplier *= handMultiplier;
        }

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

    private static float computeNeedleHeading(BlockPos coords) {
        double angleRadian = 0.0D;
        if (mc.level != null && mc.player != null) {
            double xdiff = mc.player.getX() - (coords.getX() + 0.5D);
            double zdiff = mc.player.getZ() - (coords.getZ() + 0.5D);
            angleRadian = (mc.player.getYRot() - 90.0F) * Math.PI / 180.0D - Math.atan2(zdiff, xdiff);
        }

        return (float) -(angleRadian * 180f / Math.PI);
    }
}
