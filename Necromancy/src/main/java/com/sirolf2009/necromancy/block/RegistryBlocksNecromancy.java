package com.sirolf2009.necromancy.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;
import com.sirolf2009.necromancy.tileentity.TileEntitySkullWall;

import cpw.mods.fml.common.registry.GameRegistry;

public class RegistryBlocksNecromancy
{

    public static Block altar;
    public static Block altarBlock;
    public static Block sewing;
    public static BlockBlood blood;
    public static Block skullWall;

    public static Fluid fluidBlood;

    public static void initBlocks()
    {
        altar = new BlockAltar().setHardness(4);
        altar.setBlockName("Summoning Altar");
        GameRegistry.registerBlock(altar, "Summoning Altar");
        GameRegistry.registerTileEntity(TileEntityAltar.class, "Summoning Altar");

        altarBlock = new BlockAltarBlock().setHardness(4);
        altarBlock.setBlockName("Altar Building Block");
        GameRegistry.registerBlock(altarBlock, "Altar Building Block");

        sewing = new BlockSewing(Material.iron).setHardness(4);
        sewing.setBlockName("Sewing Machine");
        GameRegistry.registerBlock(sewing, "Sewing Machine");
        GameRegistry.registerTileEntity(TileEntitySewing.class, "Sewing");

        fluidBlood = new Fluid("blood");
        FluidRegistry.registerFluid(fluidBlood);

        blood = new BlockBlood(fluidBlood);
        blood.setBlockName("FlowingBlood");
        fluidBlood.setBlock(blood);
        GameRegistry.registerBlock(blood, "FlowingBlood");
        
        skullWall = new BlockSkullWall();
        skullWall.setBlockName("skullWall");
        GameRegistry.registerBlock(skullWall, "skullWall");
        GameRegistry.registerTileEntity(TileEntitySkullWall.class, "skullWall");
    }

    public static void initRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(RegistryBlocksNecromancy.sewing, 1), new Object[] { "III", "ISB", "III", 'I', Items.iron_ingot, 'S',
                Items.string, 'B', ItemGeneric.getItemStackFromName("Bone Needle") });
    }

}
