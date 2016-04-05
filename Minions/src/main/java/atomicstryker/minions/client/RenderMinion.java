package atomicstryker.minions.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Minion Render Class, allows displaying of a Minion Description and it's item
 * 
 * 
 * @author AtomicStryker
 */

@SuppressWarnings("deprecation")
public class RenderMinion extends RenderLiving<EntityLiving>
{
	private ModelMinion model;
	private ResourceLocation tex = new ResourceLocation("minions", "textures/model/AS_EntityMinion.png");
	
    public RenderMinion(RenderManager manager, ModelBase var1, float var2)
    {
		super(manager, var1, var2);
		
		this.model = (ModelMinion) var1;
		this.layerRenderers.add(new CustomHeldItem(model.bipedRightArm));
	}

    @Override
	protected void preRenderCallback(EntityLiving var1, float var2)
    {
        float var4 = 1.0F;
        GL11.glScalef(var4, var4, var4);
        
        this.model.carryAnimation = (!var1.getPassengers().isEmpty());

        /*
        if (var1.getFlag(1))
        {
            GL11.glTranslatef(0.0F, 0.3125F, 0.0F);
        }
        */
    }
    
    /*
    @Override
    protected void renderEquippedItems(EntityLivingBase var1, float var2)
    {
        ItemStack heldItem = var1.getHeldItem();
        if (heldItem != null && !this.model.carryAnimation)
        {
            GL11.glPushMatrix();
            this.model.bipedRightArm.postRender(0.0625F);
            GL11.glTranslatef(0.0F, 0.1F, 0.0F);
            float scale;
            if (heldItem.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(heldItem.getItem()).getRenderType()))
            {
                scale = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                scale *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(scale, -scale, scale);
            }
            else if (heldItem.getItem().isFull3D())
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

            Minecraft.getMinecraft().getItemRenderer().renderItem(var1, heldItem, ItemCameraTransforms.TransformType.THIRD_PERSON);

            GL11.glPopMatrix();
        }
    }
    */

    @Override
    protected ResourceLocation getEntityTexture(EntityLiving entity)
    {
        return tex;
    }
    
    private class CustomHeldItem implements LayerRenderer<EntityLiving>
    {
        
        private final ModelRenderer rightArm;
        
        private CustomHeldItem(ModelRenderer ra)
        {
            rightArm = ra;
        }
        
        @Override
        public void doRenderLayer(EntityLiving entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_)
        {
            ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();

            if (itemstack != null)
            {
                GlStateManager.pushMatrix();
                rightArm.postRender(0.0625F);
                GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

                Item item = itemstack.getItem();
                Minecraft minecraft = Minecraft.getMinecraft();

                if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType(null) == EnumBlockRenderType.MODEL)
                {
                    GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
                    GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                    float f8 = 0.375F;
                    GlStateManager.scale(-f8, -f8, f8);
                }

                minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
                GlStateManager.popMatrix();
            }
        }

        @Override
        public boolean shouldCombineTextures()
        {
            return false;
        }
        
    }
}
