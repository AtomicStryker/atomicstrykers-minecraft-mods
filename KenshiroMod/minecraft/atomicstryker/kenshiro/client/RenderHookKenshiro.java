package atomicstryker.kenshiro.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.storage.MapData;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class RenderHookKenshiro extends Render
{
	private Minecraft mc;
	private ItemStack itemToRender = null;
	private float equippedProgress = 0.0F;
	private float prevEquippedProgress = 0.0F;
	private MapItemRenderer mapItemRenderer;
    
    @Override
    public void doRender(Entity dontcare0, double dontcare1, double dontcare2, double dontcare3, float dontcare4, float renderTick)
	{
        render(renderTick);
    }
	
    @SuppressWarnings("static-access")
    private void render(float renderTick)
	{
        RenderHelper.disableStandardItemLighting();
        mc = FMLClientHandler.instance().getClient();
        
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        
        //render here
        
        if (KenshiroClient.instance().getKenshiroMode())
        {            
            if(mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping() && !mc.gameSettings.hideGUI)
            {
                mc.entityRenderer.enableLightmap((double)renderTick);
                
               	UpdatePrivateValues();
               	
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
    
    private void UpdatePrivateValues()
    {
        itemToRender = (ItemStack) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 1);
        equippedProgress = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 2);
        prevEquippedProgress = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 3);
        mapItemRenderer = (MapItemRenderer) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, mc.entityRenderer.itemRenderer, 5);
    }
    
    private void renderItemInFirstPerson(float var1, float override)
    {
	      float var2 = this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * var1;
	      EntityPlayerSP var3 = this.mc.thePlayer;
	      float var4 = var3.prevRotationPitch + (var3.rotationPitch - var3.prevRotationPitch) * var1;
	      GL11.glPushMatrix();
	      GL11.glRotatef(var4, 1.0F, 0.0F, 0.0F);
	      GL11.glRotatef(var3.prevRotationYaw + (var3.rotationYaw - var3.prevRotationYaw) * var1, 0.0F, 1.0F, 0.0F);
	      RenderHelper.enableStandardItemLighting();
	      GL11.glPopMatrix();
	      float var6;
	      float var7;
	      if(var3 instanceof EntityPlayerSP) {
	         EntityPlayerSP var5 = (EntityPlayerSP)var3;
	         var6 = var5.prevRenderArmPitch + (var5.renderArmPitch - var5.prevRenderArmPitch) * var1;
	         var7 = var5.prevRenderArmYaw + (var5.renderArmYaw - var5.prevRenderArmYaw) * var1;
	         GL11.glRotatef((var3.rotationPitch - var6) * 0.1F, 1.0F, 0.0F, 0.0F);
	         GL11.glRotatef((var3.rotationYaw - var7) * 0.1F, 0.0F, 1.0F, 0.0F);
	      }

	      ItemStack var14 = this.itemToRender;
	      var6 = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(var3.posX), MathHelper.floor_double(var3.posY), MathHelper.floor_double(var3.posZ));
	      var6 = 1.0F;
	      int var15 = this.mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(var3.posX), MathHelper.floor_double(var3.posY), MathHelper.floor_double(var3.posZ), 0);
	      int var8 = var15 % 65536;
	      int var9 = var15 / 65536;
	      GL13.glMultiTexCoord2f('\u84c1', (float)var8 / 1.0F, (float)var9 / 1.0F);
	      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	      float var10;
	      float var17;
	      float var18;
	      if(var14 != null) {
	         var15 = Item.itemsList[var14.itemID].getColorFromItemStack(var14, 0);
	         var17 = (float)(var15 >> 16 & 255) / 255.0F;
	         var18 = (float)(var15 >> 8 & 255) / 255.0F;
	         var10 = (float)(var15 & 255) / 255.0F;
	         GL11.glColor4f(var6 * var17, var6 * var18, var6 * var10, 1.0F);
	      } else {
	         GL11.glColor4f(var6, var6, var6, 1.0F);
	      }

	      float var11;
	      float var13;
	      if(var14 != null && var14.itemID == Item.map.shiftedIndex) {
	         GL11.glPushMatrix();
	         var7 = 0.8F;
	         var17 = override;
	         var18 = MathHelper.sin(var17 * 3.1415927F);
	         var10 = MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F);
	         GL11.glTranslatef(-var10 * 0.4F, MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F * 2.0F) * 0.2F, -var18 * 0.2F);
	         var17 = 1.0F - var4 / 45.0F + 0.1F;
	         if(var17 < 0.0F) {
	            var17 = 0.0F;
	         }

	         if(var17 > 1.0F) {
	            var17 = 1.0F;
	         }

	         var17 = -MathHelper.cos(var17 * 3.1415927F) * 0.5F + 0.5F;
	         GL11.glTranslatef(0.0F, 0.0F * var7 - (1.0F - var2) * 1.2F - var17 * 0.5F + 0.04F, -0.9F * var7);
	         GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glRotatef(var17 * -85.0F, 0.0F, 0.0F, 1.0F);
	         GL11.glEnable('\u803a');
	         GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, this.mc.renderEngine.getTextureForDownloadableImage(this.mc.thePlayer.skinUrl, this.mc.thePlayer.getTexture()));

	         for(var9 = 0; var9 < 2; ++var9) {
	            int var26 = var9 * 2 - 1;
	            GL11.glPushMatrix();
	            GL11.glTranslatef(-0.0F, -0.6F, 1.1F * (float)var26);
	            GL11.glRotatef((float)(-45 * var26), 1.0F, 0.0F, 0.0F);
	            GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
	            GL11.glRotatef(59.0F, 0.0F, 0.0F, 1.0F);
	            GL11.glRotatef((float)(-65 * var26), 0.0F, 1.0F, 0.0F);
	            Render var21 = RenderManager.instance.getEntityRenderObject(this.mc.thePlayer);
	            RenderPlayer var24 = (RenderPlayer)var21;
	            var13 = 1.0F;
	            GL11.glScalef(var13, var13, var13);
	            var24.func_82441_a(var3); // drawFirstPersonHand()
	            GL11.glPopMatrix();
	         }

	         var18 = override;
	         var10 = MathHelper.sin(var18 * var18 * 3.1415927F);
	         var11 = MathHelper.sin(MathHelper.sqrt_float(var18) * 3.1415927F);
	         GL11.glRotatef(-var10 * 20.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glRotatef(-var11 * 20.0F, 0.0F, 0.0F, 1.0F);
	         GL11.glRotatef(-var11 * 80.0F, 1.0F, 0.0F, 0.0F);
	         var18 = 0.38F;
	         GL11.glScalef(var18, var18, var18);
	         GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	         GL11.glTranslatef(-1.0F, -1.0F, 0.0F);
	         var10 = 0.015625F;
	         GL11.glScalef(var10, var10, var10);
	         this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture("/misc/mapbg.png"));
	         Tessellator var27 = Tessellator.instance;
	         GL11.glNormal3f(0.0F, 0.0F, -1.0F);
	         var27.startDrawingQuads();
	         byte var25 = 7;
	         var27.addVertexWithUV((double)(0 - var25), (double)(128 + var25), 0.0D, 0.0D, 1.0D);
	         var27.addVertexWithUV((double)(128 + var25), (double)(128 + var25), 0.0D, 1.0D, 1.0D);
	         var27.addVertexWithUV((double)(128 + var25), (double)(0 - var25), 0.0D, 1.0D, 0.0D);
	         var27.addVertexWithUV((double)(0 - var25), (double)(0 - var25), 0.0D, 0.0D, 0.0D);
	         var27.draw();
	         MapData var23 = Item.map.getMapData(var14, this.mc.theWorld);
	         this.mapItemRenderer.renderMap(this.mc.thePlayer, this.mc.renderEngine, var23);
	         GL11.glPopMatrix();
	      } else if(var14 != null) {
	         GL11.glPushMatrix();
	         var7 = 0.8F;
	         float var12;
	         if(var3.getItemInUseCount() > 0) {
	            EnumAction var16 = var14.getItemUseAction();
	            if(var16 == EnumAction.eat) {
	               var18 = (float)var3.getItemInUseCount() - var1 + 1.0F;
	               var10 = 1.0F - var18 / (float)var14.getMaxItemUseDuration();
	               var12 = 1.0F - var10;
	               var12 = var12 * var12 * var12;
	               var12 = var12 * var12 * var12;
	               var12 = var12 * var12 * var12;
	               var13 = 1.0F - var12;
	               GL11.glTranslatef(0.0F, MathHelper.abs(MathHelper.cos(var18 / 4.0F * 3.1415927F) * 0.1F) * (float)((double)var10 > 0.2D?1:0), 0.0F);
	               GL11.glTranslatef(var13 * 0.6F, -var13 * 0.5F, 0.0F);
	               GL11.glRotatef(var13 * 90.0F, 0.0F, 1.0F, 0.0F);
	               GL11.glRotatef(var13 * 10.0F, 1.0F, 0.0F, 0.0F);
	               GL11.glRotatef(var13 * 30.0F, 0.0F, 0.0F, 1.0F);
	            }
	         } else {
	            var17 = override;
	            var18 = MathHelper.sin(var17 * 3.1415927F);
	            var10 = MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F);
	            GL11.glTranslatef(-var10 * 0.4F, MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F * 2.0F) * 0.2F, -var18 * 0.2F);
	         }

	         GL11.glTranslatef(0.7F * var7, -0.65F * var7 - (1.0F - var2) * 0.6F, -0.9F * var7);
	         GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glEnable('\u803a');
	         var17 = override;
	         var18 = MathHelper.sin(var17 * var17 * 3.1415927F);
	         var10 = MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F);
	         GL11.glRotatef(-var18 * 20.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glRotatef(-var10 * 20.0F, 0.0F, 0.0F, 1.0F);
	         GL11.glRotatef(-var10 * 80.0F, 1.0F, 0.0F, 0.0F);
	         var17 = 0.4F;
	         GL11.glScalef(var17, var17, var17);
	         if(var3.getItemInUseCount() > 0) {
	            EnumAction var20 = var14.getItemUseAction();
	            if(var20 == EnumAction.block) {
	               GL11.glTranslatef(-0.5F, 0.2F, 0.0F);
	               GL11.glRotatef(30.0F, 0.0F, 1.0F, 0.0F);
	               GL11.glRotatef(-80.0F, 1.0F, 0.0F, 0.0F);
	               GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
	            } else if(var20 == EnumAction.bow) {
	               GL11.glRotatef(-18.0F, 0.0F, 0.0F, 1.0F);
	               GL11.glRotatef(-12.0F, 0.0F, 1.0F, 0.0F);
	               GL11.glRotatef(-8.0F, 1.0F, 0.0F, 0.0F);
	               GL11.glTranslatef(-0.9F, 0.2F, 0.0F);
	               var10 = (float)var14.getMaxItemUseDuration() - ((float)var3.getItemInUseCount() - var1 + 1.0F);
	               var11 = var10 / 20.0F;
	               var11 = (var11 * var11 + var11 * 2.0F) / 3.0F;
	               if(var11 > 1.0F) {
	                  var11 = 1.0F;
	               }

	               if(var11 > 0.1F) {
	                  GL11.glTranslatef(0.0F, MathHelper.sin((var10 - 0.1F) * 1.3F) * 0.01F * (var11 - 0.1F), 0.0F);
	               }

	               GL11.glTranslatef(0.0F, 0.0F, var11 * 0.1F);
	               GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
	               GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
	               GL11.glTranslatef(0.0F, 0.5F, 0.0F);
	               var12 = 1.0F + var11 * 0.2F;
	               GL11.glScalef(1.0F, 1.0F, var12);
	               GL11.glTranslatef(0.0F, -0.5F, 0.0F);
	               GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
	               GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
	            }
	         }

	         if(var14.getItem().shouldRotateAroundWhenRendering()) {
	            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
	         }

	         mc.entityRenderer.itemRenderer.renderItem(var3, var14, 0);
	         GL11.glPopMatrix();
	      } else {
	         GL11.glPushMatrix();
	         var7 = 0.8F;
	         var17 = override;
	         var18 = MathHelper.sin(var17 * 3.1415927F);
	         var10 = MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F);
	         GL11.glTranslatef(-var10 * 0.3F, MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F * 2.0F) * 0.4F, -var18 * 0.4F);
	         GL11.glTranslatef(0.8F * var7, -0.75F * var7 - (1.0F - var2) * 0.6F, -0.9F * var7);
	         GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glEnable('\u803a');
	         var17 = override;
	         var18 = MathHelper.sin(var17 * var17 * 3.1415927F);
	         var10 = MathHelper.sin(MathHelper.sqrt_float(var17) * 3.1415927F);
	         GL11.glRotatef(var10 * 70.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glRotatef(-var18 * 20.0F, 0.0F, 0.0F, 1.0F);
	         GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, this.mc.renderEngine.getTextureForDownloadableImage(this.mc.thePlayer.skinUrl, this.mc.thePlayer.getTexture()));
	         GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
	         GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
	         GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
	         GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
	         GL11.glScalef(1.0F, 1.0F, 1.0F);
	         GL11.glTranslatef(5.6F, 0.0F, 0.0F);
	         Render var19 = RenderManager.instance.getEntityRenderObject(this.mc.thePlayer);
	         RenderPlayer var22 = (RenderPlayer)var19;
	         var10 = 1.0F;
	         GL11.glScalef(var10, var10, var10);
	         var22.func_82441_a(var3); //drawFirstPersonHand()
	         GL11.glPopMatrix();
	      }

	      GL11.glDisable('\u803a');
	      RenderHelper.disableStandardItemLighting();
	   }
}
