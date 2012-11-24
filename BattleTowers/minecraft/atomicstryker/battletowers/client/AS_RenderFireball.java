package atomicstryker.battletowers.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import atomicstryker.battletowers.common.AS_EntityGolemFireball;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityFireball;
import net.minecraft.src.Render;
import net.minecraft.src.Tessellator;

public class AS_RenderFireball extends Render
{

    private float fireBallSize;

    public AS_RenderFireball(float par1)
    {
        this.fireBallSize = par1;
    }

    public void doRenderFireball(AS_EntityGolemFireball fireBallEnt, double posX, double posY, double posZ, float par8, float par9)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX, (float)posY, (float)posZ);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        float var10 = this.fireBallSize;
        GL11.glScalef(var10 / 1.0F, var10 / 1.0F, var10 / 1.0F);
        byte var11 = 46;
        this.loadTexture("/gui/items.png");
        Tessellator var12 = Tessellator.instance;
        float var13 = (float)(var11 % 16 * 16 + 0) / 256.0F;
        float var14 = (float)(var11 % 16 * 16 + 16) / 256.0F;
        float var15 = (float)(var11 / 16 * 16 + 0) / 256.0F;
        float var16 = (float)(var11 / 16 * 16 + 16) / 256.0F;
        float var17 = 1.0F;
        float var18 = 0.5F;
        float var19 = 0.25F;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        var12.startDrawingQuads();
        var12.setNormal(0.0F, 1.0F, 0.0F);
        var12.addVertexWithUV((double)(0.0F - var18), (double)(0.0F - var19), 0.0D, (double)var13, (double)var16);
        var12.addVertexWithUV((double)(var17 - var18), (double)(0.0F - var19), 0.0D, (double)var14, (double)var16);
        var12.addVertexWithUV((double)(var17 - var18), (double)(1.0F - var19), 0.0D, (double)var14, (double)var15);
        var12.addVertexWithUV((double)(0.0F - var18), (double)(1.0F - var19), 0.0D, (double)var13, (double)var15);
        var12.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderFireball((AS_EntityGolemFireball)par1Entity, par2, par4, par6, par8, par9);
    }

}
