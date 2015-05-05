package atomicstryker.ropesplus.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
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
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos p)
    {
        Block l = world.getBlockState(p.add(0, -1, 0)).getBlock();
        if(!l.isOpaqueCube())
        {
            return false;
        } else
        {
            return l.getMaterial().isSolid();
        }
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos p, IBlockState state, Block l)
    {
        if(!canPlaceBlockAt(world, p))
        {
            dropBlockAsItem(world, p, state, 0);
            world.setBlockState(p,  Blocks.air.getStateFromMeta(0));
            onBlockDestroyed(world, p);
        }
    }

    @Override
    public Item getItemDropped(IBlockState s, Random var2, int var3)
    {
        return RopesPlusCore.instance.itemGrapplingHook;
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 1;
    }
    
    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos p, IBlockState state)
    {
        onBlockDestroyed(world, p);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos p, Explosion e)
    {
        onBlockDestroyed(world, p);
    }

    private void onBlockDestroyed(World world, BlockPos p)
    {
    	System.out.println("Original Hook break at "+p);
    	
    	int i = p.getX();
    	int j = p.getY();
    	int k = p.getZ();
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
            if(world.getBlockState(new BlockPos(candidates[l][0], candidates[l][1], candidates[l][2])).getBlock() != RopesPlusCore.instance.blockRopeWall)
            {
                continue;
            }
            
            System.out.println("Rope found at ["+candidates[l][0]+","+candidates[l][1]+","+candidates[l][2]+"]");
            
            for(int m = candidates[l][1]; world.getBlockState(new BlockPos(candidates[l][0], m, candidates[l][2])).getBlock() == RopesPlusCore.instance.blockRopeWall; m--)
            {
                world.setBlockState(new BlockPos(candidates[l][0],  m,  candidates[l][2]),  Blocks.air.getStateFromMeta( 0));
            }
        }
    }
    
    @Override
    public int getRenderType()
    {
        return 23; //RopesPlusCore.proxy.getGrapplingHookRenderId();
    }
}
