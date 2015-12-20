package atomicstryker.findercompass.client;

import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class CompassRenderHook
{
    
    private static final float[] strongholdNeedlecolor = { 0.4f, 0f, 0.6f };
    private static Minecraft mc;

    public static void renderItemHook(ItemStack stack)
    {
        if (stack.getItem() == Items.compass || stack.getItem() == FinderCompassMod.instance.compass)
        {
            if (mc == null)
            {
                mc = Minecraft.getMinecraft();
            }
            renderCompassNeedles();
        }
    }
    
    private static void renderCompassNeedles()
    {
        // save current ogl state for later
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        
        // switch off ogl stuff that breaks our rendering needs
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        
        GL11.glTranslatef(0.54f, 0.52f, 0.522f); // translate to the middle of the icon and slightly towards the player
        // these values were found by painful experimentation
        
        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();
        
        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet())
        {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            drawNeedle(Tessellator.getInstance(), (float)configInts[0]/255f, (float)configInts[1]/255f, (float)configInts[2]/255f, computeNeedleHeading(entryTarget.getValue()));
        }
        
        if (css.isStrongholdNeedleEnabled() && FinderCompassLogic.hasStronghold)
        {
            drawNeedle(Tessellator.getInstance(), strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.strongholdCoords));
        }
        
        // restore ogl state
        GL11.glPopAttrib();
        
        // translate back
        GL11.glTranslatef(-0.54f, -0.52f, -0.522f);
    }
    
    private static void drawNeedle(Tessellator t, float r, float g, float b, float angle)
    {
        GL11.glRotatef(-angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation

        // lets use mc code
        t.getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        t.getWorldRenderer().putColorRGB_F(r, g, b, 1); // TODO try values 1-5 for the last arg if problems

        // TODO test this
        t.getWorldRenderer().pos(-0.03f, -0.04f, 0.0f); // lower left
        t.getWorldRenderer().pos(0.03f, -0.04f, 0.0f); // lower right
        t.getWorldRenderer().pos(0.03f, 0.2f, 0.0f); // upper right
        t.getWorldRenderer().pos(-0.03f, 0.2f, 0.0f); // upper left

        t.draw();

/*        // alternative native ogl code
        GL11.glBegin(GL11.GL_QUADS); // set ogl mode, need quads
        GL11.glColor4f(r, g, b, 0.75F); // set color

        // now draw each glorious needle as single quad
        GL11.glVertex3d(-0.03D, -0.04D, 0.0D); // lower left
        GL11.glVertex3d(0.03D, -0.04D, 0.0D); // lower right
        GL11.glVertex3d(0.03D, 0.2D, 0.0D); // upper right
        GL11.glVertex3d(-0.03D, 0.2D, 0.0D); // upper left
        
        GL11.glEnd(); // let ogl draw it
        
        GL11.glRotatef(angle, 0, 0, 1f); // revert rotation for next needle
        GL11.glTranslatef(0, 0, -0.01f); // translate slightly up
*/    }
    
    private static float computeNeedleHeading(BlockPos coords)
    {        
        double angleRadian = 0.0D;
        if (mc.theWorld != null && mc.thePlayer != null)
        {
            double xdiff = mc.thePlayer.posX - (coords.getX() + 0.5D);
            double zdiff = mc.thePlayer.posZ - (coords.getZ() + 0.5D);
            angleRadian = (mc.thePlayer.rotationYaw - 90.0F) * Math.PI / 180.0D - Math.atan2(zdiff, xdiff);
            if (!mc.theWorld.provider.isSurfaceWorld())
            {
                angleRadian = Math.random() * Math.PI * 2.0D;
            }
        }

        return (float) -(angleRadian * 180f / Math.PI);
    }
}
