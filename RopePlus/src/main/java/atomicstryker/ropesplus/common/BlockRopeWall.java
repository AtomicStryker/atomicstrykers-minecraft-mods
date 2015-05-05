package atomicstryker.ropesplus.common;

import java.util.Random;

import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRopeWall extends BlockLadder
{
    public BlockRopeWall()
    {
        super();
    }
    
	@Override
    public void setBlockBoundsForItemRender()
    {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }
    
	@Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
	    int metaData = this.getMetaFromState(worldIn.getBlockState(pos));
        float var7 = 1.0F;
        float var8 = 1.0F;
        float var9 = 1.0F;
        float var10 = 0.0F;
        float var11 = 0.0F;
        float var12 = 0.0F;
        if ((metaData & 2) != 0)
        {
            var10 = Math.max(var10, 0.0625F);
            var7 = 0.0F;
            var8 = 0.0F;
            var11 = 1.0F;
            var9 = 0.0F;
            var12 = 1.0F;
        }

        if ((metaData & 8) != 0)
        {
            var7 = Math.min(var7, 0.9375F);
            var10 = 1.0F;
            var8 = 0.0F;
            var11 = 1.0F;
            var9 = 0.0F;
            var12 = 1.0F;
        }

        if ((metaData & 4) != 0)
        {
            var12 = Math.max(var12, 0.0625F);
            var9 = 0.0F;
            var7 = 0.0F;
            var10 = 1.0F;
            var8 = 0.0F;
            var11 = 1.0F;
        }

        if ((metaData & 1) != 0)
        {
            var9 = Math.min(var9, 0.9375F);
            var12 = 1.0F;
            var7 = 0.0F;
            var10 = 1.0F;
            var8 = 0.0F;
            var11 = 1.0F;
        }

        this.setBlockBounds(var7, var8, var9, var10, var11, var12);
    }

	@Override
    public int getRenderType()
    {
		return Blocks.vine.getRenderType();
    }

	@Override
    public int quantityDropped(Random random)
    {
        return 0;
    }
    
	@Override
    public void onBlockDestroyedByPlayer(World world, BlockPos p, IBlockState s)
    {
        onBlockDestroyed(world, p);
    }

	@Override
    public void onBlockDestroyedByExplosion(World world, BlockPos p, Explosion e)
    {
        onBlockDestroyed(world, p);
    }
	
    public void onBlockDestroyed(World world, BlockPos p)
    {
		if(world.isRemote)
        {
            return;
        }
		
		int rope_max_y;
		int rope_min_y;
		int a = p.getX();
		int b = p.getY();
		int c = p.getZ();
		
		for(int x = 1;; x++)
		{
			if (world.getBlockState(new BlockPos(a, b+x, c)).getBlock() != RopesPlusCore.instance.blockRopeWall)
			{
				rope_max_y = (b+x)-1;
				break;
			}
		}
		
		for(int x = -1;; x--)
		{
			if (world.getBlockState(new BlockPos(a, b+x, c)).getBlock() != RopesPlusCore.instance.blockRopeWall)
			{
				rope_min_y = (b+x)+1;
				break;
			}
		}
		
		int ropelenght = rope_max_y-rope_min_y;
		
		System.out.println("Rope min: "+rope_min_y+", Rope max: "+rope_max_y+", lenght: "+ropelenght);
		
		for(int x = 0; x <= ropelenght; x++)
		{
			world.setBlockState(new BlockPos(a,  rope_max_y-x,  c),  Blocks.air.getStateFromMeta( 0));
		}
		
		//ModLoader.getMinecraftInstance().ingameGUI.addChatMessage("Rope height of ["+(h-b)+"] removed");
		
		int h = rope_max_y;
		
		int candidates[][] = {
			{a-1, h+1, c},
			{a, h+1, c-1},
			{a, h+1, c+1},
			{a+1, h+1, c}
        };
		
		boolean IsHook = false;
		for(int y = 0; y < candidates.length; y++)
		{
			if(world.getBlockState(new BlockPos(candidates[y][0], candidates[y][1], candidates[y][2])).getBlock() == RopesPlusCore.instance.blockGrapplingHook)
			{
				world.setBlockState(new BlockPos(candidates[y][0],  candidates[y][1],  candidates[y][2]),  Blocks.air.getStateFromMeta( 0));
				
				EntityItem entityitem = new EntityItem(world, a, b, c, new ItemStack(RopesPlusCore.instance.itemGrapplingHook));
				entityitem.setPickupDelay(5);
				world.spawnEntityInWorld(entityitem);
				
				IsHook = true;
				break;
			}
		}
		
		if (!IsHook)
		{
			EntityItem entityitem = new EntityItem(world, a, b, c, new ItemStack(RopesPlusCore.instance.getArrowItemByTip(RopesPlusCore.instance.blockRope)));
			entityitem.setPickupDelay(5);
			world.spawnEntityInWorld(entityitem);
		}
	}

	@Override
	public TileEntity createTileEntity(World var1, IBlockState s)
	{
		return null;
	}
}
