package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

public class RenderTear extends Render
{

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        Necromancy.proxy.bindTexture(ReferenceNecromancy.TEXTURES_ENTITIES_TEAR);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        Tessellator tessellator = Tessellator.instance;
        float f = 0;
        float f1 = 0;
        float f2 = 16;
        float f3 = 16;
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, f, f3);
        tessellator.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, f1, f3);
        tessellator.addVertexWithUV(f4 - f5, f4 - f6, 0.0D, f1, f2);
        tessellator.addVertexWithUV(0.0F - f5, f4 - f6, 0.0D, f, f2);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return null;
    }
}
