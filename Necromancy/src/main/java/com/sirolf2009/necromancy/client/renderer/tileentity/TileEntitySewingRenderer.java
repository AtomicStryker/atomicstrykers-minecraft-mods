package com.sirolf2009.necromancy.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.sirolf2009.necromancy.client.model.ModelSewing;
import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntitySewingRenderer extends TileEntitySpecialRenderer implements IItemRenderer
{

    public TileEntitySewingRenderer()
    {
        model = new ModelSewing();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
    {
        if (tileentity instanceof TileEntitySewing)
        {
            renderSewing((TileEntitySewing) tileentity, d, d1, d2, f);
            TileEntitySewing Sewing = (TileEntitySewing) tileentity;
            int i = 0;
            if (Sewing.getWorldObj() != null)
            {
                i = Sewing.getBlockMetadata();
            }
            GL11.glPushMatrix();
            GL11.glTranslatef((float) d + 1.5F, (float) d1 + 1.0F, (float) d2 + 0.5F);
            switch (i)
            {
            case 0: // '\0'
                GL11.glTranslatef(-1F, 0.2F, -2.2F);
                GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, -0.4F, 0.0F);
                break;

            case 1: // '\001'
                GL11.glTranslatef(1.2F, 0.2F, 0.0F);
                GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, -0.4F, 0.0F);
                break;

            case 3: // '\003'
                GL11.glTranslatef(-3.2F, 0.2F, 0.0F);
                GL11.glRotatef(90F, 0.0F, 0.1F, 0.0F);
                GL11.glRotatef(180F, 0.0F, 0.1F, 0.0F);
                GL11.glRotatef(-90F, 0.1F, 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, -0.4F, 0.0F);
                break;

            case 2: // '\002'
                GL11.glTranslatef(-1F, 0.2F, 3F);
                GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, 0.4F, 0.0F);
                break;
            }
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }
    }

    private void renderSewing(TileEntitySewing entity, double x, double y, double z, float f)
    {
        ClientProxy.bindTexture(ReferenceNecromancy.TEXTURES_MODELS_SEWING);
        GL11.glPushMatrix();
        GL11.glEnable(32826);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef((float) x, (float) y + 2.0F, (float) z + 1.0F);
        GL11.glScalef(1.0F, -1F, -1F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        if (entity.getBlockMetadata() >= 0)
        {
            int i = entity.getBlockMetadata();
            int j = 0;
            if (i == 0)
            {
                j = 270;
            }
            if (i == 1)
            {
                j = 0;
            }
            if (i == 2)
            {
                j = 90;
            }
            if (i == 3)
            {
                j = 180;
            }
            GL11.glRotatef(j, 0.0F, 1.0F, 0.0F);
            model.render();
            GL11.glDisable(32826);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public ModelSewing model;

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        switch (type)
        {
        case ENTITY:
            renderSewing(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 180.0F, 2F);
            break;
        case INVENTORY:
            renderSewing(0.0F, 1.8F, 0.0F, 0.0F, 0.0F, 180.0F, 1.5F);
            break;
        case EQUIPPED:
            renderSewing(0.0F, 3.5F, 0.5F, 0.0F, 0.0F, 180.0F, 2.0F);
            break;
        default:
            break;
        }
    }

    private void renderSewing(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float scale)
    {
        ClientProxy.bindTexture(ReferenceNecromancy.TEXTURES_MODELS_SEWING);
        GL11.glPushMatrix(); // start
        GL11.glTranslatef(posX, posY, posZ); // size
        GL11.glRotatef(rotX, 1, 0, 0);
        GL11.glRotatef(rotY, 0, 1, 0);
        GL11.glRotatef(rotZ, 0, 0, 1);
        GL11.glScalef(scale, scale, scale);
        model.render();
        GL11.glPopMatrix(); // end
    }
}
