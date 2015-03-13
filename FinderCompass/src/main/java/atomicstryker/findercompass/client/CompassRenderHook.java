package atomicstryker.findercompass.client;

import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;

public class CompassRenderHook
{
    
    private final float[] strongholdNeedlecolor = { 0.4f, 0f, 0.6f };
    private final Minecraft mc;
    
    public CompassRenderHook(Minecraft m)
    {
        mc = m;
    }
    
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public void preItemRender(RenderHandEvent event)
    {
        if (mc.thePlayer != null && mc.thePlayer.getCurrentEquippedItem() != null)
        {
            Item currentItem = mc.thePlayer.getCurrentEquippedItem().getItem();
            if (currentItem == FinderCompassMod.instance.compass || currentItem == Items.compass)
            {
                float partialTicks = event.partialTicks;
                GlStateManager.clear(256);
                
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                GlStateManager.pushMatrix();
                
                if (mc.gameSettings.thirdPersonView == 0 && !mc.thePlayer.isPlayerSleeping() && !this.mc.gameSettings.hideGUI && !this.mc.playerController.isSpectator())
                {
                    mc.entityRenderer.enableLightmap();
                    
                    //float f1 = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
                    EntityPlayerSP entityplayersp = this.mc.thePlayer;
                    float f2 = entityplayersp.getSwingProgress(partialTicks);
                    float f3 = entityplayersp.prevRotationPitch + (entityplayersp.rotationPitch - entityplayersp.prevRotationPitch) * partialTicks;
                    float f4 = entityplayersp.prevRotationYaw + (entityplayersp.rotationYaw - entityplayersp.prevRotationYaw) * partialTicks;
                    
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(f4, 0.0F, 1.0F, 0.0F);
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.popMatrix();
                    
                    int i = this.mc.theWorld.getCombinedLight(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), 0);
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(i & 65535), (float)(i >> 16));
                    
                    float ff1 = mc.thePlayer.prevRenderArmPitch + (mc.thePlayer.renderArmPitch - mc.thePlayer.prevRenderArmPitch) * partialTicks;
                    float ff2 = mc.thePlayer.prevRenderArmYaw + (mc.thePlayer.renderArmYaw - mc.thePlayer.prevRenderArmYaw) * partialTicks;
                    GlStateManager.rotate((mc.thePlayer.rotationPitch - ff1) * 0.1F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate((mc.thePlayer.rotationYaw - ff2) * 0.1F, 0.0F, 1.0F, 0.0F);
                    
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.pushMatrix();
                    
                    GlStateManager.translate(-0.4F * MathHelper.sin(MathHelper.sqrt_float(partialTicks) * (float)Math.PI), 0.2F * MathHelper.sin(MathHelper.sqrt_float(partialTicks) * (float)Math.PI * 2.0F), -0.2F * MathHelper.sin(partialTicks * (float)Math.PI));
                    
                    GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
                    GlStateManager.translate(0.0F, -0.6F, 0.0F);
                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                    float f2q = MathHelper.sin(f2 * f2 * (float)Math.PI);
                    float f3q = MathHelper.sin(MathHelper.sqrt_float(f2q) * (float)Math.PI);
                    GlStateManager.rotate(f2q * -20.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(f3q * -20.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(f3q * -80.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.scale(0.4F, 0.4F, 0.4F);
                    
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.alphaFunc(516, 0.1F);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.pushMatrix();
                    
                    RenderItem.applyVanillaTransform(mc.getRenderItem().getItemModelMesher().getItemModel(mc.thePlayer.getCurrentEquippedItem()).getItemCameraTransforms().firstPerson);
                    
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5F, 0.5F, 0.5F);
                    GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                    
                    // DRAW QUADS
                    renderCompassNeedles();
                    System.out.println("Drawing compass needles!");
                    
                    GlStateManager.popMatrix();
                    
                    GlStateManager.popMatrix();
                    GlStateManager.disableRescaleNormal();
                    GlStateManager.disableBlend();                    

                    GlStateManager.popMatrix();
                    GlStateManager.disableRescaleNormal();
                    RenderHelper.disableStandardItemLighting();
                    
                    mc.entityRenderer.disableLightmap();
                }
                GlStateManager.popMatrix();
            }
        }
    }
    
    private void renderCompassNeedles()
    {
        // save current ogl state for later
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        
        // switch off ogl stuff that breaks our rendering needs
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        
        GL11.glTranslatef(0.47f, 0.52f, -0.1f); // translate to the middle of the icon and slightly towards the player
        
        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();
        
        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet())
        {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            drawFirstPersonNeedle(Tessellator.getInstance(), (float)configInts[0]/255f, (float)configInts[1]/255f, (float)configInts[2]/255f, computeNeedleHeading(entryTarget.getValue()));
        }
        
        if (css.isStrongholdNeedleEnabled() && FinderCompassLogic.hasStronghold)
        {
            drawFirstPersonNeedle(Tessellator.getInstance(), strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.strongholdCoords));
        }
        
        // restore ogl state
        GL11.glPopAttrib();
    }
    
    private void drawFirstPersonNeedle(Tessellator t, float r, float g, float b, float angle)
    {
        GL11.glRotatef(angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation

        // lets use mc code
        t.getWorldRenderer().startDrawingQuads();
        t.getWorldRenderer().setColorRGBA_F(r, g, b, 0.75f);

        t.getWorldRenderer().addVertex(-0.03D, -0.04D, 0.0D); // lower left
        t.getWorldRenderer().addVertex(0.03D, -0.04D, 0.0D); // lower right
        t.getWorldRenderer().addVertex(0.03D, 0.2D, 0.0D); // upper right
        t.getWorldRenderer().addVertex(-0.03D, 0.2D, 0.0D); // upper left

        t.draw();

        /* alternative native ogl code
        GL11.glBegin(GL11.GL_QUADS); // set ogl mode, need quads
        GL11.glColor4f(r, g, b, 0.75F); // set color

        // now draw each glorious needle as single quad
        GL11.glVertex3d(-0.03D, -0.04D, 0.0D); // lower left
        GL11.glVertex3d(0.03D, -0.04D, 0.0D); // lower right
        GL11.glVertex3d(0.03D, 0.2D, 0.0D); // upper right
        GL11.glVertex3d(-0.03D, 0.2D, 0.0D); // upper left
        
        GL11.glEnd(); // let ogl draw it
        */
        
        GL11.glRotatef(-angle, 0, 0, 1f); // revert rotation for next needle
        GL11.glTranslatef(0, 0, -0.01f); // translate slightly up
    }
    
    private float computeNeedleHeading(BlockPos coords)
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
