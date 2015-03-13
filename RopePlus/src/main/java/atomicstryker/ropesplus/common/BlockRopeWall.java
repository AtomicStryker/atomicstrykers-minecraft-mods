package atomicstryker.ropesplus.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRopeWall extends BlockContainer
{
    public BlockRopeWall()
    {
        super(Material.vine);
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
    public boolean canPlaceBlockOnSide(World var1, int var2, int var3, int var4, int var5)
    {
        switch (var5)
        {
            case 1:
                return this.canBePlacedOn(var1.getBlock(var2, var3, var4 + 1));
            case 2:
                return this.canBePlacedOn(var1.getBlock(var2, var3, var4 + 1));
            case 3:
                return this.canBePlacedOn(var1.getBlock(var2, var3, var4 - 1));
            case 4:
                return this.canBePlacedOn(var1.getBlock(var2 + 1, var3, var4));
            case 5:
                return this.canBePlacedOn(var1.getBlock(var2 - 1, var3, var4));
            default:
                return false;
        }
    }
	
    private boolean canBePlacedOn(Block var1)
    {
        return var1.renderAsNormalBlock() && var1.getMaterial().blocksMovement();
    }

	@Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k)
    {
        int l = world.getBlockMetadata(i, j, k);
        float f = 0.125F;
        if(l == 1)
        {
            setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
        }
        if(l == 4)
        {
            setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
        }
        if(l == 8)
        {
            setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
        if(l == 2)
        {
            setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
        }
        return super.getCollisionBoundingBoxFromPool(world, i, j, k);
    }
    
	@Override
    public void setBlockBoundsForItemRender()
    {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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
    public void setBlockBoundsBasedOnState(IBlockAccess var1, int var2, int var3, int var4)
    {
        int metaData = var1.getBlockMetadata(var2, var3, var4);
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
    public void onBlockDestroyedByPlayer(World world, int i, int j, int k, int l)
    {
        onBlockDestroyed(world, i, j, k);
    }

	@Override
    public void onBlockDestroyedByExplosion(World world, int i, int j, int k, Explosion e)
    {
        onBlockDestroyed(world, i, j, k);
    }
	
    public void onBlockDestroyed(World world, int a, int b, int c)
    {
		if(world.isRemote)
        {
            return;
        }
		
		int rope_max_y;
		int rope_min_y;
		
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
				entityitem.delayBeforeCanPickup = 5;
				world.spawnEntityInWorld(entityitem);
				
				IsHook = true;
				break;
			}
		}
		
		if (!IsHook)
		{
			EntityItem entityitem = new EntityItem(world, a, b, c, new ItemStack(RopesPlusCore.instance.getArrowItemByTip(RopesPlusCore.instance.blockRope)));
			entityitem.delayBeforeCanPickup = 5;
			world.spawnEntityInWorld(entityitem);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int i)
	{
		return null;
	}
}
