package mods.nandonalt.coralmod;

import java.util.Random;

import net.minecraft.world.World;

public interface IReefGen {

	boolean generate(World world, Random random, int x, int y, int z);
	boolean isGenerated();

}