package mods.nandonalt.coralmod;

import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

public class CoralGenerator {

	@SubscribeEvent
	public void populateChunk(PopulateChunkEvent.Post evt) {
		// Check coral generation is enabled
		if(!CoralMod.getEnable()) {
			return;
		}

		// Check dimension
		if(evt.world.getWorldInfo().getVanillaDimension() != 0 && !CoralMod.getDimensions()) {
			return;
		}

		// Convert to non-chunk positions
		final int posX = evt.chunkX << 4;
		final int posZ = evt.chunkZ << 4;

		// Check biome
		if(CoralMod.getOcean()) {
			final BiomeGenBase biome = getBiomeGenAt(evt.world, posX, posZ);
			if(biome.biomeName.endsWith("River") || biome.biomeName.startsWith("River")
			|| !BiomeDictionary.isBiomeOfType(biome, Type.WATER)) {
				return;
			}
		} else {
			List<String> biomesList = CoralMod.getBiomesList();
			if(!biomesList.isEmpty()) {
				int biomeID = getBiomeGenAt(evt.world, posX, posZ).biomeID;
				if(!biomesList.contains(((Integer)biomeID).toString())) {
					return;
				}
			}
		}

		generate(evt.rand, evt.chunkX, evt.chunkZ, evt.world);
	}

	private BiomeGenBase getBiomeGenAt(World world, int posX, int posZ) {
		return world.getWorldChunkManager().getBiomeGenAt(posX, posZ);
	}

	/**
	 * Generate coral reef
	 */
	public static boolean generate(Random random, int chunkX, int chunkZ, World world) {
		// Convert to non-chunk positions
		final int posX = chunkX << 4;
		final int posZ = chunkZ << 4;

		// Reef generation size
		final int min1;
		final int min2;
		final int max1;
		final int max2;

		final int size = CoralMod.getSize();
		if(size == 0) {
			min1 = 15;
			min2 = 10;
			max1 = 40;
			max2 = 20;
		} else if(size == 2) {
			min1 = 45;
			min2 = 30;
			max1 = 70;
			max2 = 45;
		} else {
			min1 = 35;
			min2 = 25;
			max1 = 60;
			max2 = 35;
		}

		IReefGen reefGen;
		int genNum = 0;

		final int maxHeight = CoralMod.getMaxHeight(world);
		final int minHeight = CoralMod.getMinHeight();
		final int iterations = CoralMod.getIterations();
		final int radius = CoralMod.getRadius();

		for(int i = 0; i < iterations; i++) {
			final int x = posX + random.nextInt(radius);
			final int y = minHeight + random.nextInt(maxHeight - minHeight);
			final int z = posZ + random.nextInt(radius);
			final int numberReef = random.nextInt(max1 - min1 + 1) + min1;
			reefGen = new ReefGen(numberReef, CoralMod.Coral2, CoralMod.getSpiky());
			reefGen.generate(world, random, x, y, z);
			if(reefGen.isGenerated()) {
				genNum++;
			}
		}

		for(int i = 0; i < iterations; i++) {
			final int x = posX + random.nextInt(radius);
			final int y = minHeight + random.nextInt(maxHeight - minHeight);
			final int z = posZ + random.nextInt(radius);
			final int numberReef = random.nextInt(max2 - min2 + 1) + min2;
			reefGen = new ReefGen2(numberReef, CoralMod.Coral3);
			reefGen.generate(world, random, x, y, z);
			if(reefGen.isGenerated()) {
				genNum++;
			}
		}

		return genNum > 0;
	}

}