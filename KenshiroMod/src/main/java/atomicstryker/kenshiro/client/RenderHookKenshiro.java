package atomicstryker.kenshiro.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderHookKenshiro
{
	private Minecraft mc;
	private ItemStack itemToRender = null;
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
            if(mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping() && !mc.gameSettings.hideGUI)
            {
                mc.entityRenderer.enableLightmap((double)renderTick);
                
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
        itemToRender = (ItemStack) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 4);
        equippedProgress = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 5);
        prevEquippedProgress = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 6);
    }
    // entityclientplayermp.getSwingProgress(par1) -> float override
    private void renderItemInFirstPerson(float par1, float override)
    {
        float f1 = this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * par1;
        EntityClientPlayerMP entityclientplayermp = this.mc.thePlayer;
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
        ItemStack itemstack = this.itemToRender;
        float f5 = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(entityclientplayermp.posX), MathHelper.floor_double(entityclientplayermp.posY), MathHelper.floor_double(entityclientplayermp.posZ));
        f5 = 1.0F;
        int i = this.mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(entityclientplayermp.posX), MathHelper.floor_double(entityclientplayermp.posY), MathHelper.floor_double(entityclientplayermp.posZ), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f6;
        float f7;
        float f8;

        if (itemstack != null)
        {
            i = itemstack.getItem().getColorFromItemStack(itemstack, 0);
            f7 = (float)(i >> 16 & 255) / 255.0F;
            f8 = (float)(i >> 8 & 255) / 255.0F;
            f6 = (float)(i & 255) / 255.0F;
            GL11.glColor4f(f5 * f7, f5 * f8, f5 * f6, 1.0F);
        }
        else
        {
            GL11.glColor4f(f5, f5, f5, 1.0F);
        }

        float f9;
        float f10;
        float f11;
        float f12;
        Render render;
        RenderPlayer renderplayer;
        
        if (itemstack != null)
        {
            GL11.glPushMatrix();
            f12 = 0.8F;

            if (entityclientplayermp.getItemInUseCount() > 0)
            {
                EnumAction enumaction = itemstack.getItemUseAction();

                if (enumaction == EnumAction.eat || enumaction == EnumAction.drink)
                {
                    f8 = (float)entityclientplayermp.getItemInUseCount() - par1 + 1.0F;
                    f6 = 1.0F - f8 / (float)itemstack.getMaxItemUseDuration();
                    f9 = 1.0F - f6;
                    f9 = f9 * f9 * f9;
                    f9 = f9 * f9 * f9;
                    f9 = f9 * f9 * f9;
                    f10 = 1.0F - f9;
                    GL11.glTranslatef(0.0F, MathHelper.abs(MathHelper.cos(f8 / 4.0F * (float)Math.PI) * 0.1F) * (float)((double)f6 > 0.2D ? 1 : 0), 0.0F);
                    GL11.glTranslatef(f10 * 0.6F, -f10 * 0.5F, 0.0F);
                    GL11.glRotatef(f10 * 90.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(f10 * 10.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(f10 * 30.0F, 0.0F, 0.0F, 1.0F);
                }
            }
            else
            {
                f7 = override;
                f8 = MathHelper.sin(f7 * (float)Math.PI);
                f6 = MathHelper.sin(MathHelper.sqrt_float(f7) * (float)Math.PI);
                GL11.glTranslatef(-f6 * 0.4F, MathHelper.sin(MathHelper.sqrt_float(f7) * (float)Math.PI * 2.0F) * 0.2F, -f8 * 0.2F);
            }

            GL11.glTranslatef(0.7F * f12, -0.65F * f12 - (1.0F - f1) * 0.6F, -0.9F * f12);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            f7 = override;
            f8 = MathHelper.sin(f7 * f7 * (float)Math.PI);
            f6 = MathHelper.sin(MathHelper.sqrt_float(f7) * (float)Math.PI);
            GL11.glRotatef(-f8 * 20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-f6 * 20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-f6 * 80.0F, 1.0F, 0.0F, 0.0F);
            f9 = 0.4F;
            GL11.glScalef(f9, f9, f9);
            float f13;
            float f14;

            if (entityclientplayermp.getItemInUseCount() > 0)
            {
                EnumAction enumaction1 = itemstack.getItemUseAction();

                if (enumaction1 == EnumAction.block)
                {
                    GL11.glTranslatef(-0.5F, 0.2F, 0.0F);
                    GL11.glRotatef(30.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-80.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
                }
                else if (enumaction1 == EnumAction.bow)
                {
                    GL11.glRotatef(-18.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-12.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-8.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glTranslatef(-0.9F, 0.2F, 0.0F);
                    f11 = (float)itemstack.getMaxItemUseDuration() - ((float)entityclientplayermp.getItemInUseCount() - par1 + 1.0F);
                    f13 = f11 / 20.0F;
                    f13 = (f13 * f13 + f13 * 2.0F) / 3.0F;

                    if (f13 > 1.0F)
                    {
                        f13 = 1.0F;
                    }

                    if (f13 > 0.1F)
                    {
                        GL11.glTranslatef(0.0F, MathHelper.sin((f11 - 0.1F) * 1.3F) * 0.01F * (f13 - 0.1F), 0.0F);
                    }

                    GL11.glTranslatef(0.0F, 0.0F, f13 * 0.1F);
                    GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glTranslatef(0.0F, 0.5F, 0.0F);
                    f14 = 1.0F + f13 * 0.2F;
                    GL11.glScalef(1.0F, 1.0F, f14);
                    GL11.glTranslatef(0.0F, -0.5F, 0.0F);
                    GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
                }
            }

            if (itemstack.getItem().shouldRotateAroundWhenRendering())
            {
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }

            if (itemstack.getItem().requiresMultipleRenderPasses())
            {
                mc.entityRenderer.itemRenderer.renderItem(entityclientplayermp, itemstack, 0, ItemRenderType.EQUIPPED_FIRST_PERSON);
                for (int x = 1; x < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); x++)
                {
                    int i1 = itemstack.getItem().getColorFromItemStack(itemstack, x);
                    f11 = (float)(i1 >> 16 & 255) / 255.0F;
                    f13 = (float)(i1 >> 8 & 255) / 255.0F;
                    f14 = (float)(i1 & 255) / 255.0F;
                    GL11.glColor4f(f5 * f11, f5 * f13, f5 * f14, 1.0F);
                    mc.entityRenderer.itemRenderer.renderItem(entityclientplayermp, itemstack, x, ItemRenderType.EQUIPPED_FIRST_PERSON);
                }
            }
            else
            {
                mc.entityRenderer.itemRenderer.renderItem(entityclientplayermp, itemstack, 0, ItemRenderType.EQUIPPED_FIRST_PERSON);
            }

            GL11.glPopMatrix();
        }
        else if (!entityclientplayermp.isInvisible())
        {
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
            render = RenderManager.instance.getEntityRenderObject(this.mc.thePlayer);
            renderplayer = (RenderPlayer)render;
            f11 = 1.0F;
            GL11.glScalef(f11, f11, f11);
            renderplayer.renderFirstPersonArm(this.mc.thePlayer);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
	   }
}
