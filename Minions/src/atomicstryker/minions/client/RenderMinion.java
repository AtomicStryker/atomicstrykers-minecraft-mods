package atomicstryker.minions.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Minion Render Class, allows displaying of a Minion Description and it's item
 * 
 * 
 * @author AtomicStryker
 */

public class RenderMinion extends RenderLiving
{
	private ModelMinion model;
	private ResourceLocation tex = new ResourceLocation("minions", "textures/model/AS_EntityMinion.png");
	
    public RenderMinion(ModelBase var1, float var2)
    {
		super(var1, var2);
		
		this.model = (ModelMinion) var1;
		this.setRenderPassModel(var1);
		this.setRenderManager(RenderManager.instance);
	}

	protected void preRenderCallback(EntityLiving var1, float var2)
    {
        float var4 = 1.0F;
        GL11.glScalef(var4, var4, var4);
        
        this.model.carryAnimation = (var1.riddenByEntity != null);
        
        if ((var1.getDataWatcher().getWatchableObjectByte(0) & 1 << 1) != 0)
        {
            GL11.glTranslatef(0.0F, 0.3125F, 0.0F);
        }
    }

    protected void renderEquippedItems(EntityLiving var1, float var2)
    {
        ItemStack heldItem = var1.getHeldItem();
        if (heldItem != null && !this.model.carryAnimation)
        {
            GL11.glPushMatrix();
            this.model.bipedRightArm.postRender(0.0625F);
            GL11.glTranslatef(0.0F, 0.1F, 0.0F);
            float scale;
            if (heldItem.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[heldItem.itemID].getRenderType()))
            {
                scale = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                scale *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(scale, -scale, scale);
            }
            else if (Item.itemsList[heldItem.itemID].isFull3D())
            {
                scale = 0.4F;
                GL11.glTranslatef(0.0F, 0.35F, 0.0F);
                GL11.glScalef(scale, -scale, scale);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                scale = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(scale, scale, scale);
                GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
            }

            this.renderManager.itemRenderer.renderItem(var1, heldItem, 0);

            GL11.glPopMatrix();
        }
    }

    protected void passSpecialRender(EntityLiving var1, double var2, double var4, double var6)
    {
        this.renderMinionName((EntityMinion)var1, var2, var4, var6);
    }

    protected void renderMinionName(EntityMinion var1, double var2, double var4, double var6)
    {
        if (Minecraft.isGuiEnabled() && var1 != this.renderManager.livingPlayer)
        {
            float var10 = var1.getDistanceToEntity(this.renderManager.livingPlayer);
            float var11 = 12.0F;
            if (var10 < var11)
            {
                String var12 = var1.getDisplayName();
                if (var12 != null)
                {
                    this.renderLivingLabel(var1, var12, var2, var4 - 0.825D, var6, 64);
                }
            }
        }
    }

    @Override
    protected ResourceLocation func_110775_a(Entity entity)
    {
        return tex;
    }
}
