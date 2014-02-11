package com.sirolf2009.necromancy.client.renderer.tileentity;

import java.util.Random;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.client.model.ModelAltar;
import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;


public class TileEntityAltarRenderer extends TileEntitySpecialRenderer implements IItemRenderer
{

    private final RenderBlocks renderBlocks;
    private final ModelBook modelBook;
    private final ResourceLocation terrain;
    private final ResourceLocation book;
    
    private final ModelAltar model;
    private final Random rand;

    public TileEntityAltarRenderer()
    {
        model = new ModelAltar();
        renderBlocks = new RenderBlocks();
        modelBook = new ModelBook();
        terrain = TextureMap.locationBlocksTexture;
        book = new ResourceLocation("textures/entity/enchanting_table_book.png");
        rand = new Random();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
    {
        if (tileentity instanceof TileEntityAltar)
        {
            renderAltar((TileEntityAltar) tileentity, d, d1, d2, f);
            TileEntityAltar altar = (TileEntityAltar) tileentity;

            EntityMinion entity = altar.getMinion();

            if (altar.hasContainerChanged())
            {
                entity = (EntityMinion) altar.getPreviewEntity();
                entity.getModel().updateModel(entity, true);
            }

            int i = 0;
            if (altar.getWorldObj() != null)
            {
                i = altar.getBlockMetadata();
            }
            if (entity != null)
            {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) d + 1.5F, (float) d1 + 1.0F, (float) d2 + 0.5F);
                switch (i)
                {
                case 2: // '\0'
                    GL11.glTranslatef(-1F, 0.2F, -2.2F);
                    GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                    GL11.glTranslatef(0.0F, -0.4F, 0.0F);
                    break;

                case 3: // '\001'
                    GL11.glTranslatef(1.2F, 0.2F, 0.0F);
                    GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                    GL11.glTranslatef(0.0F, -0.4F, 0.0F);
                    break;

                case 1: // '\003'
                    GL11.glTranslatef(-3.2F, 0.2F, 0.0F);
                    GL11.glRotatef(90F, 0.0F, 0.1F, 0.0F);
                    GL11.glRotatef(180F, 0.0F, 0.1F, 0.0F);
                    GL11.glRotatef(-90F, 0.1F, 0.0F, 0.0F);
                    GL11.glTranslatef(0.0F, -0.4F, 0.0F);
                    break;

                case 0: // '\002'
                    GL11.glTranslatef(-1F, 0.2F, 3F);
                    GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                    GL11.glTranslatef(0.0F, 0.4F, 0.0F);
                    break;
                }
                GL11.glScalef(1.0F, 1.0F, 1.0F);
                GL11.glTranslated(0, 0, 0.050000000000000003D * Math.sin(0.001D * System.currentTimeMillis()) + 0.10000000000000001D);
                entity.setWorld(altar.getWorldObj());
                entity.setLocationAndAngles(d, d1, d2, 0.0F, 0.0F);
                RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
                GL11.glPopMatrix();
            }
        }
        else
        {
            System.out.println("Tried to use Altar Renderer to render a Tile Entity that isn't actually an Altar.");
        }
    }

    private void renderAltar(TileEntityAltar entity, double x, double y, double z, float f)
    {
        Necromancy.proxy.bindTexture(ReferenceNecromancy.TEXTURES_MODELS_ALTAR);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y + 2.0F, (float) z + 1.0F);
        GL11.glScalef(1.0F, -1F, -1F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        if (entity.getBlockMetadata() >= 0)
        {
            int i = entity.getBlockMetadata();
            int j = 0;
            if (i == 0)
            {
                j = 90;
            }
            if (i == 1)
            {
                j = 180;
            }
            if (i == 2)
            {
                j = 270;
            }
            if (i == 3)
            {
                j = 0;
            }
            GL11.glRotatef(j, 0.0F, 1.0F, 0.0F);
            model.render();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glPushMatrix();
            GL11.glTranslatef(.3F, -.1F, -0.3F);
            float f1 = 0.5F;
            f1 *= 1.0F;
            GL11.glScalef(-f1, -f1, f1);
            int i1 = 1000000000;
            int j1 = i1 % 65536;
            int k = i1 / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k / 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Necromancy.proxy.bindTexture(terrain);
            renderBlocks.renderBlockAsItem(Blocks.torch, 0, 1.0F);
            Necromancy.proxy.bindTexture(book);
            GL11.glScalef(0.1F, 0.1F, 0.1F);
            GL11.glRotatef(90, 0, 0, 1);
            GL11.glTranslatef(-4F, -8F, 8F);
            GL11.glColor3f(0.9F, 0.9F, 0.9F);
            modelBook.render(null, 1F, 0F, 0F, 1.22F, 0F, 1F);
            if (rand.nextInt(100) == 0)
            {
                randomDisplayTick(ClientProxy.mc.theWorld, entity.xCoord, entity.yCoord + 1, entity.zCoord, rand, i);
            }
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
    }

    /**
     * A randomly called display update to be able to add particles or other
     * items for display also shameless copying
     */
    private void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random, int metadata)
    {
        int l = par1World.getBlockMetadata(par2, par3, par4);
        double d0 = 0;
        double d1 = 0;
        double d2 = 0;
        if (metadata == 0)
        {
            d0 = par2 + .2F;
            d1 = par3 + 0.7F;
            d2 = par4 + .8F;
        }
        if (metadata == 1)
        {
            d0 = par2 + .2F;
            d1 = par3 + 0.7F;
            d2 = par4 + 0.2F;
        }
        if (metadata == 2)
        {
            d0 = par2 + .8F;
            d1 = par3 + 0.7F;
            d2 = par4 + 0.2F;
        }
        if (metadata == 3)
        {
            d0 = par2 + .8F;
            d1 = par3 + 0.7F;
            d2 = par4 + 0.8F;
        }
        double d3 = 0.5D;
        double d4 = 0.27000001072883606D;

        if (l == 1)
        {
            par1World.spawnParticle("smoke", d0 - d4, d1 + d3, d2, 0.0D, 0.0D, 0.0D);
            par1World.spawnParticle("flame", d0 - d4, d1 + d3, d2, 0.0D, 0.0D, 0.0D);
        }
        else if (l == 2)
        {
            par1World.spawnParticle("smoke", d0 + d4, d1 + d3, d2, 0.0D, 0.0D, 0.0D);
            par1World.spawnParticle("flame", d0 + d4, d1 + d3, d2, 0.0D, 0.0D, 0.0D);
        }
        else if (l == 3)
        {
            par1World.spawnParticle("smoke", d0, d1 + d3, d2 - d4, 0.0D, 0.0D, 0.0D);
            par1World.spawnParticle("flame", d0, d1 + d3, d2 - d4, 0.0D, 0.0D, 0.0D);
        }
        else if (l == 4)
        {
            par1World.spawnParticle("smoke", d0, d1 + d3, d2 + d4, 0.0D, 0.0D, 0.0D);
            par1World.spawnParticle("flame", d0, d1 + d3, d2 + d4, 0.0D, 0.0D, 0.0D);
        }
        else
        {
            par1World.spawnParticle("smoke", d0, d1, d2, 0.0D, 0.0D, 0.0D);
            par1World.spawnParticle("flame", d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

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
            renderAltar(1.5F, 1F, 0.5F, 1F, 1F, 180F, 1);
            break;
        case EQUIPPED:
            renderAltar(0.5F, 1F, 0.5F, 1F, 250F, 180F, 1);
            break;
        case EQUIPPED_FIRST_PERSON:
            renderAltar(0.5F, 1F, 0.5F, 1F, 250F, 180F, 1);
            break;
        case INVENTORY:
            renderAltar(-0.5F, 0.5F, 0, 1F, -180F, 180F, 0.55F);
            break;
        default:
            break;
        }

    }

    private void renderAltar(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float scale)
    {
        Necromancy.proxy.bindTexture(ReferenceNecromancy.TEXTURES_MODELS_ALTAR);
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
