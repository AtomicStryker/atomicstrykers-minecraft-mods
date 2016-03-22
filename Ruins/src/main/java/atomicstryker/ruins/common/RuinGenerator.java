package atomicstryker.ruins.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;

public class RuinGenerator
{
    public static final int WORLD_MAX_HEIGHT = 256;
    private final static String FILENAME = "RuinsPositionsFile.txt";

    private final FileHandler fileHandler;
    private final RuinStats stats;
    private int numTries = 0, LastNumTries = 0;
    private final ArrayList<RuinData> registeredRuins;
    private File ruinsDataFile;
    private File ruinsDataFileWriting;
    private final RuinData spawnPointBlock;
    
    // indicates to waiting threads that the ruins positions file has been loaded successful
	private boolean loaded;
	// indicates to any waiting thread, that the load thread failed
    private boolean error;
    // indicates whether there are changes in ruins data to be written to ruins positions file
	private boolean modified;
	// the dimension this generator is responsible for
	private int dimension;
	// controls whether to use multi-threaded initialisation/save or not
	private boolean multithreaded = false;


    public RuinGenerator(FileHandler rh, World world)
    {
    	
    	loaded = false;
    	error = false;

        fileHandler = rh;
        multithreaded = fileHandler.isMultiThreaded();
        
        stats = new RuinStats();
        registeredRuins = new ArrayList<RuinData>();
        
        // lets create a banned area 2 chunks around the spawn
        final int minX = world.getSpawnPoint().posX - 32;
        final int minY = world.getSpawnPoint().posY - 32;
        final int minZ = world.getSpawnPoint().posZ - 32;
        spawnPointBlock = new RuinData(minX, minX+64, minY, minY+64, minZ, minZ+64, "SpawnPointBlock");
        
        ruinsDataFile = new File(rh.getSaveFolder(), FILENAME);
        ruinsDataFileWriting = new File(rh.getSaveFolder(), FILENAME + "_writing");
        
        if (ruinsDataFile.getAbsolutePath().contains(world.getWorldInfo().getWorldName()))
        {
            if (multithreaded) loadAsync();
            else loadSync();
        }
        else
        {
            System.err.println("Ruins attempted to load invalid worldname " + world.getWorldInfo().getWorldName() + " posfile");
            error = true;
        }
    }

    private void loadSync() {
    	loadPosFile(ruinsDataFile);
	}

	private void loadAsync() {
    	new LoadThread().start();
	}

	synchronized
	public void waitLoaded() throws InterruptedException {
    	while (!loaded && !error) {
    		wait();
    	}
	}

    private class LoadThread extends Thread
    {
    	LoadThread() {
    		super("ruins loading dim " + dimension);
    	}
        @Override
        public void run()
        {
            loadPosFile(ruinsDataFile);
        }
    }

    synchronized
    public void flushPosFile(String worldName)
    {
    	try {
    		// We have to wait until the load thread has finished.
    		// Otherwise we will remove the entries previously stored.
    		waitLoaded();
	    	
	        if (!modified || error || registeredRuins.isEmpty() || worldName.equals("MpServer"))
	        {
	            return;
	        }
	        FlushThread flushThread = new FlushThread();
	        if (multithreaded) flushThread.start();
	        else flushThread.run();
    	} catch (InterruptedException e) {
    		// system is shutting down before we've even initialised
    		// at least we will complain about it
    		e.printStackTrace();
    	}
    }

    private class FlushThread extends Thread
    {
    	FlushThread() {
    		super("ruins saving dim " + dimension);
    	}

		@Override
        public void run()
        {
        	synchronized (RuinGenerator.this) {
	            if (ruinsDataFileWriting.exists())
	            {
	                ruinsDataFileWriting.delete();
	            }
	
	            try
	            {
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
	                for (RuinData r : registeredRuins)
	                {
	                    pw.println(r.toString());
	                    // System.out.println("saved ruin data line ["+r.toString()+"]");
	                }
	
	                pw.flush();
	                pw.close();
	                // System.out.println("Ruins Positions flushed, entries "+registeredRuins.size());
	                
	                if (ruinsDataFile.exists())
	                {
	                    ruinsDataFile.delete();
	                }
	                ruinsDataFileWriting.renameTo(ruinsDataFile);
	                modified = false;
	            }
	            catch (Throwable e)
	            {
	                e.printStackTrace();
	            }
        	} // end synchronized
        }
    }

    synchronized
    private void loadPosFile(File file)
    {
        try
        {
            if (!file.exists())
            {
                file.createNewFile();
                
                // put it into the initial set
                registeredRuins.add(spawnPointBlock);
                modified = true;
            }
            int lineNumber = 1;
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null)
            {
                line = line.trim();
                if (!line.startsWith("#") && !line.isEmpty())
                {
                    try
                    {
                        registeredRuins.add(new RuinData(line));
                        modified = true;
                    }
                    catch (Exception e)
                    {
                        System.err.println("Ruins positions file is invalid in line " + lineNumber + ", skipping...");
                    }
                }

                lineNumber++;
                line = br.readLine();
            }
            br.close();
            loaded = true;
            // System.out.println("Ruins Positions reloaded. Lines "+lineNumber+", entries "+registeredRuins.size());
        }
        catch (Throwable e)
        {
        	// indicate error state so any flush thread does not write this state to ruins pos file
            error = true;
            e.printStackTrace();
        } 
        finally
        {
        	// release any waiting flush thread
        	if (!loaded) error = true;
        	notifyAll();
        }
        
    }

    synchronized
    public boolean generateNormal(World world, Random random, int xBase, int j, int zBase)
    {
        for (int c = 0; c < fileHandler.getTriesPerChunkNormal(); c++)
        {
            if (random.nextFloat() * 100 < fileHandler.getChanceToSpawnNormal())
            {
                createBuilding(world, random, xBase + random.nextInt(16), zBase + random.nextInt(16), false);
            }
        }
        return true;
    }

    synchronized
    public boolean generateNether(World world, Random random, int xBase, int j, int zBase)
    {
        for (int c = 0; c < fileHandler.getTriesPerChunkNether(); c++)
        {
            if (random.nextFloat() * 100 < fileHandler.getChanceToSpawnNether())
            {
                int xMod = (random.nextBoolean() ? random.nextInt(16) : 0 - random.nextInt(16));
                int zMod = (random.nextBoolean() ? random.nextInt(16) : 0 - random.nextInt(16));
                createBuilding(world, random, xBase + xMod, zBase + zMod, true);
            }
        }
        return true;
    }

    private void createBuilding(World world, Random random, int x, int z, boolean nether)
    {
    	final int rotate = random.nextInt(4);
        final BiomeGenBase biome = world.getBiomeGenForCoordsBody(x, z);
        int biomeID = biome.biomeID;

        if (fileHandler.useGeneric(random, biomeID))
        {
            biomeID = RuinsMod.BIOME_NONE;
        }
        stats.biomes[biomeID]++;
        
        RuinTemplate ruinTemplate = fileHandler.getTemplate(random, biomeID);
        if (ruinTemplate == null)
        {
            biomeID = RuinsMod.BIOME_NONE;
            ruinTemplate = fileHandler.getTemplate(random, biomeID);

            if (ruinTemplate == null)
            {
                return;
            }
        }
        numTries++;
        
        int y = findSuitableY(world, ruinTemplate, x, z, nether);
        if (y > 0)
        {
            if (checkMinDistance(ruinTemplate, ruinTemplate.getRuinData(x, y, z, rotate)))
            {
                y = ruinTemplate.checkArea(world, x, y, z, rotate);
                if (y < 0)
                {
                    stats.LevelingFails++;
                    // System.out.println("checkArea fail");
                    return;
                }
                
                if (MinecraftForge.EVENT_BUS.post(new EventRuinTemplateSpawn(world, ruinTemplate, x, y, z, rotate, false, true)))
                {
                    return;
                }
                
                if (!fileHandler.isDisableLogging())
                {
                    System.out.printf("Creating ruin %s of Biome %s at [%d|%d|%d]\n", ruinTemplate.getName(), biome.biomeName, x, y, z);
                }
                stats.NumCreated++;

                int finalY = ruinTemplate.doBuild(world, random, x, y, z, rotate);
                if (finalY > 0)
                {
                    registeredRuins.add(ruinTemplate.getRuinData(x, y, z, rotate));
                    modified = true;
                    MinecraftForge.EVENT_BUS.post(new EventRuinTemplateSpawn(world, ruinTemplate, x, finalY, z, rotate, false, false));
                }
            }
            else
            {
                // System.out.println("Min Dist fail");
                stats.minDistFails++;
                return;
            }
        }
        else
        {
            // System.out.println("y fail");
            stats.LevelingFails++;
        }

        if (numTries > (LastNumTries + 1000))
        {
            LastNumTries = numTries;
            printStats();
        }

    }

    private void printStats()
    {
        if (!fileHandler.isDisableLogging())
        {
            int total =
                    stats.NumCreated + stats.BadBlockFails + stats.LevelingFails + stats.CutInFails + stats.OverhangFails + stats.NoAirAboveFails;
            System.out.println("Current Stats:");
            System.out.println("    Total Tries:                 " + total);
            System.out.println("    Number Created:              " + stats.NumCreated);
            System.out.println("    Site Tries:                  " + stats.siteTries);
            System.out.println("    Min Dist fail:               " + stats.minDistFails);
            System.out.println("    Bad Blocks:                  " + stats.BadBlockFails);
            System.out.println("    No Leveling:                 " + stats.LevelingFails);
            System.out.println("    No Cut-In:                   " + stats.CutInFails);

            for (int i = 0; i < RuinsMod.BIOME_NONE; i++)
            {
                if (stats.biomes[i] != 0)
                {
                    System.out.println(BiomeGenBase.getBiomeGenArray()[i].biomeName + ": " + stats.biomes[i] + " Biome building attempts");
                }
            }
            System.out.println("Any-Biome: " + stats.biomes[RuinsMod.BIOME_NONE] + " building attempts");
            
            System.out.println();
        }
    }
    
    private boolean checkMinDistance(RuinTemplate ruinTemplate, RuinData ruinData)
    {
		//
		// We increase the bounding box by the required minimal distance
		// in each direction and check on intersections with other ruins.
		//
        int uniqueMinDist = (int) ((ruinTemplate.uniqueMinDistance == 0) ? fileHandler.getTemplateInstancesMinDistance() : ruinTemplate.uniqueMinDistance);
        
        int bbExtension = uniqueMinDist;
        final RuinData checkSelfMinDist = new RuinData(
				ruinData.xMin - bbExtension, ruinData.xMax + bbExtension, 
				ruinData.yMin - bbExtension, ruinData.yMax + bbExtension, 
				ruinData.zMin - bbExtension, ruinData.zMax + bbExtension, 
				ruinData.name);
        
        bbExtension = (int) fileHandler.getAnyRuinsMinDistance();
        final RuinData checkOtherMinDist = new RuinData(
				ruinData.xMin - bbExtension, ruinData.xMax + bbExtension, 
				ruinData.yMin - bbExtension, ruinData.yMax + bbExtension, 
				ruinData.zMin - bbExtension, ruinData.zMax + bbExtension, 
				ruinData.name);
        
        // refuse Ruins spawning too close to each other
    	boolean tooClose = false;
        for (RuinData r : registeredRuins)
        {
        	if (r.name.equals(ruinData.name)) 
        	{
        		tooClose = checkSelfMinDist.intersectsWith(r);
        	} else {
        		tooClose = checkOtherMinDist.intersectsWith(r);
        	}
        	
            if (tooClose)
            {
                return false;
            }
        }
        return true;
    }

    private int findSuitableY(World world, RuinTemplate r, int x, int z, boolean nether)
    {
        if (!nether)
        {
            for (int y = WORLD_MAX_HEIGHT - 1; y > 7; y--)
            {
                final Block b = world.getBlock(x, y, z);
                if (r.isIgnoredBlock(b, world, x, y, z))
                {
                    continue;
                }
                
                if (r.isAcceptableSurface(b))
                {
                    return y;
                }
                return -1;
            }
        }
        else
        {
            /*
             * The Nether has an entirely different topography so we'll use two
             * methods in a semi-random fashion (since we're not getting the
             * random here)
             */
            if ((x % 2 == 1) ^ (z % 2 == 1))
            {
                // from the top. Find the first air block from the ceiling
                for (int y = WORLD_MAX_HEIGHT - 1; y > -1; y--)
                {
                    final Block b = world.getBlock(x, y, z);
                    if (b == Blocks.air)
                    {
                        // now find the first non-air block from here
                        for (; y > -1; y--)
                        {
                            if (!r.isIgnoredBlock(world.getBlock(x, y, z), world, x, y, z))
                            {
                                if (r.isAcceptableSurface(b))
                                {
                                    return y;
                                }
                                return -1;
                            }
                        }
                    }
                }
            }
            else
            {
                // from the bottom. find the first air block from the floor
                for (int y = 0; y < WORLD_MAX_HEIGHT; y++)
                {
                    final Block b = world.getBlock(x, y, z);
                    if (!r.isIgnoredBlock(b, world, x, y, z))
                    {
                        if (r.isAcceptableSurface(b))
                        {
                            return y - 1;
                        }
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

}