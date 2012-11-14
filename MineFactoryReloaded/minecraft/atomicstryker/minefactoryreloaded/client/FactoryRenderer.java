package atomicstryker.minefactoryreloaded.client;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.src.Block;
import net.minecraft.src.BlockRedstoneRepeater;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;

public class FactoryRenderer implements ISimpleBlockRenderingHandler
{
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
    }
    
    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        renderConveyorWorld(renderer, world, x, y, z, block, modelId);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory()
    {
        return false;
    }

    @Override
    public int getRenderId()
    {
        return MineFactoryClient.instance().renderId;
    }
    
	private void renderConveyorWorld(RenderBlocks renderblocks, IBlockAccess iblockaccess, int blockX, int blockY, int blockZ, Block block, int renderId)
	{
		Tessellator tessellator = Tessellator.instance;
		int conveyorMetadata = iblockaccess.getBlockMetadata(blockX, blockY, blockZ);
		int conveyorTextureIndex = block.getBlockTexture(iblockaccess, blockX, blockY, blockZ, 0);
		tessellator.setBrightness(block.getMixedBrightnessForBlock(iblockaccess, blockX, blockY, blockZ));
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		int l1 = (conveyorTextureIndex & 0xf) << 4;
		int i2 = conveyorTextureIndex & 0xf0;
		double d = (float)l1 / 256F;
		double d1 = ((float)l1 + 15.99F) / 256F;
		double d2 = (float)i2 / 256F;
		double d3 = ((float)i2 + 15.99F) / 256F;
		
		float renderHeight = 0.0625F;
		
		float vert1x = blockX + 1;
		float vert2x = blockX + 1;
		float vert3x = blockX + 0;
		float vert4x = blockX + 0;
		
		float vert1z = blockZ + 0;
		float vert2z = blockZ + 1;
		float vert3z = blockZ + 1;
		float vert4z = blockZ + 0;
		
		float vert1y = (float)blockY + renderHeight;
		float vert2y = (float)blockY + renderHeight;
		float vert3y = (float)blockY + renderHeight;
		float vert4y = (float)blockY + renderHeight;
		
		if(conveyorMetadata == 0 || conveyorMetadata == 4 || conveyorMetadata == 8)
		{
			vert1x = vert4x = blockX + 0;
			vert2x = vert3x = blockX + 1;
			vert1z = vert2z = blockZ + 0;
			vert3z = vert4z = blockZ + 1;
			if(conveyorMetadata == 4)
			{
				vert2y++; // 1,0
				vert3y++; // 1,1
			}
			if(conveyorMetadata == 8)
			{
				vert1y++;
				vert4y++;
			}
		}
		else if(conveyorMetadata == 1 || conveyorMetadata == 5 || conveyorMetadata == 9)
		{
			vert1x = vert2x = blockX + 1;
			vert3x = vert4x = blockX + 0;
			vert1z = vert4z = blockZ + 0;
			vert2z = vert3z = blockZ + 1;
			if(conveyorMetadata == 5)
			{
				vert3y++; // 0,1
				vert2y++; // 1,1
			}
			if(conveyorMetadata == 9)
			{
				vert1y++;
				vert4y++;
			}
		}
		else if(conveyorMetadata == 2 || conveyorMetadata == 6 || conveyorMetadata == 10)
		{
			vert1x = vert4x = blockX + 1;
			vert2x = vert3x = blockX + 0;
			vert1z = vert2z = blockZ + 1;
			vert3z = vert4z = blockZ + 0;
			if(conveyorMetadata == 6)
			{
				vert2y++;
				vert3y++;
			}
			if(conveyorMetadata == 10)
			{
				vert4y++; // 1,0
				vert1y++; // 1,1
			}
		}
		else if(conveyorMetadata == 3 || conveyorMetadata == 7 || conveyorMetadata == 11)
		{
			vert1x = vert2x = blockX + 0;
			vert3x = vert4x = blockX + 1;
			vert1z = vert4z = blockZ + 1;
			vert2z = vert3z = blockZ + 0;
			if(conveyorMetadata == 7)
			{
				vert2y++;
				vert3y++;
			}
			if(conveyorMetadata == 11)
			{
				vert1y++; // 0,1
				vert4y++; // 1,1
			}
		}
		
		tessellator.addVertexWithUV(vert1x, vert1y, vert1z, d1, d2);
		tessellator.addVertexWithUV(vert2x, vert2y, vert2z, d1, d3);
		tessellator.addVertexWithUV(vert3x, vert3y, vert3z, d, d3);
		tessellator.addVertexWithUV(vert4x, vert4y, vert4z, d, d2);
		tessellator.addVertexWithUV(vert4x, vert4y, vert4z, d, d2);
		tessellator.addVertexWithUV(vert3x, vert3y, vert3z, d, d3);
		tessellator.addVertexWithUV(vert2x, vert2y, vert2z, d1, d3);
		tessellator.addVertexWithUV(vert1x, vert1y, vert1z, d1, d2);
	}
}
