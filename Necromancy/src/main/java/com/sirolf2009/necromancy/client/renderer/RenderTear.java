package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.sirolf2009.necromancy.item.ItemIsaacsHead;

public class RenderTear extends Render
{

    @Override
    public void doRender(Entity par1Entity, double x, double y, double z, float dontCare, float unUsed)
    {
        IIcon iicon = getTearIcon();

        if (iicon != null)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)x, (float)y, (float)z);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            bindEntityTexture(par1Entity);
            Tessellator tessellator = Tessellator.instance;

            double minU = iicon.getMinU();
            double maxU = iicon.getMaxU();
            double minV = iicon.getMinV();
            double maxV = iicon.getMaxV();
            GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            tessellator.addVertexWithUV(-0.5D, -0.25D, 0.0D, minU, maxV);
            tessellator.addVertexWithUV(0.5D, -0.25D, 0.0D, maxU, maxV);
            tessellator.addVertexWithUV(0.5D, 0.75D, 0.0D, maxU, minV);
            tessellator.addVertexWithUV(-0.5D, 0.75D, 0.0D, minU, minV);
            tessellator.draw();
            
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
        }
    }
    
    protected IIcon getTearIcon()
    {
        return ItemIsaacsHead.tearIcon;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return TextureMap.locationItemsTexture;
    }
}
