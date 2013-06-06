package atomicstryker.battletowers.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import atomicstryker.battletowers.common.AS_EntityGolemFireball;

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
        GL11.glScalef(fireBallSize / 1.0F, fireBallSize / 1.0F, fireBallSize / 1.0F);
        Icon icon = Item.fireballCharge.getIconFromDamage(0);
        this.loadTexture("/gui/items.png");
        Tessellator tessellator = Tessellator.instance;
        float minu = icon.getMinU();
        float maxu = icon.getMaxU();
        float minv = icon.getMinV();
        float maxv = icon.getMaxV();
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5D, -0.25D, 0.0D, (double)minu, (double)maxv);
        tessellator.addVertexWithUV(0.5D, -0.25D, 0.0D, (double)maxu, (double)maxv);
        tessellator.addVertexWithUV(0.5D, 0.75D, 0.0D, (double)maxu, (double)minv);
        tessellator.addVertexWithUV(-0.5D, 0.75D, 0.0D, (double)minu, (double)minv);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderFireball((AS_EntityGolemFireball)par1Entity, par2, par4, par6, par8, par9);
    }

}
