package atomicstryker.ropesplus.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import atomicstryker.ropesplus.common.RopesPlusCore;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BlockRenderHandler implements ISimpleBlockRenderingHandler
{    
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        return renderGrapplingHook(renderer, block, x, y, z, world);
    }

    @Override
    public boolean shouldRender3DInInventory()
    {
        return false;
    }

    @Override
    public int getRenderId()
    {
        return RopesPlusCore.proxy.getGrapplingHookRenderId();
    }

    private boolean renderGrapplingHook(RenderBlocks renderblocks, Block block, int i, int j, int k, IBlockAccess iblockaccess)
    {
        int l = block.colorMultiplier(iblockaccess, i, j, k);
        float f = (float)(l >> 16 & 0xff) / 255F;
        float f1 = (float)(l >> 8 & 0xff) / 255F;
        float f2 = (float)(l & 0xff) / 255F;
        return renderGrapplingHook2(renderblocks, block, i, j, k, f, f1, f2, iblockaccess);
    }

    private boolean renderGrapplingHook2(RenderBlocks renderblocks, Block block, int i, int j, int k, float f, float f1, float f2, 
            IBlockAccess iblockaccess)
    {
        Tessellator tessellator = Tessellator.instance;
        boolean flag = false;
        float f3 = 1.0F;
        float f4 = f3 * f;
        float f5 = f3 * f1;
        float f6 = f3 * f2;
        if(block == Block.grass)
        {
            f = f1 = f2 = 1.0F;
        }
        float f7 = block.getMixedBrightnessForBlock(iblockaccess, i, j, k);
        int l = iblockaccess.getBlockMetadata(i, j, k);
        if(block.shouldSideBeRendered(iblockaccess, i, j + 1, k, 1))
        {
            float f8 = block.getMixedBrightnessForBlock(iblockaccess, i, j + 1, k);
            if(block.getBlockBoundsMaxY() != 1.0D && !block.blockMaterial.isLiquid())
            {
                f8 = f7;
            }
            tessellator.setColorOpaque_F(f4 * f8, f5 * f8, f6 * f8);

            tessellator.setBrightness(block.getMixedBrightnessForBlock(iblockaccess, i, j, k));
            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);

            int i1 = block.getBlockTexture(iblockaccess, i, j, k, 1);
            renderGrapplingHook3(block, i, j, k, i1, l);
            flag = true;
        }
        return flag;
    }

    private void renderGrapplingHook3(Block block, double d, double d1, double d2, int i, 
            int blockMeta)
    {
        Tessellator tessellator = Tessellator.instance;
        int k = (i & 0xf) << 4;
        int l = i & 0xf0;
        double d3 = ((double)k + block.getBlockBoundsMinX() * 16D) / 256D; // minX
        double d4 = (((double)k + block.getBlockBoundsMaxX() * 16D) - 0.01D) / 256D; // maxX
        double d5 = ((double)l + block.getBlockBoundsMinZ() * 16D) / 256D; // minZ
        double d6 = (((double)l + block.getBlockBoundsMaxZ() * 16D) - 0.01D) / 256D; // maxZ
        if(block.getBlockBoundsMinX() < 0.0D || block.getBlockBoundsMaxX() > 1.0D)
        {
            d3 = ((float)k + 0.0F) / 256F;
            d4 = ((float)k + 15.99F) / 256F;
        }
        if(block.getBlockBoundsMinZ() < 0.0D || block.getBlockBoundsMaxZ() > 1.0D)
        {
            d5 = ((float)l + 0.0F) / 256F;
            d6 = ((float)l + 15.99F) / 256F;
        }
        double d7 = d + block.getBlockBoundsMinX();
        double d8 = d + block.getBlockBoundsMaxX();
        double d9 = d1 + block.getBlockBoundsMaxY(); // maxY
        double d10 = d2 + block.getBlockBoundsMinZ();
        double d11 = d2 + block.getBlockBoundsMaxZ();

        switch(blockMeta)
        {
        case 1:
            double d13 = d5;
            d5 = d6;
            d6 = d13;
            break;

        case 2: // '\002'
            double d12 = d5;
            d5 = d6;
            d6 = d12;
            d12 = d3;
            d3 = d4;
            d4 = d12;
            break;

        case 5: // '\005'
            double d14 = d3;
            d3 = d4;
            d4 = d14;
            break;
        }
        tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
        tessellator.addVertexWithUV(d8, d9, d10, d4, d5);
        tessellator.addVertexWithUV(d7, d9, d10, d3, d5);
        tessellator.addVertexWithUV(d7, d9, d11, d3, d6);
    }
}
