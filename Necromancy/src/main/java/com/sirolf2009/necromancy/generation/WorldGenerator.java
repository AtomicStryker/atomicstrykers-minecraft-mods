package com.sirolf2009.necromancy.generation;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator
{

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        int x = random.nextInt(16) * chunkX;
        int y = random.nextInt(60);
        int z = random.nextInt(16) * chunkZ;
        if (world.provider.dimensionId == -1)
        {
            new WorldGenNetherChalice().generate(world, random, x, y, z);
        }
    }

}
