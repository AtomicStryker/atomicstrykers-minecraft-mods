package atomicstryker.battletowers.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import atomicstryker.battletowers.common.AS_EntityGolemFireball;

public class AS_RenderFireball extends Render
{

    private float fireBallSize;

    public AS_RenderFireball(RenderManager mgr, float par1)
    {
    	super(mgr);
        this.fireBallSize = par1;
    }

    public void doRenderFireball(AS_EntityGolemFireball fireBallEnt, double posX, double posY, double posZ, float par8, float par9)
    {
        GL11.glPushMatrix();
        this.bindEntityTexture(fireBallEnt);
        GL11.glTranslatef((float)posX, (float)posY, (float)posZ);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        float f2 = this.fireBallSize;
        GL11.glScalef(f2 / 1.0F, f2 / 1.0F, f2 / 1.0F);
        TextureAtlasSprite icon = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(Items.fire_charge);
        Tessellator tessellator = Tessellator.getInstance();
        float f3 = icon.getMinU();
        float f4 = icon.getMaxU();
        float f5 = icon.getMinV();
        float f6 = icon.getMaxV();
        float f7 = 1.0F;
        float f8 = 0.5F;
        float f9 = 0.25F;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.getWorldRenderer().startDrawingQuads();
        tessellator.getWorldRenderer().setNormal(0.0F, 1.0F, 0.0F);
        tessellator.getWorldRenderer().addVertexWithUV((double)(0.0F - f8), (double)(0.0F - f9), 0.0D, (double)f3, (double)f6);
        tessellator.getWorldRenderer().addVertexWithUV((double)(f7 - f8), (double)(0.0F - f9), 0.0D, (double)f4, (double)f6);
        tessellator.getWorldRenderer().addVertexWithUV((double)(f7 - f8), (double)(1.0F - f9), 0.0D, (double)f4, (double)f5);
        tessellator.getWorldRenderer().addVertexWithUV((double)(0.0F - f8), (double)(1.0F - f9), 0.0D, (double)f3, (double)f5);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderFireball((AS_EntityGolemFireball)par1Entity, par2, par4, par6, par8, par9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return TextureMap.locationBlocksTexture;
    }
    
}
