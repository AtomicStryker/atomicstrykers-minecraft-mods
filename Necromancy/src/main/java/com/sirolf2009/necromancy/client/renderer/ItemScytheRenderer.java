package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.client.model.ModelScythe;
import com.sirolf2009.necromancy.client.model.ModelScytheSpecial;
import com.sirolf2009.necromancy.lib.ConfigurationNecromancy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;


public class ItemScytheRenderer implements IItemRenderer
{

    private final ModelScythe model = new ModelScythe();
    private final ModelScytheSpecial modelSpecial = new ModelScytheSpecial();
    boolean isSpecial = false;

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
        if (data.length > 1)
        {
            if (data[1] instanceof EntityPlayer)
            {
                if (Necromancy.specialFolk.contains(((EntityPlayer) data[1]).getCommandSenderName()) && ConfigurationNecromancy.RenderSpecialScythe)
                {
                    isSpecial = true;
                }
            }
        }
        switch (type)
        {
        case ENTITY:
            renderScythe(0F, 1F, 0F, 1F, 1F, 180F, 1);
            break;
        case EQUIPPED:
            if (isSpecial)
            {
                renderSpecialScythe(.8F, 1F, .8F, 0F, -40F, 0F, 1.2F);
            }
            else
            {
                renderScythe(0.8F, 4F, 0.8F, 0F, -140F, 180F, 3.8F);
            }
            break;
        case EQUIPPED_FIRST_PERSON:
            if (isSpecial)
            {
                renderSpecialScythe(0F, 1F, 0F, 20F, -140F, 20F, .6F);
            }
            else
            {
                renderScythe(0F, 2.2F, 0F, -10F, 140F, 180F, 2);
            }
            break;
        case INVENTORY:
            renderScythe(0F, 0.4F, 0F, 150F, 60F, 0F, 0.8F);
            break;
        default:
            break;
        }
        isSpecial = false;

    }

    private void renderScythe(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float scale)
    {
        Necromancy.proxy.bindTexture(ReferenceNecromancy.TEXTURES_MODELS_SCYTHE);
        GL11.glPushMatrix(); // start
        GL11.glTranslatef(posX, posY, posZ); // size
        GL11.glRotatef(rotX, 1, 0, 0);
        GL11.glRotatef(rotY, 0, 1, 0);
        GL11.glRotatef(rotZ, 0, 0, 1);
        GL11.glScalef(scale, scale, scale);
        model.render();
        GL11.glPopMatrix(); // end
    }

    private void renderSpecialScythe(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float scale)
    {
        GL11.glPushMatrix(); // start
        GL11.glTranslatef(posX, posY, posZ); // size
        GL11.glRotatef(rotX, 1, 0, 0);
        GL11.glRotatef(rotY, 0, 1, 0);
        GL11.glRotatef(rotZ, 0, 0, 1);
        GL11.glScalef(scale, scale, scale);
        modelSpecial.render();
        GL11.glPopMatrix(); // end
    }

}
