package atomicstryker.ropesplus.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockGrapplingHook extends Block
{
    public BlockGrapplingHook(int i)
    {
        super(i, Material.wood);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    }
    
    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("ropesplus:blockGrapplingHook");
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k)
    {
        return null;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int i, int j, int k)
    {
        int l = world.getBlockId(i, j - 1, k);
        if(l == 0 || !Block.blocksList[l].isOpaqueCube())
        {
            return false;
        } else
        {
            return world.getBlockMaterial(i, j - 1, k).isSolid();
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int i, int j, int k, int l)
    {
        if(!canPlaceBlockAt(world, i, j, k))
        {
            dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
            world.setBlock(i, j, k, 0, 0, 3);
            onBlockDestroyed(world, i, j, k);
        }
    }

    @Override
    public int idDropped(int var1, Random var2, int var3)
    {
        return RopesPlusCore.itemGrapplingHook.itemID;
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 1;
    }
    
    @Override
    public void onBlockDestroyedByPlayer(World world, int i, int j, int k, int l)
    {
        onBlockDestroyed(world, i, j, k);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int i, int j, int k, Explosion e)
    {
        onBlockDestroyed(world, i, j, k);
    }

    private void onBlockDestroyed(World world, int i, int j, int k)
    {
    	System.out.println("Original Hook break at ["+i+","+j+","+k+"]");
    	
        int candidates[][] = {
            {
                i - 1, j - 1, k
            }, {
                i + 1, j - 1, k
            }, {
                i, j - 1, k - 1
            }, {
                i, j - 1, k + 1
            }
        };
        for(int l = 0; l < candidates.length; l++)
        {
            if(world.getBlockId(candidates[l][0], candidates[l][1], candidates[l][2]) != RopesPlusCore.blockRopeWallPos.blockID)
            {
                continue;
            }
            
            System.out.println("Rope found at ["+candidates[l][0]+","+candidates[l][1]+","+candidates[l][2]+"]");
            
            for(int m = candidates[l][1]; world.getBlockId(candidates[l][0], m, candidates[l][2]) == RopesPlusCore.blockRopeWallPos.blockID; m--)
            {
                world.setBlock(candidates[l][0], m, candidates[l][2], 0, 0, 3);
            }
        }
    }
    
    @Override
    public int getRenderType()
    {
        return 23; //RopesPlusCore.proxy.getGrapplingHookRenderId();
    }
}
