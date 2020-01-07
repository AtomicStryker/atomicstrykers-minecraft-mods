package atomicstryker.ruins.common;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.*;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

class RuinGenerator {
    static final int WORLD_MAX_HEIGHT = 256;
    private final static String fileName = "RuinsPositionsFile.txt";

    private static IForgeRegistry<Biome> biomeRegistry = null;

    private final FileHandler fileHandler;
    private final RuinStats stats;
    private final ConcurrentSkipListSet<RuinData> registeredRuins;
    private final File ruinsDataFile;
    private final File ruinsDataFileWriting;
    private int numTries = 0, LastNumTries = 0;
    private AtomicBoolean flushing;

    public RuinGenerator(FileHandler rh, World world) {
        fileHandler = rh;
        stats = new RuinStats();
        registeredRuins = new ConcurrentSkipListSet<>();
        flushing = new AtomicBoolean(false);

        ruinsDataFile = new File(rh.saveFolder, fileName);
        ruinsDataFileWriting = new File(rh.saveFolder, fileName + "_writing");

        new LoadThread().start();
    }

    void flushPosFile(String worldName) {
        if (registeredRuins.isEmpty() || worldName.equals("MpServer")) {
            return;
        }

        // begin new flush operation unless another already in progress
        if (flushing.compareAndSet(false, true)) {
            new FlushThread().start();
        }
    }

    private void loadPosFile(File file) {
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Ruins crashed trying to access file " + file);
                }
            }
            int lineNumber = 1;
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                if (!line.startsWith("#") && !line.isEmpty()) {
                    try {
                        registeredRuins.add(new RuinData(line));
                    } catch (Exception e) {
                        RuinsMod.LOGGER.error("Ruins positions file is invalid in line {}}, skipping...", lineNumber);
                    }
                }

                lineNumber++;
                line = br.readLine();
            }
            br.close();
            // System.out.println("Ruins Positions reloaded. Lines "+lineNumber+", entries "+registeredRuins.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void generateNormal(World world, Random random, int xBase, int zBase) {
        for (int c = 0; c < fileHandler.triesPerChunkNormal; c++) {
            if (random.nextFloat() * 100 < fileHandler.chanceToSpawnNormal) {
                createBuilding(world, random, xBase + random.nextInt(16), zBase + random.nextInt(16), false);
            }
        }
    }

    void generateNether(World world, Random random, int xBase, int zBase) {
        for (int c = 0; c < fileHandler.triesPerChunkNether; c++) {
            if (random.nextFloat() * 100 < fileHandler.chanceToSpawnNether) {
                createBuilding(world, random, xBase + random.nextInt(16), zBase + random.nextInt(16), true);
            }
        }
    }

    private void createBuilding(World world, Random random, int x, int z, boolean nether) {
        final int rotate = random.nextInt(4);
        final Biome biome = world.getBiome(new BlockPos(x, 0, z));
        String biomeID = biome.getRegistryName().getPath();

        if (fileHandler.useGeneric(random, biomeID)) {
            biomeID = RuinsMod.BIOME_ANY;
        }

        Integer i = stats.biomes.get(biomeID);
        if (i != null) {
            i = i + 1;
        } else {
            i = 1;
        }
        stats.biomes.put(biomeID, i);

        RuinTemplate ruinTemplate = fileHandler.getTemplate(random, biomeID);
        if (ruinTemplate == null) {
            return;
        }
        numTries++;

        int y = findSuitableY(world, ruinTemplate, x, z, nether);
        if (y > 0) {
            if (checkMinDistance(world, ruinTemplate, ruinTemplate.getRuinData(x, y, z, rotate))) {
                y = ruinTemplate.checkArea(world, x, y, z, rotate);
                if (y < 0) {
                    stats.LevelingFails++;
                    // System.out.println("checkArea fail");
                    return;
                }

                int finalY = ruinTemplate.doBuild(world, random, x, y, z, rotate, false, false);
                if (finalY >= 0) {
                    if (!fileHandler.disableLogging) {
                        RuinsMod.LOGGER.info("Creating ruin {} of Biome {} at [{}|{}|{}]\n", ruinTemplate.getName(), biome.getRegistryName().getPath(), x, y, z);
                    }
                    stats.NumCreated++;

                    registeredRuins.add(ruinTemplate.getRuinData(x, y, z, rotate));
                }
            } else {
                // System.out.println("Min Dist fail");
                stats.minDistFails++;
                return;
            }
        } else {
            // System.out.println("y fail");
            stats.LevelingFails++;
        }

        if (numTries > (LastNumTries + 1000)) {
            LastNumTries = numTries;
            printStats();
        }
    }

    private IForgeRegistry<Biome> getBiomeRegistry() {
        if (biomeRegistry == null) {
            biomeRegistry = GameRegistry.findRegistry(Biome.class);
        }
        return biomeRegistry;
    }

    private void printStats() {
        if (!fileHandler.disableLogging) {
            int total = stats.NumCreated + stats.LevelingFails;
            RuinsMod.LOGGER.info("Current Stats:");
            RuinsMod.LOGGER.info("    Total Tries:                 " + total);
            RuinsMod.LOGGER.info("    Number Created:              " + stats.NumCreated);
            RuinsMod.LOGGER.info("    Min Dist fail:               " + stats.minDistFails);
            RuinsMod.LOGGER.info("    Leveling:                    " + stats.LevelingFails);

            Biome bgb;
            for (ResourceLocation rl : getBiomeRegistry().getKeys()) {
                bgb = getBiomeRegistry().getValue(rl);
                if (bgb != null) {
                    Integer i = stats.biomes.get(bgb.getRegistryName().getPath());
                    if (i != null) {
                        RuinsMod.LOGGER.info(bgb.getRegistryName().getPath() + ": " + i + " Biome building attempts");
                    }
                }
            }
            RuinsMod.LOGGER.info("Any-Biome: " + stats.biomes.get(RuinsMod.BIOME_ANY) + " building attempts");

            RuinsMod.LOGGER.info("");
        }
    }

    private boolean checkMinDistance(World world, RuinTemplate ruinTemplate, RuinData ruinData) {
        // in overworld, check min/max distances from world spawn
        if (world.getDimension() instanceof OverworldDimension) {
            BlockPos spawn = world.getSpawnPoint();
            final int min_distance = Math.max(fileHandler.anySpawnMinDistance, ruinTemplate.spawnMinDistance);
            if (
                    ruinData.xMin - spawn.getX() < min_distance && spawn.getX() - ruinData.xMax < min_distance &&
                            ruinData.zMin - spawn.getZ() < min_distance && spawn.getZ() - ruinData.zMax < min_distance) {
                return false;
            }
            final int max_distance = Math.min(fileHandler.anySpawnMaxDistance, ruinTemplate.spawnMaxDistance);
            if (
                    ruinData.xMax - spawn.getX() > max_distance || spawn.getX() - ruinData.xMin > max_distance ||
                            ruinData.zMax - spawn.getZ() > max_distance || spawn.getZ() - ruinData.zMin > max_distance) {
                return false;
            }
        }

        //
        // We increase the bounding box by the required minimal distance
        // in each direction and check on intersections with other ruins.
        //

        int bbExtension = (int) ((ruinTemplate.uniqueMinDistance == 0) ? fileHandler.templateInstancesMinDistance : ruinTemplate.uniqueMinDistance);
        final RuinData checkSelfMinDist = new RuinData(
                ruinData.xMin - bbExtension, ruinData.xMax + bbExtension,
                ruinData.yMin - bbExtension, ruinData.yMax + bbExtension,
                ruinData.zMin - bbExtension, ruinData.zMax + bbExtension,
                ruinData.name);

        bbExtension = (int) fileHandler.anyRuinsMinDistance;
        final RuinData checkOtherMinDist = new RuinData(
                ruinData.xMin - bbExtension, ruinData.xMax + bbExtension,
                ruinData.yMin - bbExtension, ruinData.yMax + bbExtension,
                ruinData.zMin - bbExtension, ruinData.zMax + bbExtension,
                ruinData.name);

        // refuse Ruins spawning too close to each other
        boolean tooClose;
        for (RuinData r : registeredRuins) {
            if (r.name.equals(ruinData.name)) {
                tooClose = checkSelfMinDist.intersectsWith(r);
            } else {
                tooClose = checkOtherMinDist.intersectsWith(r);
            }

            if (tooClose) {
                return false;
            }
        }
        return true;
    }

    private int findSuitableY(World world, RuinTemplate r, int x, int z, boolean nether) {
        if (!nether) {
            for (int y = WORLD_MAX_HEIGHT - 1; y > 7; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!world.isBlockPresent(pos)) {
                    return -1;
                }
                final BlockState b = world.getBlockState(pos);
                if (r.isIgnoredBlock(b)) {
                    continue;
                }

                if (r.isAcceptableSurface(b)) {
                    return y + 1;
                }
                return -1;
            }
        } else {
            /*
             * The Nether has an entirely different topography so we'll use two
             * methods in a semi-random fashion (since we're not getting the
             * random here)
             */
            if ((x % 2 == 1) ^ (z % 2 == 1)) {
                // from the top. Find the first air block from the ceiling
                for (int y = WORLD_MAX_HEIGHT - 1; y > -1; y--) {
                    BlockPos basePos = new BlockPos(x, y, z);
                    if (!world.isBlockPresent(basePos)) {
                        return -1;
                    }
                    final BlockState b = world.getBlockState(basePos);
                    if (b.getBlock() == Blocks.AIR) {
                        // now find the first non-air block from here
                        for (; y > -1; y--) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (!r.isIgnoredBlock(world.getBlockState(pos))) {
                                if (r.isAcceptableSurface(b)) {
                                    return y + 1;
                                }
                                return -1;
                            }
                        }
                    }
                }
            } else {
                // from the bottom. find the first air block from the floor
                boolean accept = false;
                for (int y = 0; y < WORLD_MAX_HEIGHT; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.isBlockPresent(pos)) {
                        return -1;
                    }
                    final BlockState b = world.getBlockState(pos);
                    if (!r.isIgnoredBlock(b)) {
                        accept = r.isAcceptableSurface(b);
                    } else {
                        return accept ? y : -1;
                    }
                }
            }
        }
        return -1;
    }

    private class LoadThread extends Thread {
        @Override
        public void run() {
            // prevent conflict with flush operation
            synchronized (ruinsDataFile) {
                loadPosFile(ruinsDataFile);
            }
        }
    }

    private class FlushThread extends Thread {
        @Override
        public void run() {
            try {
                doFlush();
            } finally {
                // clear flush-in-progress flag regardless of outcome
                flushing.set(false);
            }
        }

        private void doFlush() {
            if (ruinsDataFileWriting.exists()) {
                if (!ruinsDataFileWriting.delete()) {
                    throw new RuntimeException("Ruins crashed trying to access file " + ruinsDataFileWriting);
                }
            }

            try {
                if (!ruinsDataFileWriting.createNewFile()) {
                    System.err.println("Ruins could not create new file: " + ruinsDataFileWriting.getAbsolutePath());
                }
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(ruinsDataFileWriting)));
                pw.println("# Ruins data management file. Below, you see all data accumulated by AtomicStrykers Ruins during the last run of this World.");
                pw.println("# Data is noted as follows: Each line stands for one successfull Ruin spawn. Data syntax is:");
                pw.println("# xMin yMin zMin xMax yMax zMax templateName");
                pw.println("# everything but the last value is an integer value. Template name equals the template file name.");
                pw.println("#");
                pw.println("# DO NOT EDIT THIS FILE UNLESS YOU ARE SURE OF WHAT YOU ARE DOING");
                pw.println("#");
                pw.println("# The primary function of this file is to lock areas you do not want Ruins spawning in. Put them here before worldgen.");
                pw.println("# It should also prevent Ruins re-spawning under any circumstances. Areas registered in here block any overlapping new Ruins.");
                pw.println("# Empty lines and those prefixed by '#' are ignored by the parser. Don't save notes in here, file gets wiped upon flushing.");
                pw.println("#");
                for (RuinData r : registeredRuins) {
                    pw.println(r.toString());
                    // RuinsMod.LOGGER.info("saved ruin data line ["+r.toString()+"]");
                }

                pw.flush();
                pw.close();
                // RuinsMod.LOGGER.info("Ruins Positions flushed, entries "+registeredRuins.size());

                // prevent conflict with load operation
                synchronized (ruinsDataFile) {
                    if (ruinsDataFile.exists()) {
                        if (!ruinsDataFile.delete()) {
                            throw new RuntimeException("Ruins crashed trying to access file " + ruinsDataFileWriting);
                        }
                    }
                    if (!ruinsDataFileWriting.renameTo(ruinsDataFile)) {
                        throw new RuntimeException("Ruins crashed trying to access file " + ruinsDataFileWriting);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}