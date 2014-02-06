package com.sirolf2009.necromancy.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.client.renderer.RenderScent;
import com.sirolf2009.necromancy.tileentity.TileEntityScent;

public class BlockScent extends Block implements ITileEntityProvider
{

    private IIcon[] IIcons;

    public BlockScent()
    {
        super(Material.air);
        this.setCreativeTab(Necromancy.tabNecromancy);
        setBlockTextureName("necromancy:scent");
    }

    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
    {
        par1World.markBlockForUpdate(par2, par3, par4);
    }

    public TileEntity createNewTileEntity(World par1World)
    {
        return new TileEntityScent();
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
        return RenderScent.renderID;
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }

    @Override
    public boolean canRenderInPass(int pass)
    {
        RenderScent.renderPass = pass;
        return pass == 0;
    }

    @Override
    public int getRenderBlockPass()
    {
        return 0;
    }

    public void registerIIcons(IIconRegister par1IIconRegister)
    {
        IIcons = new IIcon[4];
        for (int i = 0; i < 4; i++)
        {
            IIcons[i] = par1IIconRegister.registerIcon("necromancy:scent" + i);
        }
    }

    public IIcon getIIcon(int par1, int par2)
    {
        return IIcons[par2];
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return null;
    }
}
