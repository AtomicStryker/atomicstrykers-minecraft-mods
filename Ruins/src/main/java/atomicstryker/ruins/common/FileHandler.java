package atomicstryker.ruins.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import com.google.common.io.Files;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

public class FileHandler
{
    private final static int COUNT = 0, WEIGHT = 1, CHANCE = 2;
    private final ArrayList<HashSet<RuinTemplate>> templates = new ArrayList<HashSet<RuinTemplate>>();
    private final ArrayList<Exclude> excluded = new ArrayList<Exclude>();
    private int[][] vars;

    private int triesPerChunkNormal = 6, triesPerChunkNether = 6;
    private float chanceToSpawnNormal = 10, chanceToSpawnNether = 10;
    private int[] allowedDimensions = {-1, 0, 1};

    private boolean disableLogging;
    private File saveFolder;
    
    private float templateInstancesMinDistance = 75f;
    private float anyRuinsMinDistance = 0f;
    private static HashSet<Block> registeredTEBlocks = new HashSet<Block>();
    
    private int templateCount;

    /** Ruins config entry. Indicates whether to use sync or async loading/storing of templates. 
     * Default: enabled*/
    private boolean multithreaded = true;
    
    /** indicates whether configuration and templates have been successfully loaded. */
    private boolean loaded;

    /** indicates whether a critical error occurred during initialisation. */
    private boolean error;
    
    /** log print stream during initialisation */
	private PrintWriter log;
	
	/** minecraft base directory (aka. workDir) */
	private File mcBasedir;

    FileHandler(File worldPath)
    {
        saveFolder =  worldPath;
        templateCount = 0;
        loaded = false;

        //
        // Initialisation is split into a synchronous part, initialising 
        // essentials, and an optionally asynchronous part, loading templates.
        //
        error = !initEssentials();
        if (!error) {
	        if (multithreaded) loadAsync();
	        else loadSync();
        }
    }

    /**
     * This method initialises the log and loads the configuration.
     * @return Indicates whether initialisation was successful.
     */
    private boolean initEssentials() {
        try
        {
            mcBasedir = new File(RuinsMod.getMinecraftBaseDir(), "mods");
        }
        catch (Throwable e)
        {
            System.err.println("Could not access the main Minecraft mods directory; error: " + e);
            System.err.println("The ruins mod could not be loaded.");
            e.printStackTrace();
            return false;
        }
        
        try
        {
            File logFile = new File(mcBasedir, "ruins_log.txt");
            if (logFile.exists())
            {
                logFile.delete();
                logFile.createNewFile();
            }
            log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
        }
        catch (Throwable e)
        {
            System.err.println("There was an error when creating the log file.");
            System.err.println("The ruins mod could not be loaded.");
            e.printStackTrace();
            return false;
        }
        
        
        try {
	    	// create the vars array fitting to the number of Biomes present
	        int biomeAmountPlusOne = RuinsMod.BIOME_NONE + 1;
	        vars = new int[3][biomeAmountPlusOne];
	        for (int j = 0; j < vars[0].length; j++)
	        {
	            vars[CHANCE][j] = 75;
	        }
	
	        // initialise the template arraylist
	        for (int fill = 0; fill < biomeAmountPlusOne; fill++)
	        {
	            templates.add(new HashSet<RuinTemplate>());
	        }
        } catch (Throwable e) {
        	printErrorToLog(e, "failed to instantiate structures for biome specific data.");
        	return false;
        }

        
        /*
         * Now load in the main options file. All of these will revert to
         * defaults if the file could not be loaded.
         */
        try
        {
            log.println();
            log.println("Loading options from: " + getSaveFolder().getCanonicalPath());
            readPerWorldOptions(getSaveFolder(), log);
        }
        catch (Throwable e)
        {
            printErrorToLog(e, "There was an error when loading the options file.  Defaults will be used instead.");
        }
        return true;
	}


	private void loadAsync() {
        // load asynchronously
        new Thread(){
        	public void run() {
       			loadSync();
        	}
        }.start();
    }
    
    synchronized
    private void loadSync() {
		try {
			load();
    	} finally {
    		// we need to have a guaranteed result whether its success or error.
    		if (!loaded) error = true;
    		notifyAll();
    	}
    }
    
    
    
    synchronized
    private void load()
    {

        final File templPath = new File(mcBasedir, "resources/ruins");
        if (!templPath.exists())
        {
            error = true;
            System.out.println("Could not access the resources path for the ruins templates, file doesn't exist!");
            System.err.println("The ruins mod could not be loaded.");
            log.close();
            return;
        }

        try
        {
            // load in the generic templates
            // pw.println("Loading the generic ruins templates...");
            addRuins(log, new File(templPath, "generic"), RuinsMod.BIOME_NONE);
            vars[COUNT][RuinsMod.BIOME_NONE] = templates.get(RuinsMod.BIOME_NONE).size();
            recalcBiomeWeight(RuinsMod.BIOME_NONE);
        }
        catch (Throwable e)
        {
            printErrorToLog(e, "There was an error when loading the generic ruins templates:");
        }

        
        /*
         * dynamic Biome config loader, gets all information straight from
         * BiomeGenBase
         */
        BiomeGenBase bgb;
        for (int x = 0; x < BiomeGenBase.getBiomeGenArray().length; x++)
        {
            bgb = BiomeGenBase.getBiomeGenArray()[x];
            if (bgb != null)
            {
                try
                {
                    loadSpecificTemplates(log, templPath, bgb.biomeID, bgb.biomeName);
                    // log.println("Loaded " + bgb.biomeName + " ruins templates, biomeID " + bgb.biomeID);
                }
                catch (Throwable e)
                {
                    printErrorToLog(e, "There was an error when loading the " + bgb.biomeName + " ruins templates:");
                }
            }
        }

        /*
         * Find and load the excluded file. If this does not exist, no
         * worries.
         */
        try
        {
            log.println();
            log.println("Loading excluded list from: " + getSaveFolder().getCanonicalPath());
            readExclusions(getSaveFolder(), log);
        }
        catch (Throwable e)
        {
            log.println("No exclusions found for this world.");
        }

        loaded = true;

        log.println("Ruins mod loaded successfully for world "+getSaveFolder()+", template files: "+templateCount);
        log.close(); // close flushes
    }


    synchronized
    public void waitLoaded() throws InterruptedException {
    	while(!loaded && !error) {
    		wait();
    	}
	}

    synchronized
    public RuinTemplate getTemplate(Random random, int biome)
    {
        try
        {
            int rand = random.nextInt(vars[WEIGHT][biome]);
            int oldval = 0, increment = 0;
            RuinTemplate retval = null;
            final Iterator<RuinTemplate> i = templates.get(biome).iterator();
            while (i.hasNext())
            {
                retval = i.next();
                increment += retval.getWeight();
                if ((oldval <= rand) && (rand < increment))
                {
                    return retval;
                }
                oldval += retval.getWeight();
            }
            return retval;
        }
        catch (Throwable e)
        {
            return null;
        }
    }

	public boolean useGeneric(Random random, int biome)
    {
        return biome == RuinsMod.BIOME_NONE || random.nextInt(100) + 1 >= vars[CHANCE][biome];
    }

    public void removeTemplate(String name, int biome)
    {
        /*
         * removes a ruin from the specified biome, providing support for unique
         * templates.
         */
        final Iterator<RuinTemplate> i = templates.get(biome).iterator();
        RuinTemplate rem = null;
        boolean found = false;
        while (i.hasNext())
        {
            rem = i.next();
            if (rem.getName().equals(name))
            {
                found = true;
                break;
            }
        }
        if (found)
        {
            templates.get(biome).remove(rem);
            excluded.add(new Exclude(name, biome));
            recalcBiomeWeight(biome);
        }
    }

    private void writeExclusions(File dir) throws Exception
    {
        final File file = new File(dir, "excl.txt");
        final PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        final Iterator<Exclude> i = excluded.iterator();
        while (i.hasNext())
        {
            pw.println(i.next().toString());
        }
        pw.flush();
        pw.close();
    }

    private void loadSpecificTemplates(PrintWriter pw, File dir, int biome, String bname) throws Exception
    {
        bname = bname.toLowerCase();
        // pw.println("Loading the " + bname + " ruins templates...");
        pw.flush();
        File path_biome = new File(dir, bname);
        addRuins(pw, path_biome, biome);
        vars[COUNT][biome] = templates.get(biome).size();
        recalcBiomeWeight(biome);
    }

    private void printErrorToLog(Throwable e, String msg)
    {
        log.println();
        log.println(msg);
        e.printStackTrace(log);
        log.flush();
    }

    private void recalcBiomeWeight(int biome)
    {
        final Iterator<RuinTemplate> i = templates.get(biome).iterator();
        vars[WEIGHT][biome] = 0;
        while (i.hasNext())
        {
            vars[WEIGHT][biome] += i.next().getWeight();
        }
    }

    private void readPerWorldOptions(File dir, PrintWriter ruinsLog) throws Exception
    {
        final File file = new File(dir, "ruins.txt");
        if (!file.exists())
        {
            copyGlobalOptionsTo(dir);
        }
        final BufferedReader br = new BufferedReader(new FileReader(file));
        String read = br.readLine();
        String[] check;
        while (read != null)
        {
            check = read.split("=");
            if (check[0].equals("multi_threaded"))
            {
            	int v;
            	try {
            		v = Integer.parseInt(check[1]);
            	} catch (Throwable e) {
            		log.println("error reading value for config property multi_threaded");
            		e.printStackTrace(log);
            		v = 1;
            	}
                multithreaded = (v == 0 ? false : true);
                ruinsLog.println("multi_threaded = " + multithreaded);
            }
            if (check[0].equals("tries_per_chunk_normal"))
            {
                triesPerChunkNormal = Integer.parseInt(check[1]);
                ruinsLog.println("tries_per_chunk_normal = "+triesPerChunkNormal);
            }
            if (check[0].equals("chance_to_spawn_normal"))
            {
                chanceToSpawnNormal = Float.parseFloat(check[1]);
                ruinsLog.println("chance_to_spawn_normal = "+chanceToSpawnNormal);
            }
            if (check[0].equals("tries_per_chunk_nether"))
            {
                triesPerChunkNether = Integer.parseInt(check[1]);
            }
            if (check[0].equals("chance_to_spawn_nether"))
            {
                chanceToSpawnNether = Float.parseFloat(check[1]);
            }
            if (check[0].equals("disableRuinSpawnCoordsLogging"))
            {
                disableLogging = Boolean.parseBoolean(check[1]);
            }
            if (check[0].equals("templateInstancesMinDistance"))
            {
                templateInstancesMinDistance = Float.parseFloat(check[1]);
                ruinsLog.println("templateInstancesMinDistance = "+templateInstancesMinDistance);
            }
            if (check[0].equals("anyRuinsMinDistance"))
            {
                anyRuinsMinDistance = Float.parseFloat(check[1]);
                ruinsLog.println("anyRuinsMinDistance = "+anyRuinsMinDistance);
            }
            if (check[0].equals("allowedDimensions") && check.length > 1)
            {
                String[] ints = check[1].split(",");
                allowedDimensions = new int[ints.length];
                for (int i = 0; i < ints.length; i++)
                {
                    allowedDimensions[i] = Integer.parseInt(ints[i]);
                }
            }
            if (check[0].equals("teblocks") && check.length > 1)
            {
                String[] blocks = check[1].split(",");
                synchronized (registeredTEBlocks) {
	                for (String b : blocks)
	                {
	                    Block bl = GameData.getBlockRegistry().getObject(b);
	                    if (bl != Blocks.air)
	                    {
	                        registeredTEBlocks.add(bl);
	                    }
	                }
                }
            }

            if (read.startsWith("specific_"))
            {
                read = read.split("_")[1];
                check = read.split("=");
                if (check.length > 1)
                {
                    boolean found = false;
                    for (int i = 0; i < BiomeGenBase.getBiomeGenArray().length; i++)
                    {
                        if (BiomeGenBase.getBiomeGenArray()[i] != null && BiomeGenBase.getBiomeGenArray()[i].biomeName.equalsIgnoreCase(check[0]))
                        {
                            vars[CHANCE][i] = Integer.parseInt(check[1]);
                            found = true;
                            break;
                        }
                    }

                    if (!found && !isDisableLogging())
                    {
                        System.out.println("Did not find Matching Biome for config string: [" + check[0] + "]");
                    }
                }
            }

            read = br.readLine();
        }
        br.close();
    }

    private void readExclusions(File dir, PrintWriter pw) throws Exception
    {
        final File file = new File(dir, "excl.txt");
        final BufferedReader br = new BufferedReader(new FileReader(file));
        String read = br.readLine();
        String[] check;

        while (read != null)
        {
            if (read.startsWith("excl="))
            {
                check = read.split("=");
                if (check.length > 1)
                {
                    check = check[1].split(";");
                    int biome = Integer.parseInt(check[0]);
                    removeTemplate(check[1], biome);
                    pw.println("Excluded from biome " + BiomeGenBase.getBiomeGenArray()[biome].biomeName + ": " + check[1]);
                }
            }
            read = br.readLine();
        }
        br.close();
    }

    private void addRuins(PrintWriter pw, File path, int biomeID) throws Exception
    {
        final HashSet<RuinTemplate> targetList = templates.get(biomeID);
        RuinTemplate r;
        String candidate;
        BiomeGenBase bgb;

        if (path.listFiles() != null)
        {
            for (File f : path.listFiles())
            {
                try
                {
                    r = new RuinTemplate(pw, f.getCanonicalPath(), f.getName());
                    targetList.add(r);
                    for (String biomeName : ((RuinTemplate) r).getBiomesToSpawnIn())
                    {
                        for (int x = 0; x < BiomeGenBase.getBiomeGenArray().length; x++)
                        {
                            bgb = BiomeGenBase.getBiomeGenArray()[x];
                            if (bgb != null)
                            {
                                candidate = bgb.biomeName.toLowerCase();
                                if (candidate.equals(biomeName))
                                {
                                    if (bgb.biomeID != biomeID)
                                    {
                                        templates.get(x).add(r);
                                        // pw.println("template " + f.getName() + "also registered for Biome " + bgb.biomeName);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    // pw.println("Successfully loaded template " + f.getName() + " with weight " + r.getWeight() + ".");
                    templateCount++;
                }
                catch (Throwable e)
                {
                    pw.println();
                    pw.println("There was a problem loading the file: " + f.getName());
                    e.printStackTrace(pw);
                }
            }
        }
        else
        {
            pw.println("Did not find any Building data for " + path + ", creating empty folder for it: " + (path.mkdir() ? "success" : "failed"));
        }
        pw.flush();
        
    }
    
    public boolean allowsDimension(int dimensionId)
    {
        for (int i : allowedDimensions)
        {
            if (i == dimensionId)
            {
                return true;
            }
        }
        return false;
    }

    private class Exclude
    {
        protected String name;
        protected int biome;

        public Exclude(String n, int b)
        {
            name = n;
            biome = b;
        }

        public String toString()
        {
            return "excl=" + biome + ";" + name;
        }
    }
    
    private void copyGlobalOptionsTo(File dir) throws Exception
    {
        File copyfile = new File(dir, "ruins.txt");
        if (copyfile.exists())
        {
            return;
        }
        File modsdir = new File(RuinsMod.getMinecraftBaseDir(), "mods");
        File basefile = new File(modsdir, "ruins.txt");
        if (!basefile.exists())
        {
            createDefaultGlobalOptions(modsdir);
        }
        Files.copy(basefile, copyfile);
    }

    private void createDefaultGlobalOptions(File dir) throws Exception
    {
        File file = new File(dir, "ruins.txt");
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        pw.println("# Global Options for the Ruins mod");
        pw.println("#");
        pw.println("# tries_per_chunk is the number of times, per chunk, that the generator will");
        pw.println("#     attempt to create a ruin.");
        pw.println("#");
        pw.println("# chance_to_spawn is the chance, out of 100, that a ruin will be generated per");
        pw.println("#     try in this chunk.  This may still fail if the ruin does not have a");
        pw.println("#     suitable place to generate.");
        pw.println("#");
        pw.println("# chance_for_site is the chance, out of 100, that another ruin will attempt to");
        pw.println("#     spawn nearby if a ruin was already successfully spawned.  This bypasses");
        pw.println("#     the normal tries per chunk, so if this chance is set high you may end up");
        pw.println("#     with a lot of ruins even with a low tries per chunk and chance to spawn.");
        pw.println("#");
        pw.println("# specific_<biome name> is the chance, out of 100, that a ruin spawning in the");
        pw.println("#     specified biome will be chosen from the biome specific folder.  If not,");
        pw.println("#     it will choose a generic ruin from the folder of the same name.");
        pw.println();
        pw.println("tries_per_chunk_normal=6");
        pw.println("chance_to_spawn_normal=10.0");
        pw.println("chance_for_site_normal=15.0");
        pw.println();
        pw.println("tries_per_chunk_nether=6");
        pw.println("chance_to_spawn_nether=10");
        pw.println("chance_for_site_nether=15");
        pw.println("disableRuinSpawnCoordsLogging=true");
        pw.println();
        pw.println("# minimum distance a template must have from instances of itself");
        pw.println("templateInstancesMinDistance=256");
        pw.println("# minimum distance a template must have from any other template");
        pw.println("anyRuinsMinDistance=64");
        pw.println("# dimension IDs whitelisted for ruins spawning, add custom dimensions IDs here as needed");
        pw.println("allowedDimensions=0,1,-1");
        pw.println();
        pw.println("# tileentity blocks, those (nonvanilla)blocks which cannot function without storing their nbt data, full name as stick dictates, seperated by commata");
        pw.println("teblocks=");
        pw.println();
        pw.println("# whether to use asynchronous initialisation and save or not.");
        pw.println("# 1 := enabled, 0 := disabled.");
        pw.println("multi_threaded=1");
        pw.println();

        // print all the biomes!
        for (int i = 0; i < BiomeGenBase.getBiomeGenArray().length; i++)
        {
            if (BiomeGenBase.getBiomeGenArray()[i] != null)
            {
                pw.println("specific_" + BiomeGenBase.getBiomeGenArray()[i].biomeName + "=75");
            }
        }
        pw.flush();
        pw.close();
    }

	public float getAnyRuinsMinDistance() {
		return anyRuinsMinDistance;
	}

	public float getTemplateInstancesMinDistance() {
		return templateInstancesMinDistance;
	}

	public boolean isDisableLogging() {
		return disableLogging;
	}

	public int getTriesPerChunkNormal() {
		return triesPerChunkNormal;
	}

	public float getChanceToSpawnNormal() {
		return chanceToSpawnNormal;
	}

	public int getTriesPerChunkNether() {
		return triesPerChunkNether;
	}

	public float getChanceToSpawnNether() {
		return chanceToSpawnNether;
	}

	public HashSet<Block> getRegisteredTEBlocks() {
		return registeredTEBlocks;
	}

	public File getSaveFolder() {
		return saveFolder;
	}

	public boolean isMultiThreaded() {
		return multithreaded;
	}
	
	public static boolean isRegisteredTEBlock(Block block) {
		synchronized (FileHandler.registeredTEBlocks) {
			return FileHandler.registeredTEBlocks.contains(block);
		}
	}


    
}