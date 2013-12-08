package atomicstryker.findercompass.client;

import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import atomicstryker.findercompass.common.CompassIntPair;

public class CompassCustomRenderer implements IItemRenderer
{
    
    private final float[] strongholdNeedlecolor = { 0.4f, 0f, 0.6f };
    
    private final RenderItem renderItem;
    private final Minecraft mc;
    
    public CompassCustomRenderer()
    {
        renderItem = (RenderItem) RenderManager.instance.entityRenderMap.get(EntityItem.class);
        mc = Minecraft.getMinecraft();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        switch (type)
        {
        case EQUIPPED_FIRST_PERSON:
            return true;
        case INVENTORY:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        // uses renderhelper because our needle needs to match the item bobbing around and stuff
        return type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {        
        switch (type)
        {
            case EQUIPPED_FIRST_PERSON:
            {
                // these translation/rotation values are taken from vanilla code somewhere
                renderCompassFirstPerson((RenderBlocks) data[0], item, -0.4f, 0.8f, 0.9f, 75f);
                break;
            }
            case INVENTORY:
            {
                renderCompassInventory((RenderBlocks) data[0], item);
                break;
            }
            default:
            {
            }
        }
    }
    
    private void renderCompassInventory(RenderBlocks renderBlocks, ItemStack item)
    {
        // vanilla render first
        Icon icon = item.getIconIndex();
        renderItem.renderIcon(0, 0, icon, 16, 16);
        
        // save current ogl state for later
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        
        // switch off ogl stuff that breaks our rendering needs
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        
        GL11.glTranslatef(8f, 8f, 0); // translate to the middle of the icon
        GL11.glRotatef(180, 0, 0, 1f); // flip 180 degrees because it is facing that wrong way without renderhelper
        
        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();
        
        for (Entry<CompassIntPair, ChunkCoordinates> entryTarget : css.getCustomNeedleTargets().entrySet())
        {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            drawInventoryNeedle((float)configInts[0]/255f, (float)configInts[1]/255f, (float)configInts[2]/255f, computeNeedleHeading(entryTarget.getValue()));
        }
        
        if (css.isStrongholdNeedleEnabled() && FinderCompassLogic.hasStronghold)
        {
            drawInventoryNeedle(strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.strongholdCoords));
        }
        
        // restore ogl state
        GL11.glPopAttrib();
    }
    
    private void drawInventoryNeedle(float r, float g, float b, float angle)
    {
        GL11.glRotatef(angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation
        
        GL11.glBegin(GL11.GL_QUADS); // set ogl mode, need quads
        GL11.glColor4f(r, g, b, 0.75F); // set color
        
        // now draw each glorious needle as single quad
        GL11.glVertex3d(-1, -1, 0); // lower left
        GL11.glVertex3d(1, -1, 0); // lower right
        GL11.glVertex3d(1, 4, 0); // upper right
        GL11.glVertex3d(-1, 4, 0); // upper left
        
        GL11.glEnd(); // let ogl draw it all
        
        GL11.glRotatef(-angle, 0, 0, 1f); // revert rotation for next needle
        GL11.glTranslatef(0, 0, 0.01f); // translate slightly up
    }
    
    private void renderCompassFirstPerson(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ, float rotateAngle)
    {
        // translate, rotate and render vanilla like
        GL11.glTranslatef(translateX, translateY, translateZ);
        GL11.glRotatef(rotateAngle, 0, 1.0F, 0);
        Icon icon = item.getIconIndex();
        ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0825F);
        
        // save current ogl state for later
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        
        // switch off ogl stuff that breaks our rendering needs
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        
        GL11.glTranslatef(0.47f, 0.52f, -0.1f); // translate to the middle of the icon and slightly towards the player
        
        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();
        
        for (Entry<CompassIntPair, ChunkCoordinates> entryTarget : css.getCustomNeedleTargets().entrySet())
        {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            drawFirstPersonNeedle((float)configInts[0]/255f, (float)configInts[1]/255f, (float)configInts[2]/255f, computeNeedleHeading(entryTarget.getValue()));
        }
        
        if (css.isStrongholdNeedleEnabled() && FinderCompassLogic.hasStronghold)
        {
            drawFirstPersonNeedle(strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.strongholdCoords));
        }
        
        // restore ogl state
        GL11.glPopAttrib();
    }
    
    private void drawFirstPersonNeedle(float r, float g, float b, float angle)
    {
        GL11.glRotatef(angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation
        
        GL11.glBegin(GL11.GL_QUADS); // set ogl mode, need quads
        GL11.glColor4f(r, g, b, 0.75F); // set color
        
        // now draw each glorious needle as single quad
        GL11.glVertex3d(-0.03D, -0.04D, 0.0D); // lower left
        GL11.glVertex3d(0.03D, -0.04D, 0.0D); // lower right
        GL11.glVertex3d(0.03D, 0.2D, 0.0D); // upper right
        GL11.glVertex3d(-0.03D, 0.2D, 0.0D); // upper left
        
        GL11.glEnd(); // let ogl draw it
        
        GL11.glRotatef(-angle, 0, 0, 1f); // revert rotation for next needle
        GL11.glTranslatef(0, 0, -0.01f); // translate slightly up
    }
    
    private float computeNeedleHeading(ChunkCoordinates coords)
    {
        double angleRadian = 0.0D;
        if (mc.theWorld != null && mc.thePlayer != null)
        {
            double xdiff = mc.thePlayer.posX - (coords.posX + 0.5D);
            double zdiff = mc.thePlayer.posZ - (coords.posZ + 0.5D);
            angleRadian = (mc.thePlayer.rotationYaw - 90.0F) * Math.PI / 180.0D - Math.atan2(zdiff, xdiff);
            if (mc.theWorld.provider.isHellWorld)
            {
                angleRadian = Math.random() * Math.PI * 2.0D;
            }
        }

        return (float) -(angleRadian * 180f / Math.PI);
    }

}
