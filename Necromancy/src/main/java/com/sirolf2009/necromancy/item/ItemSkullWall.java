package com.sirolf2009.necromancy.item;

import java.util.List;

import net.minecraft.block.BlockSkull;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemSkullWall extends Item
{
    private static final String[] skullTypes = new String[] { "skeleton", "wither", "zombie", "char", "creeper" };
    private static final String[] skullNames = new String[] { "skull_skeleton", "skull_wither", "skull_zombie", "skull_char", "skull_creeper" };
    private IIcon[] icons;

    public ItemSkullWall()
    {
        super();
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }
    
    @Override
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
    {
        if (par7 == 0)
            return false;
        else if (!par3World.getBlock(par4, par5, par6).getMaterial().isSolid())
            return false;
        else
        {
            if (par7 == 1)
            {
                ++par5;
            }

            if (par7 == 2)
            {
                --par6;
            }

            if (par7 == 3)
            {
                ++par6;
            }

            if (par7 == 4)
            {
                --par4;
            }

            if (par7 == 5)
            {
                ++par4;
            }

            if (!par2EntityPlayer.canPlayerEdit(par4, par5, par6, par7, par1ItemStack))
                return false;
            else if (!Blocks.skull.canPlaceBlockAt(par3World, par4, par5, par6))
                return false;
            else
            {
                par3World.setBlock(par4, par5, par6, Blocks.skull, par7, 2);
                int i1 = 0;

                if (par7 == 1)
                {
                    i1 = MathHelper.floor_double(par2EntityPlayer.rotationYaw * 16.0F / 360.0F + 0.5D) & 15;
                }

                TileEntity tileentity = par3World.getTileEntity(par4, par5, par6);

                if (tileentity != null && tileentity instanceof TileEntitySkull)
                {
                    String s = "";

                    if (par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey("SkullOwner"))
                    {
                        s = par1ItemStack.getTagCompound().getString("SkullOwner");
                    }

                    ((TileEntitySkull) tileentity).func_145905_a(par1ItemStack.getItemDamage(), s);
                    ((TileEntitySkull) tileentity).func_145903_a(i1);
                    ((BlockSkull) Blocks.skull).func_149965_a(par3World, par4, par5, par6, (TileEntitySkull) tileentity);
                }

                --par1ItemStack.stackSize;
                return true;
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int j = 0; j < skullTypes.length; ++j)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

    @Override
    public int getMetadata(int par1)
    {
        return par1;
    }

    @Override
    public IIcon getIconFromDamage(int par1)
    {
        if (par1 < 0 || par1 >= skullTypes.length)
        {
            par1 = 0;
        }

        return icons[par1];
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        int i = par1ItemStack.getItemDamage();

        if (i < 0 || i >= skullTypes.length)
        {
            i = 0;
        }

        return super.getUnlocalizedName() + "." + skullTypes[i];
    }

    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack)
    {
        return par1ItemStack.getItemDamage() == 3 && par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey("SkullOwner")
                ? StatCollector.translateToLocalFormatted("Items.skull.player.name",
                        new Object[] { par1ItemStack.getTagCompound().getString("SkullOwner") }) : super.getItemStackDisplayName(par1ItemStack);
    }

    @Override
    public void registerIcons(IIconRegister par1IIconRegister)
    {
        icons = new IIcon[skullNames.length];

        for (int i = 0; i < skullNames.length; ++i)
        {
            icons[i] = par1IIconRegister.registerIcon(skullNames[i]);
        }
    }
}
