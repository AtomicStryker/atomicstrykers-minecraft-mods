package com.sirolf2009.necromancy.client.renderer;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;
import com.sirolf2009.necromancy.tileentity.TileEntityScent;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderScent implements ISimpleBlockRenderingHandler
{

    public static int renderID = RenderingRegistry.getNextAvailableRenderId();
    private Random rand = new Random();

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        TileEntityScent tileEntityScent = (TileEntityScent) world.getTileEntity(x, y, z);
        Tessellator.instance.setColorRGBA(tileEntityScent.getRed(), tileEntityScent.getGreen(), tileEntityScent.getBlue(),
                (int) Math.floor(tileEntityScent.getAlpha() * 0.2));
        tileEntityScent.iconTimer--;
        if (tileEntityScent.iconTimer <= 0)
        {
            tileEntityScent.iconTimer = rand.nextInt(5000) + 5000;
            for (int i = 0; i < 6; i++)
            {
                tileEntityScent.icons[i] = RegistryBlocksNecromancy.scent.getIcon(0, rand.nextInt(4));
            }
        }
        renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        renderer.lockBlockBounds = true;
        renderer.overrideBlockBounds(0, 0, 0, 0, 0, 0);
        renderer.renderFaceXPos(block, x, y, z, tileEntityScent.icons[0]);
        renderer.renderFaceYPos(block, x, y, z, tileEntityScent.icons[1]);
        renderer.renderFaceZPos(block, x, y, z, tileEntityScent.icons[2]);
        renderer.renderFaceXNeg(block, x, y, z, tileEntityScent.icons[3]);
        renderer.renderFaceYNeg(block, x, y, z, tileEntityScent.icons[4]);
        renderer.renderFaceZNeg(block, x, y, z, tileEntityScent.icons[5]);
        renderer.lockBlockBounds = false;
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int i)
    {
        return true;
    }

    @Override
    public int getRenderId()
    {
        return renderID;
    }

}
