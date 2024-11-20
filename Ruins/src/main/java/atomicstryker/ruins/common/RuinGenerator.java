package atomicstryker.ruins.common;


import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

class RuinGenerator {

    // google says world height is between 320 and -64, max height has a getter
    static final int WORLD_MIN_HEIGHT = -64;
    private final static String fileName = "RuinsPositionsFile.txt";

    private final FileHandler fileHandler;
    private final RuinStats stats;
    private final ConcurrentSkipListSet<RuinData> registeredRuins;
    private final File ruinsDataFile;
    private final File ruinsDataFileWriting;
    private int numTries = 0, LastNumTries = 0;
    private AtomicBoolean flushing;

    public RuinGenerator(FileHandler rh, Level world) {
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

    void generateNormal(Level world, RandomSource random, int xBase, int zBase) {
        for (int c = 0; c < fileHandler.triesPerChunkNormal; c++) {
            createBuilding(world, random, xBase + random.nextInt(16), zBase + random.nextInt(16), false);
        }
    }

    void generateNether(Level world, RandomSource random, int xBase, int zBase) {
        for (int c = 0; c < fileHandler.triesPerChunkNether; c++) {
            createBuilding(world, random, xBase + random.nextInt(16), zBase + random.nextInt(16), true);
        }
    }

    private void createBuilding(Level world, RandomSource random, int x, int z, boolean nether) {
        final int rotate = random.nextInt(4);
        // note in 1.19+ a chunk can contain different biomes at different heights ... we usually want the surface
        String biomeID = world.getBiome(new BlockPos(x, world.getSeaLevel(), z)).unwrapKey().get().location().getPath();
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
        if (y > world.getMinBuildHeight()) {
            if (checkMinDistance(world, ruinTemplate, ruinTemplate.getRuinData(x, y, z, rotate))) {
                y = ruinTemplate.checkArea(world, x, y, z, rotate);
                if (y < world.getMinBuildHeight()) {
                    stats.levelingFails++;
                    return;
                }

                int finalY = ruinTemplate.doBuild(world, random, x, y, z, rotate, false, false);
                if (finalY > world.getMinBuildHeight()) {
                    if (!fileHandler.disableLogging) {
                        RuinsMod.LOGGER.info("Creating ruin {} of Biome {} at [{}|{}|{}]\n", ruinTemplate.getName(), biomeID, x, y, z);
                    }
                    stats.numCreated++;

                    registeredRuins.add(ruinTemplate.getRuinData(x, y, z, rotate));
                }
            } else {
                stats.minDistFails++;
                return;
            }
        } else {
            stats.noSurfaceFails++;
        }

        if (numTries > (LastNumTries + 5000)) {
            LastNumTries = numTries;
            printStats();
        }
    }

    private void printStats() {
        if (!fileHandler.disableLogging) {
            int total = stats.numCreated + stats.levelingFails;
            RuinsMod.LOGGER.info("Current Stats:");
            RuinsMod.LOGGER.info("    Total Tries:                 " + total);
            RuinsMod.LOGGER.info("    Number Created:              " + stats.numCreated);
            RuinsMod.LOGGER.info("    Min Dist fails:              " + stats.minDistFails);
            RuinsMod.LOGGER.info("    No Surface fails:            " + stats.noSurfaceFails);
            RuinsMod.LOGGER.info("    Leveling fails:              " + stats.levelingFails);

            for (Map.Entry<ResourceKey<Biome>, Biome> entry : ForgeRegistries.BIOMES.getEntries()) {
                Biome biome = entry.getValue();
                if (biome != null) {
                    Integer i = stats.biomes.get(ForgeRegistries.BIOMES.getKey(biome).getPath());
                    if (i != null) {
                        RuinsMod.LOGGER.info(ForgeRegistries.BIOMES.getKey(biome).getPath() + ": " + i + " Biome building attempts");
                    }
                }
            }
            RuinsMod.LOGGER.info("Any-Biome: " + stats.biomes.get(RuinsMod.BIOME_ANY) + " building attempts");

            RuinsMod.LOGGER.info("");
        }
    }

    private boolean checkMinDistance(Level world, RuinTemplate ruinTemplate, RuinData ruinData) {
        // in overworld, check min/max distances from world spawn
        if (world.dimension().location().getPath().equals("overworld")) {
            BlockPos spawn = new BlockPos(world.getLevelData().getXSpawn(), world.getLevelData().getYSpawn(), world.getLevelData().getZSpawn());
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

    private int findSuitableY(Level world, RuinTemplate r, int x, int z, boolean nether) {
        if (!nether) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int y = world.getMaxBuildHeight() - 1; y > WORLD_MIN_HEIGHT; y--) {
                pos.set(x, y, z);
                final BlockState b = world.getBlockState(pos);
                if (b.is(Blocks.BEDROCK)) {
                    return world.getMinBuildHeight() - 1;
                }
                if (r.isIgnoredBlock(b)) {
                    continue;
                }

                if (r.isAcceptableSurface(world, b, pos)) {
                    return y + 1;
                }
            }
            // how did we reach here? no bedrock?
            return world.getMinBuildHeight() - 1;
        } else {
            /*
             * The Nether has an entirely different topography so we'll use two
             * methods in a semi-random fashion (since we're not getting the
             * random here)
             */
            if ((x % 2 == 1) ^ (z % 2 == 1)) {
                // from the top. Find the first air block from the ceiling
                for (int y = world.getMaxBuildHeight() - 1; y > WORLD_MIN_HEIGHT; y--) {
                    BlockPos basePos = new BlockPos(x, y, z);
                    final BlockState b = world.getBlockState(basePos);
                    if (b.is(Blocks.BEDROCK)) {
                        return world.getMinBuildHeight() - 1;
                    }
                    if (b.is(Blocks.AIR)) {
                        // now find the first non-air block from here
                        for (; y > WORLD_MIN_HEIGHT; y--) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (!r.isIgnoredBlock(world.getBlockState(pos))) {
                                if (r.isAcceptableSurface(world, b, pos)) {
                                    return y + 1;
                                }
                                return world.getMinBuildHeight() - 1;
                            }
                        }
                    }
                }
            } else {
                // from the bottom. find the first air block from the floor
                boolean accept = false;
                for (int y = 0; y < world.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    final BlockState b = world.getBlockState(pos);
                    if (b.is(Blocks.BEDROCK)) {
                        return world.getMinBuildHeight() - 1;
                    }
                    if (!r.isIgnoredBlock(b)) {
                        accept = r.isAcceptableSurface(world, b, pos);
                    } else {
                        return accept ? y : world.getMinBuildHeight() - 1;
                    }
                }
            }
        }
        return world.getMinBuildHeight() - 1;
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