package com.sirolf2009.necromancy.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;

public class BlockAltarBlock extends BlockContainer
{

    public BlockAltarBlock()
    {
        super(Material.rock);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int i)
    {
        return getTileEntity(var1, (int) maxX, (int) maxY, (int) maxZ);
    }

    public TileEntity getTileEntity(World par1World, int x, int y, int z)
    {
        switch (par1World.getBlockMetadata(x, y, z))
        {
        case 2:
            if (par1World.getBlock(x, y, z + 1) == this)
                return par1World.getTileEntity(x, y, z + 2);
            else
                return par1World.getTileEntity(x, y, z + 1);
        case 0:
            if (par1World.getBlock(x, y, z - 1) == this)
                return par1World.getTileEntity(x, y, z - 2);
            else
                return par1World.getTileEntity(x, y, z - 1);
        case 1:
            if (par1World.getBlock(x + 1, y, z) == this)
                return par1World.getTileEntity(x + 2, y, z);
            else
                return par1World.getTileEntity(x + 1, y, z);
        case 3:
            if (par1World.getBlock(x - 1, y, z) == this)
                return par1World.getTileEntity(x - 2, y, z);
            else
                return par1World.getTileEntity(x - 1, y, z);
        }
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int idk, float what, float these, float are)
    {
        TileEntity tileEntity = getTileEntity(world, x, y, z);
        if (tileEntity == null || player.isSneaking())
            return false;
        else
        {
            player.openGui(Necromancy.instance, 0, world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
            return true;
        }
    }

    @Override
    public void onBlockDestroyedByPlayer(World par1World, int x, int y, int z, int par5)
    {
        switch (par5)
        {
        case 2:
            if (par1World.getBlock(x, y, z + 1) == this)
            {
                par1World.setBlock(x, y, z + 1, Blocks.air, 0, 0);
                par1World.setBlock(x, y, z + 2, Blocks.air, 0, 0);
                par1World.removeTileEntity(x, y, z + 2);
            }
            else
            {
                par1World.setBlock(x, y, z - 1, Blocks.air, 0, 0);
                par1World.setBlock(x, y, z + 1, Blocks.air, 0, 0);
                par1World.removeTileEntity(x, y, z + 1);
            }
        case 0:
            if (par1World.getBlock(x, y, z - 1) == this)
            {
                par1World.setBlock(x, y, z - 1, Blocks.air, 0, 0);
                par1World.setBlock(x, y, z - 2, Blocks.air, 0, 0);
                par1World.removeTileEntity(x, y, z - 2);
            }
            else
            {
                par1World.setBlock(x, y, z + 1, Blocks.air, 0, 0);
                par1World.setBlock(x, y, z - 1, Blocks.air, 0, 0);
                par1World.removeTileEntity(x, y, z - 1);
            }
        case 1:
            if (par1World.getBlock(x + 1, y, z) == this)
            {
                par1World.setBlock(x + 1, y, z, Blocks.air, 0, 0);
                par1World.setBlock(x + 2, y, z, Blocks.air, 0, 0);
                par1World.removeTileEntity(x + 2, y, z);
            }
            else
            {
                par1World.setBlock(x - 1, y, z, Blocks.air, 0, 0);
                par1World.setBlock(x + 1, y, z, Blocks.air, 0, 0);
                par1World.removeTileEntity(x + 1, y, z);
            }
        case 3:
            if (par1World.getBlock(x - 1, y, z) == this)
            {
                par1World.setBlock(x - 1, y, z, Blocks.air, 0, 0);
                par1World.setBlock(x - 2, y, z, Blocks.air, 0, 0);
                par1World.removeTileEntity(x - 2, y, z);
            }
            else
            {
                par1World.setBlock(x + 1, y, z, Blocks.air, 0, 0);
                par1World.setBlock(x - 1, y, z, Blocks.air, 0, 0);
                par1World.removeTileEntity(x - 1, y, z);
            }
        }
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }
}
