package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.Map.Entry;

import static net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.*;

public class CompassRenderHook {

    private static final float[] strongholdNeedlecolor = {0.4f, 0f, 0.6f};
    private static Minecraft mc;

    public static void renderItemHook(ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn) {
        if (FinderCompassClientTicker.instance != null) {
            if (mc == null) {
                mc = Minecraft.getInstance();
            }
            if (transformType == GUI) {
                renderCompassNeedles(transformType, matrixStackIn);
            } else if (transformType == FIRST_PERSON_RIGHT_HAND || transformType == FIRST_PERSON_LEFT_HAND) {
                // disabled. the transforms fight me too much.
                // renderCompassNeedles(transformType, matrixStackIn);
            }
        }
    }

    // these values are here for on-the-fly adjusting in a debugger...
    private static float lefthandX = 0.3F;
    private static float lefthandY = 0.3F;
    private static float lefthandZ = 0F;

    private static float righthandX = -0.2F;
    private static float righthandY = 0.3F;
    private static float righthandZ = 0F;

    private static double handMultiplier = 0.5D;
    private static double debugMultiplier = 1D;

    private static void renderCompassNeedles(ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn) {

        // save current ogl state for later
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

        // switch off ogl stuff that breaks our rendering needs
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // back-face culling can save some (tiny amount of) performance in third person perspective
        // GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);

        // save modelview matrix for later restoration
        GL11.glPushMatrix();

        if (transformType == GUI) {
            // translate directly above the normal compass render and center a bit
            GL11.glTranslatef(0.025f, 0.025f, 1.001f);
        } else {
            // apply finished vanilla matrix to move with the compass
            Vector4f pos = new Vector4f(0, 0, 0, 1.0F);
            Matrix4f matrix = matrixStackIn.getLast().getMatrix();
            pos.transform(matrix);
            GL11.glTranslatef(pos.getX(), pos.getY(), pos.getZ());
            // then try to fix the needle positions, still broken
            // needs rotation of some kind aswell because the compass is held at a stronger angle
            if (transformType == FIRST_PERSON_LEFT_HAND) {
                GL11.glTranslatef(lefthandX, lefthandY, lefthandZ);
            } else {
                GL11.glTranslatef(righthandX, righthandY, righthandZ);
            }
        }

        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();

        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet()) {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            GL11.glTranslatef(0, 0, 0.001f); // elevating the needles outside of drawNeedle() is better blackboxing
            drawNeedle(Tessellator.getInstance(), transformType, (float) configInts[0] / 255f, (float) configInts[1] / 255f, (float) configInts[2] / 255f, computeNeedleHeading(entryTarget.getValue()));
        }

        if (css.getFeatureNeedle() != null && FinderCompassLogic.hasFeature) {
            GL11.glTranslatef(0, 0, 0.001f); // elevating the needles outside of drawNeedle() is better blackboxing
            drawNeedle(Tessellator.getInstance(), transformType, strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.featureCoords));
        }

        // restore modelview matrix
        GL11.glPopMatrix();

        // restore ogl state
        GL11.glPopAttrib();
    }

    private static void drawNeedle(Tessellator t, ItemCameraTransforms.TransformType transformType, float r, float g, float b, float angle) {
        // save modelview matrix for later restoration
        GL11.glPushMatrix();
        // make the needle cover roughly the same elliptical shape as the default pixelled one
        GL11.glScalef(1.7875f, 0.8125f, 1f);

        GL11.glRotatef(-angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation

        // make the vertex much bigger for debugging - where did the damn thing go
        double sizeMultiplier = debugMultiplier;

        if (transformType != GUI) {
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
        if (mc.world != null && mc.player != null) {
            double xdiff = mc.player.getPosX() - (coords.getX() + 0.5D);
            double zdiff = mc.player.getPosZ() - (coords.getZ() + 0.5D);
            angleRadian = (mc.player.rotationYaw - 90.0F) * Math.PI / 180.0D - Math.atan2(zdiff, xdiff);
        }

        return (float) -(angleRadian * 180f / Math.PI);
    }
}
