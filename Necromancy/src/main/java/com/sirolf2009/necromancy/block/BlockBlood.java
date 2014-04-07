package com.sirolf2009.necromancy.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import com.sirolf2009.necromancy.Necromancy;

public class BlockBlood extends BlockFluidClassic
{

    public BlockBlood(Fluid fluid)
    {
        super(fluid, Material.water);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.flowing_water.getIcon(side, meta);
    }

    @Override
    public int colorMultiplier(IBlockAccess iblockaccess, int x, int y, int z)
    {
        return 0xD90000;
    }
    
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        super.randomDisplayTick(world, x, y, z, rand);
        if (rand.nextInt(10) == 0
                && World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)
                && !world.getBlock(x, y - 2, z).getMaterial().blocksMovement()) {
            
            double px = (double) ((float) x + rand.nextFloat());
            double py = (double) y - 1.05D;
            double pz = (double) ((float) z + rand.nextFloat());
            world.spawnParticle("dripLava", px, py, pz, 0, 0, 0);
        }
    }
}
