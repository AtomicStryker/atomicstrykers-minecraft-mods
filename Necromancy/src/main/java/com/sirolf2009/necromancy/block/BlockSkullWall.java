package com.sirolf2009.necromancy.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.tileentity.TileEntitySkullWall;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSkullWall extends BlockContainer
{
    public BlockSkullWall()
    {
        super(Material.circuits);
        setHardness(50.0F);
        setResistance(2000.0F);
        setStepSound(soundTypeStone);
        setBlockName("skullWall");
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public TileEntity createNewTileEntity(World par1World, int i)
    {
        return new TileEntitySkullWall();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(Blocks.skull);
    }

    @Override
    public int getDamageValue(World par1World, int par2, int par3, int par4)
    {
        TileEntity tileentity = par1World.getTileEntity(par2, par3, par4);
        return tileentity != null && tileentity instanceof TileEntitySkull ? ((TileEntitySkull) tileentity).func_145904_a() : super.getDamageValue(
                par1World, par2, par3, par4);
    }

    @Override
    public int damageDropped(int par1)
    {
        return par1;
    }

    @Override
    public void onBlockHarvested(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer)
    {
        if (par6EntityPlayer.capabilities.isCreativeMode)
        {
            par5 |= 8;
            par1World.setBlockMetadataWithNotify(par2, par3, par4, par5, 4);
        }

        dropBlockAsItem(par1World, par2, par3, par4, par5, 0);

        super.onBlockHarvested(par1World, par2, par3, par4, par5, par6EntityPlayer);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if ((metadata & 8) == 0)
        {
            ItemStack itemstack = new ItemStack(Items.skull, 1, this.getDamageValue(world, x, y, z));
            TileEntitySkull tileentityskull = (TileEntitySkull) world.getTileEntity(x, y, z);

            if (tileentityskull == null)
                return drops;
            if (tileentityskull.func_145904_a() == 3 && tileentityskull.func_145907_c() != null && tileentityskull.func_145907_c().length() > 0)
            {
                itemstack.setTagCompound(new NBTTagCompound());
                itemstack.getTagCompound().setString("SkullOwner", tileentityskull.func_145907_c());
            }
            drops.add(itemstack);
        }
        return drops;
    }

    @Override
    public Item getItemDropped(int par1, Random par2Random, int par3)
    {
        return Items.skull;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IIconRegister)
    {
        par1IIconRegister.registerIcon("obsidian");
    }
}
