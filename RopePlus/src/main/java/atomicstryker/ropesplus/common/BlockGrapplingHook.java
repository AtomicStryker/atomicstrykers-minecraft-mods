package atomicstryker.ropesplus.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockGrapplingHook extends Block
{
    public BlockGrapplingHook()
    {
        super(Material.vine);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    }
    
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
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
        Block l = world.getBlock(i, j - 1, k);
        if(!l.isOpaqueCube())
        {
            return false;
        } else
        {
            return l.getMaterial().isSolid();
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int i, int j, int k, Block l)
    {
        if(!canPlaceBlockAt(world, i, j, k))
        {
            dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
            world.setBlock(i, j, k, Blocks.air, 0, 3);
            onBlockDestroyed(world, i, j, k);
        }
    }

    @Override
    public Item getItemDropped(int var1, Random var2, int var3)
    {
        return RopesPlusCore.instance.itemGrapplingHook;
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
            if(world.getBlock(candidates[l][0], candidates[l][1], candidates[l][2]) != RopesPlusCore.instance.blockRopeWall)
            {
                continue;
            }
            
            System.out.println("Rope found at ["+candidates[l][0]+","+candidates[l][1]+","+candidates[l][2]+"]");
            
            for(int m = candidates[l][1]; world.getBlock(candidates[l][0], m, candidates[l][2]) == RopesPlusCore.instance.blockRopeWall; m--)
            {
                world.setBlock(candidates[l][0], m, candidates[l][2], Blocks.air, 0, 3);
            }
        }
    }
    
    @Override
    public int getRenderType()
    {
        return 23; //RopesPlusCore.proxy.getGrapplingHookRenderId();
    }
}
