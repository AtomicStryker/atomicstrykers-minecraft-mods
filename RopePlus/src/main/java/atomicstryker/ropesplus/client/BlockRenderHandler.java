package atomicstryker.ropesplus.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
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
    public boolean shouldRender3DInInventory(int modelID)
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
        if(block == Blocks.grass)
        {
            f = f1 = f2 = 1.0F;
        }
        float f7 = block.getMixedBrightnessForBlock(iblockaccess, i, j, k);
        if(block.shouldSideBeRendered(iblockaccess, i, j + 1, k, 1))
        {
            float f8 = block.getMixedBrightnessForBlock(iblockaccess, i, j + 1, k);
            if(block.getBlockBoundsMaxY() != 1.0D && !block.getMaterial().isLiquid())
            {
                f8 = f7;
            }
            tessellator.setColorOpaque_F(f4 * f8, f5 * f8, f6 * f8);

            tessellator.setBrightness(block.getMixedBrightnessForBlock(iblockaccess, i, j, k));
            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            flag = true;
        }
        return flag;
    }
}
