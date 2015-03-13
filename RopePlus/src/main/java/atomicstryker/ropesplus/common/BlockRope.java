package atomicstryker.ropesplus.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class BlockRope extends Block
{
    public BlockRope()
    {
        super(Material.vine);
        float f = 0.1F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
    
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("ropesplus:rope");
    }
    
    @Override
    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity)
    {
        return true;
    }

    @Override
    public void onBlockAdded(World world, int i, int j, int k)
    {
        byte xoffset = 0;
        byte zoffset = 0;
        byte ropeending = 0;
        if(world.getBlockState(new BlockPos(i - 1, j, k + 0)).getBlock() == this)
        {
            xoffset = -1;
            zoffset = 0;
        }
        else if(world.getBlockState(new BlockPos(i + 1, j, k + 0)).getBlock() == this)
        {
            xoffset = 1;
            zoffset = 0;
        }
        else if(world.getBlockState(new BlockPos(i, j, k - 1)).getBlock() == this)
        {
            xoffset = 0;
            zoffset = -1;
        }
        else if(world.getBlockState(new BlockPos(i, j, k + 1)).getBlock() == this)
        {
            xoffset = 0;
            zoffset = 1;
        }
        if(xoffset != 0 || zoffset != 0)
        {
            for(int length = 1; length <= 32; length++)
            {
                if(world.getBlockState(new BlockPos(i + xoffset, j - length, k + zoffset)).getBlock().isOpaqueCube())
                {
                    ropeending = 2;
                }
                if(ropeending == 0 && world.getBlockState(new BlockPos(i + xoffset, j - length, k + zoffset)).getBlock() == Blocks.air)
                {
                    ropeending = 1;
                    world.setBlockState(new BlockPos(i,  j,  k),  Blocks.air.getStateFromMeta( 0));
                    world.setBlockState(new BlockPos(i + xoffset,  j - length,  k + zoffset),  this.getStateFromMeta( 0));
                }
            }

        }
        if((ropeending == 0 || ropeending == 2) && (world.getBlockState(new BlockPos(i, j + 1, k)).getBlock() != this) && !world.getBlockState(new BlockPos(i, j + 1, k)).getBlock().isOpaqueCube())
        {
            dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
            world.setBlockState(new BlockPos(i,  j,  k),  Blocks.air.getStateFromMeta( 0));
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int i, int j, int k, Block l)
    {
        super.onNeighborBlockChange(world, i, j, k, l);
        boolean blockstays = false;
        if(world.getBlockState(new BlockPos(i - 1, j, k + 0)).getBlock() == this)
        {
            blockstays = true;
        }
        if(world.getBlockState(new BlockPos(i + 1, j, k + 0)).getBlock() == this)
        {
            blockstays = true;
        }
        if(world.getBlockState(new BlockPos(i, j, k - 1)).getBlock() == this)
        {
            blockstays = true;
        }
        if(world.getBlockState(new BlockPos(i, j, k + 1)).getBlock() == this)
        {
            blockstays = true;
        }
        if(world.getBlockState(new BlockPos(i, j + 1, k)).getBlock().isOpaqueCube())
        {
            blockstays = true;
        }
        if(world.getBlockState(new BlockPos(i, j + 1, k)).getBlock() == this)
        {
            blockstays = true;
        }
        if(!blockstays)
        {
            dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
            world.setBlockState(new BlockPos(i,  j,  k),  Blocks.air.getStateFromMeta( 0));
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, int i, int j, int k)
    {
        if(world.getBlockState(new BlockPos(i - 1, j, k + 0)).getBlock() == this)
        {
            return true;
        }
        if(world.getBlockState(new BlockPos(i + 1, j, k + 0)).getBlock() == this)
        {
            return true;
        }
        if(world.getBlockState(new BlockPos(i, j, k - 1)).getBlock() == this)
        {
            return true;
        }
        if(world.getBlockState(new BlockPos(i, j, k + 1)).getBlock() == this)
        {
            return true;
        }
        if(world.getBlockState(new BlockPos(i, j + 1, k)).getBlock().isOpaqueCube())
        {
            return true;
        }
        return world.getBlockState(new BlockPos(i, j + 1, k)).getBlock() == this;
    }
	
    @Override
    public void onBlockDestroyedByPlayer(World world, int a, int b, int c, int l)
    {
        System.out.println("Player destroyed Rope block at "+a+","+b+","+c);	
    	
		int[] coords = RopesPlusCore.instance.areCoordsArrowRope(a, b, c);
		if (coords == null)
		{
			System.out.println("Player destroyed Rope is not Arrow Rope, going on");
			return;
		}
		
		int rope_max_y;
		int rope_min_y;
		
		if (world.getBlockState(new BlockPos(a, b, c)).getBlock() == this)
		{
			world.setBlockState(new BlockPos(a,  b,  c),  Blocks.air.getStateFromMeta( 0));
		}
		
		for(int x = 1;; x++)
		{
			if (world.getBlockState(new BlockPos(a, b+x, c)).getBlock() != this)
			{
				rope_max_y = b+x-1;
				System.out.println("Player destroyed Rope goes ["+(x-1)+"] blocks higher, up to "+a+","+rope_max_y+","+c);
				System.out.println("Differing BlockID is: "+world.getBlockState(new BlockPos(a, b+x, c)).getBlock());
				break;
			}
		}
		
		for(int x = 0;; x--)
		{
			if (world.getBlockState(new BlockPos(a, b+x, c)).getBlock() != this)
			{
				rope_min_y = b+x+1;
				System.out.println("Player destroyed Rope goes ["+(x+1)+"] blocks lower, down to "+a+","+rope_min_y+","+c);
				break;
			}
		}
		
		int ropelenght = rope_max_y-rope_min_y;
		
		for(int x = 0; x <= ropelenght; x++)
		{
			coords = RopesPlusCore.instance.areCoordsArrowRope(a, rope_min_y+x, c);
			
			world.setBlockState(new BlockPos(a,  rope_min_y+x,  c),  Blocks.air.getStateFromMeta( 0));
			
			if (coords != null)
			{
				RopesPlusCore.instance.removeCoordsFromRopeArray(coords);
			}
		}
		
		System.out.println("Player destroyed Rope lenght: "+(rope_max_y-rope_min_y));
		
		if(!world.isRemote)
        {
			EntityItem entityitem = new EntityItem(world, a, b, c, new ItemStack(Items.stick));
			entityitem.delayBeforeCanPickup = 5;
			world.spawnEntityInWorld(entityitem);
			
			entityitem = new EntityItem(world, a, b, c, new ItemStack(Items.feather));
			entityitem.delayBeforeCanPickup = 5;
			world.spawnEntityInWorld(entityitem);
		}
		
		System.out.println("Rope destruction func finished");
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
    public int getRenderType()
    {
        return 1;
    }
}
