package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.sirolf2009.necromancy.client.model.ModelScytheBone;
import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemScytheBoneRenderer implements IItemRenderer
{

    public ModelScytheBone model = new ModelScytheBone();

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
            renderScythe(0F, 1F, 0F, 1F, 1F, 180F, 1);
            break;
        case EQUIPPED:
            renderScythe(0.8F, 4F, 0.8F, 0F, -140F, 180F, 3.8F);
            break;
        case EQUIPPED_FIRST_PERSON:
            renderScythe(0F, 2.2F, 0F, -10F, 140F, 180F, 2);
            break;
        case INVENTORY:
            renderScythe(0F, 0.4F, 0F, 150F, 60F, 0F, 0.8F);
            break;
        default:
            break;
        }

    }

    private void renderScythe(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float scale)
    {
        ClientProxy.bindTexture(ReferenceNecromancy.TEXTURES_MODELS_SCYTHEBONE);
        GL11.glPushMatrix(); // start
        GL11.glTranslatef(posX, posY, posZ); // size
        GL11.glRotatef(rotX, 1, 0, 0);
        GL11.glRotatef(rotY, 0, 1, 0);
        GL11.glRotatef(rotZ, 0, 0, 1);
        GL11.glScalef(scale, scale, scale);
        model.render();
        GL11.glPopMatrix(); // end
    }

    public void log(Object msg)
    {
        System.out.println(this.getClass() + "	" + msg);
    }

}
