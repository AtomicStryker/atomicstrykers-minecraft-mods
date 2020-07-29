package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Map.Entry;

public class CompassRenderHook {

    private static final float[] strongholdNeedlecolor = {0.4f, 0f, 0.6f};
    private static Minecraft mc;

    public static void renderItemHook(ItemStack stack) {
        if (stack.getItem() == Items.COMPASS) {
            if (mc == null) {
                mc = Minecraft.getInstance();
            }
            renderCompassNeedles(false);
        }
    }

    public static void renderItemInHandHook(ItemStack stack) {
        if (stack.getItem() == Items.COMPASS) {
            if (mc == null) {
                mc = Minecraft.getInstance();
            }
            // TODO: leave this disabled until implemented
            // renderCompassNeedles(true);
        }
    }

    private static void renderCompassNeedles(boolean inHandTranslations) {
        // save current ogl state for later
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

        // switch off ogl stuff that breaks our rendering needs
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        // back-face culling can save some (tiny amount of) performance in third person perspective
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);

        // save modelview matrix for later restoration
        GL11.glPushMatrix();

        if (inHandTranslations) {
            // TODO: need to mimic translations from FirstPersonRenderer.renderArmFirstPerson to get to where the inhand needles should render
        }

        // translate directly above the normal compass render and center a bit
        GL11.glTranslatef(0.025f, 0.025f, 1.001f);

        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();

        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet()) {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            GL11.glTranslatef(0, 0, 0.001f); // elevating the needles outside of drawNeedle() is better blackboxing
            drawNeedle(Tessellator.getInstance(), (float) configInts[0] / 255f, (float) configInts[1] / 255f, (float) configInts[2] / 255f, computeNeedleHeading(entryTarget.getValue()));
        }

        if (css.getFeatureNeedle() != null && FinderCompassLogic.hasFeature) {
            GL11.glTranslatef(0, 0, 0.001f); // elevating the needles outside of drawNeedle() is better blackboxing
            drawNeedle(Tessellator.getInstance(), strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.featureCoords));
        }

        // restore ogl state
        GL11.glPopAttrib();
        // restore modelview matrix
        GL11.glPopMatrix();
    }

    private static void drawNeedle(Tessellator t, float r, float g, float b, float angle) {
        // save modelview matrix for later restoration
        GL11.glPushMatrix();
        // make the needle cover roughly the same elliptical shape as the default pixelled one
        GL11.glScalef(1.7875f, 0.8125f, 1f);

        GL11.glRotatef(-angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation

        /*
        // this mc code worked until forge 1592, then the fire nation attacked RIP
        t.getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        t.getWorldRenderer().putColorRGB_F(r, g, b, 1); // TODO try values 1-5 for the last arg if problems

        // TODO test this
        t.getWorldRenderer().pos(-0.03f, -0.04f, 0.0f); // lower left
        t.getWorldRenderer().pos(0.03f, -0.04f, 0.0f); // lower right
        t.getWorldRenderer().pos(0.03f, 0.2f, 0.0f); // upper right
        t.getWorldRenderer().pos(-0.03f, 0.2f, 0.0f); // upper left

        t.draw();
        */

        // alternative native ogl code
        GL11.glBegin(GL11.GL_QUADS); // set ogl mode, need quads
        GL11.glColor4f(r, g, b, 0.85F); // set color

        // now draw each glorious needle as single quad
        GL11.glVertex3d(-0.03D, -0.04D, 0.0D); // lower left
        GL11.glVertex3d(0.03D, -0.04D, 0.0D); // lower right
        GL11.glVertex3d(0.03D, 0.2D, 0.0D); // upper right
        GL11.glVertex3d(-0.03D, 0.2D, 0.0D); // upper left

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
