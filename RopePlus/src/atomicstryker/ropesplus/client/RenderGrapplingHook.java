package atomicstryker.ropesplus.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import atomicstryker.ropesplus.common.EntityGrapplingHook;


public class RenderGrapplingHook extends Render
{
    
    private ResourceLocation tex = new ResourceLocation("ropesplus", "textures/items/itemGrapplingHookThrown.png");

    public RenderGrapplingHook()
    {
    }

    public void render(EntityGrapplingHook entitygrapplinghook, double d, double d1, double d2, float f, float f1)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)d, (float)d1, (float)d2);
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        this.func_110777_b(entitygrapplinghook);
        Tessellator tessellator = Tessellator.instance;
        float f2 = 0.0F;
        float f3 = 1.0F;
        float f4 = 0.0F;
        float f5 = 1.0F;
        float f6 = 1.0F;
        float f7 = 0.5F;
        float f8 = 0.5F;
        GL11.glRotatef(180F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(0.0F - f7, 0.0F - f8, 0.0D, f2, f5);
        tessellator.addVertexWithUV(f6 - f7, 0.0F - f8, 0.0D, f3, f5);
        tessellator.addVertexWithUV(f6 - f7, 1.0F - f8, 0.0D, f3, f4);
        tessellator.addVertexWithUV(0.0F - f7, 1.0F - f8, 0.0D, f2, f4);
        tessellator.draw();
        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glPopMatrix();
        if(entitygrapplinghook.owner != null)
        {
            float f9 = ((entitygrapplinghook.owner.prevRotationYaw + (entitygrapplinghook.owner.rotationYaw - entitygrapplinghook.owner.prevRotationYaw) * f1) * 3.141593F) / 180F;
            float f10 = ((entitygrapplinghook.owner.prevRotationPitch + (entitygrapplinghook.owner.rotationPitch - entitygrapplinghook.owner.prevRotationPitch) * f1) * 3.141593F) / 180F;
            double d3 = MathHelper.sin(f9);
            double d4 = MathHelper.cos(f9);
            double d5 = MathHelper.sin(f10);
            double d6 = MathHelper.cos(f10);
            double d7 = (entitygrapplinghook.owner.prevPosX + (entitygrapplinghook.owner.posX - entitygrapplinghook.owner.prevPosX) * (double)f1) - d4 * 0.69999999999999996D - d3 * 0.5D * d6;
            double d8 = (entitygrapplinghook.owner.prevPosY + (entitygrapplinghook.owner.posY - entitygrapplinghook.owner.prevPosY) * (double)f1) - d5 * 0.5D;
            double d9 = ((entitygrapplinghook.owner.prevPosZ + (entitygrapplinghook.owner.posZ - entitygrapplinghook.owner.prevPosZ) * (double)f1) - d3 * 0.69999999999999996D) + d4 * 0.5D * d6;
            if(renderManager.options.thirdPersonView != 0)
            {
                float f11 = ((entitygrapplinghook.owner.prevRenderYawOffset + (entitygrapplinghook.owner.renderYawOffset - entitygrapplinghook.owner.prevRenderYawOffset) * f1) * 3.141593F) / 180F;
                double d11 = MathHelper.sin(f11);
                double d13 = MathHelper.cos(f11);
                d7 = (entitygrapplinghook.owner.prevPosX + (entitygrapplinghook.owner.posX - entitygrapplinghook.owner.prevPosX) * (double)f1) - d13 * 0.34999999999999998D - d11 * 0.84999999999999998D;
                d8 = (entitygrapplinghook.owner.prevPosY + (entitygrapplinghook.owner.posY - entitygrapplinghook.owner.prevPosY) * (double)f1) - 0.45000000000000001D;
                d9 = ((entitygrapplinghook.owner.prevPosZ + (entitygrapplinghook.owner.posZ - entitygrapplinghook.owner.prevPosZ) * (double)f1) - d11 * 0.34999999999999998D) + d13 * 0.84999999999999998D;
            }
            double d10 = entitygrapplinghook.prevPosX + (entitygrapplinghook.posX - entitygrapplinghook.prevPosX) * (double)f1;
            double d12 = entitygrapplinghook.prevPosY + (entitygrapplinghook.posY - entitygrapplinghook.prevPosY) * (double)f1 + 0.25D;
            double d14 = entitygrapplinghook.prevPosZ + (entitygrapplinghook.posZ - entitygrapplinghook.prevPosZ) * (double)f1;
            double d15 = (float)(d7 - d10);
            double d16 = (float)(d8 - d12);
            double d17 = (float)(d9 - d14);
            GL11.glDisable(3553 /*GL_TEXTURE_2D*/);
            GL11.glDisable(2896 /*GL_LIGHTING*/);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);
            int i = 16;
            for(int j = 0; j <= i; j++)
            {
                float f12 = (float)j / (float)i;
                tessellator.addVertex(d + d15 * (double)f12, d1 + d16 * (double)(f12 * f12 + f12) * 0.5D + 0.25D, d2 + d17 * (double)f12);
            }

            tessellator.draw();
            GL11.glEnable(2896 /*GL_LIGHTING*/);
            GL11.glEnable(3553 /*GL_TEXTURE_2D*/);
        }
    }

    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1)
    {
        render((EntityGrapplingHook)entity, d, d1, d2, f, f1);
    }

    @Override
    protected ResourceLocation func_110775_a(Entity entity)
    {
        return tex;
    }
}
