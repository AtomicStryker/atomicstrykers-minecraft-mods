package atomicstryker.kenshiro.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderHookKenshiro
{
	private Minecraft mc;
	private float equippedProgress = 0.0F;
	private float prevEquippedProgress = 0.0F;
	
    @SubscribeEvent
    public void onDrawSelectionBow(DrawBlockHighlightEvent event)
    {
        float renderTick = event.partialTicks;
        RenderHelper.disableStandardItemLighting();
        mc = FMLClientHandler.instance().getClient();
        
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        
        //render here
        
        if (KenshiroClient.instance().getKenshiroMode())
        {
            if(mc.gameSettings.thirdPersonView == 0 && !((EntityPlayer)mc.getRenderViewEntity()).isPlayerSleeping() && !mc.gameSettings.hideGUI)
            {
                mc.entityRenderer.enableLightmap();
                
               	updatePrivateValues();
               	
                GL11.glPushMatrix();
                GL11.glMatrixMode(5888 /*GL_MODELVIEW0_ARB*/);
                GL11.glLoadIdentity();
                
   	            GL11.glScalef(-1F, 1.0F, 1.0F);
   	            GL11.glFrontFace(2304 /*GL_CW*/);
   	            renderItemInFirstPerson(renderTick, Math.abs(mc.thePlayer.getSwingProgress(renderTick) - 0.5F));
   	            GL11.glFrontFace(2305 /*GL_CCW*/);
   	            
   	            GL11.glPopMatrix();
            }	
        }
        
		//render end
        RenderHelper.enableStandardItemLighting();
    }
    
    private void updatePrivateValues()
    {
        equippedProgress = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 4);
        prevEquippedProgress = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 5);
    }
    // entityclientplayermp.getSwingProgress(par1) -> float override
    private void renderItemInFirstPerson(float par1, float override)
    {
        float f1 = this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * par1;
        EntityPlayerSP entityclientplayermp = this.mc.thePlayer;
        float f2 = entityclientplayermp.prevRotationPitch + (entityclientplayermp.rotationPitch - entityclientplayermp.prevRotationPitch) * par1;
        GL11.glPushMatrix();
        GL11.glRotatef(f2, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(entityclientplayermp.prevRotationYaw + (entityclientplayermp.rotationYaw - entityclientplayermp.prevRotationYaw) * par1, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        EntityPlayerSP entityplayersp = (EntityPlayerSP)entityclientplayermp;
        float f3 = entityplayersp.prevRenderArmPitch + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * par1;
        float f4 = entityplayersp.prevRenderArmYaw + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * par1;
        GL11.glRotatef((entityclientplayermp.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((entityclientplayermp.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);

        BlockPos bp = new BlockPos(MathHelper.floor_double(entityclientplayermp.posX), MathHelper.floor_double(entityclientplayermp.posY), MathHelper.floor_double(entityclientplayermp.posZ));
        float f5 = this.mc.theWorld.getLightBrightness(bp);
        f5 = 1.0F;
        
        int i = mc.theWorld.getLightFor(EnumSkyBlock.SKY, bp);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f6;
        float f7;
        float f8;

        GL11.glColor4f(f5, f5, f5, 1.0F);

        float f11;
        float f12;
        Render<?> render;
        RenderPlayer renderplayer;
        
        GL11.glPushMatrix();
        f12 = 0.8F;
        f7 = override;
        f8 = MathHelper.sin(f7 * (float)Math.PI);
        f6 = MathHelper.sin(MathHelper.sqrt_float(f7) * (float)Math.PI);
        GL11.glTranslatef(-f6 * 0.3F, MathHelper.sin(MathHelper.sqrt_float(f7) * (float)Math.PI * 2.0F) * 0.4F, -f8 * 0.4F);
        GL11.glTranslatef(0.8F * f12, -0.75F * f12 - (1.0F - f1) * 0.6F, -0.9F * f12);
        GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        f7 = override;
        f8 = MathHelper.sin(f7 * f7 * (float)Math.PI);
        f6 = MathHelper.sin(MathHelper.sqrt_float(f7) * (float)Math.PI);
        GL11.glRotatef(f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-f8 * 20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(entityclientplayermp.getLocationSkin());
        GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
        GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(1.0F, 1.0F, 1.0F);
        GL11.glTranslatef(5.6F, 0.0F, 0.0F);
        render = mc.getRenderManager().getEntityRenderObject(this.mc.thePlayer);
        renderplayer = (RenderPlayer)render;
        f11 = 1.0F;
        GL11.glScalef(f11, f11, f11);
        renderplayer.renderRightArm(this.mc.thePlayer);
        GL11.glPopMatrix();

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
	   }
}
