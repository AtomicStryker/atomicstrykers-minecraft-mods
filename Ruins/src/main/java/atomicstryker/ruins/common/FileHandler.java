package atomicstryker.ruins.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import com.google.common.io.Files;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

class FileHandler
{
    private final static int COUNT = 0, WEIGHT = 1, CHANCE = 2;
    private final HashMap<String, HashSet<RuinTemplate>> templates = new HashMap<>();
    private final int dimension;
    private final HashMap<String, int[]> vars = new HashMap<>();

    int triesPerChunkNormal = 6, triesPerChunkNether = 6;
    float chanceToSpawnNormal = 10, chanceToSpawnNether = 10;
    private int[] allowedDimensions = { -1, 0, 1 };

    public boolean loaded;
    boolean disableLogging;
    final File saveFolder;

    public float templateInstancesMinDistance = 75f;
    float anyRuinsMinDistance = 0f;
    static final HashSet<Block> registeredTEBlocks = new HashSet<>();

    private int templateCount;

    public FileHandler(File worldPath, int dim)
    {
        saveFolder = worldPath;
        loaded = false;
        templateCount = 0;
        dimension = dim;
        new LoaderThread().start();
    }

    private class LoaderThread extends Thread
    {
        @Override
        public void run()
        {
            PrintWriter pw;
            File basedir;
            try
            {
                basedir = RuinsMod.getMinecraftBaseDir();
            }
            catch (Exception e)
            {
                System.err.println("Could not access the main Minecraft directory; error: " + e);
                System.err.println("The ruins mod could not be loaded.");
                e.printStackTrace();
                loaded = true;
                return;
            }
            try
            {
                File log = new File(basedir, "logs/ruins_log_dim_" + dimension + ".txt");
                if (log.exists())
                {
                    if (!log.delete() || !log.createNewFile())
                    {
                        throw new RuntimeException("Ruins crashed trying to access file: " + log.getAbsolutePath());
                    }
                }
                pw = new PrintWriter(new BufferedWriter(new FileWriter(log)));
            }
            catch (Exception e)
            {
                System.err.println("There was an error when creating the log file.");
                System.err.println("The ruins mod could not be loaded.");
                e.printStackTrace();
                loaded = true;
                return;
            }

            final File templPath = new File(basedir, "mods/resources/ruins");
            if (!templPath.exists())
            {
                System.out.println("Could not access the resources path for the ruins templates, file doesn't exist!");
                System.err.println("The ruins mod could not be loaded.");
                pw.close();
                loaded = true;
                return;
            }

            try
            {
                // load in the generic templates
                // pw.println("Loading the generic ruins templates...");
                HashSet<RuinTemplate> set = new HashSet<>();
                templates.put(RuinsMod.BIOME_ANY, set);
                addRuins(pw, new File(templPath, RuinsMod.BIOME_ANY), RuinsMod.BIOME_ANY, set);
                int[] val = new int[3];
                val[COUNT] = templates.get(RuinsMod.BIOME_ANY).size();
                vars.put(RuinsMod.BIOME_ANY, val);
                recalcBiomeWeight(RuinsMod.BIOME_ANY);
            }
            catch (Exception e)
            {
                printErrorToLog(pw, e, "There was an error when loading the generic ruins templates:");
            }

            /*
             * dynamic Biome config loader, gets all information straight from
             * Biome
             */
            Biome bgb;
            for (ResourceLocation rl : Biome.REGISTRY.getKeys())
            {
                bgb = Biome.REGISTRY.getObject(rl);
                if (bgb != null)
                {
                    try
                    {
                        loadSpecificTemplates(pw, templPath, bgb.getBiomeName());
                        // pw.println("Loaded " + bgb.biomeName + " ruins templates, biomeID " + bgb.biomeID);
                    }
                    catch (Exception e)
                    {
                        printErrorToLog(pw, e, "There was an error when loading the " + bgb.getBiomeName() + " ruins templates:");
                    }
                }
            }

            /*
             * Now load in the main options file. All of these will revert to
             * defaults if the file could not be loaded.
             */
            try
            {
                pw.println();
                pw.println("Loading options from: " + saveFolder.getCanonicalPath());
                readPerWorldOptions(saveFolder, pw);
            }
            catch (Exception e)
            {
                printErrorToLog(pw, e, "There was an error when loading the options file.  Defaults will be used instead.");
            }

            registeredTEBlocks.add(Blocks.CHAIN_COMMAND_BLOCK);
            registeredTEBlocks.add(Blocks.REPEATING_COMMAND_BLOCK);
            loaded = true;
            pw.println("Ruins mod loaded successfully for world " + saveFolder + ", template files: " + templateCount);
            pw.flush();
            pw.close();
        }
    }

    RuinTemplate getTemplate(Random random, String biome)
    {
        try
        {
            int rand = random.nextInt(vars.get(biome)[WEIGHT]);
            int oldval = 0, increment = 0;
            RuinTemplate retval = null;
            for (RuinTemplate ruinTemplate : templates.get(biome))
            {
                retval = ruinTemplate;
                increment += retval.getWeight();
                if ((oldval <= rand) && (rand < increment))
                {
                    return retval;
                }
                oldval += retval.getWeight();
            }
            return retval;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    boolean useGeneric(Random random, String biome)
    {
        int[] val = vars.get(biome);
        return RuinsMod.BIOME_ANY.equals(biome) || (val != null && random.nextInt(100) + 1 >= val[CHANCE]);
    }

    private void loadSpecificTemplates(PrintWriter pw, File dir, String bname) throws Exception
    {
        // pw.println("Loading the " + bname + " ruins templates...");
        pw.flush();
        File path_biome = new File(dir, bname);
        HashSet<RuinTemplate> set = new HashSet<>();
        templates.put(bname, set);
        addRuins(pw, path_biome, bname, set);
        int[] val = new int[3];
        val[COUNT] = templates.get(bname).size();
        vars.put(bname, val);
        recalcBiomeWeight(bname);
    }

    private void printErrorToLog(PrintWriter pw, Exception e, String msg)
    {
        pw.println();
        pw.println(msg);
        e.printStackTrace(pw);
        pw.flush();
    }

    private void recalcBiomeWeight(String biomeName)
    {
        final Iterator<RuinTemplate> i = templates.get(biomeName).iterator();
        int[] val = vars.get(biomeName);
        val[WEIGHT] = 0;
        while (i.hasNext())
        {
            val[WEIGHT] += i.next().getWeight();
        }
        vars.put(biomeName, val);
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
            if (check[0].equals("tries_per_chunk_normal"))
            {
                triesPerChunkNormal = Integer.parseInt(check[1]);
                ruinsLog.println("tries_per_chunk_normal = " + triesPerChunkNormal);
            }
            if (check[0].equals("chance_to_spawn_normal"))
            {
                chanceToSpawnNormal = Float.parseFloat(check[1]);
                ruinsLog.println("chance_to_spawn_normal = " + chanceToSpawnNormal);
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
                ruinsLog.println("templateInstancesMinDistance = " + templateInstancesMinDistance);
            }
            if (check[0].equals("anyRuinsMinDistance"))
            {
                anyRuinsMinDistance = Float.parseFloat(check[1]);
                ruinsLog.println("anyRuinsMinDistance = " + anyRuinsMinDistance);
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
                for (String b : blocks)
                {
                    Block bl = Block.REGISTRY.getObject(new ResourceLocation(b));
                    if (bl != Blocks.AIR)
                    {
                        registeredTEBlocks.add(bl);
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
                    Biome bgb;
                    for (ResourceLocation rl : Biome.REGISTRY.getKeys())
                    {
                        bgb = Biome.REGISTRY.getObject(rl);
                        if (bgb != null && bgb.getBiomeName().equals(check[0]))
                        {
                            int[] val = vars.get(bgb.getBiomeName());
                            if (val != null)
                            {
                                val[CHANCE] = Integer.parseInt(check[1]);
                                found = true;
                                vars.put(bgb.getBiomeName(), val);
                                break;
                            }
                        }
                    }

                    if (!found && !disableLogging)
                    {
                        System.out.println("Did not find Matching Biome for config string: [" + check[0] + "]");
                    }
                }
            }

            read = br.readLine();
        }
        br.close();
    }

    private void addRuins(PrintWriter pw, File path, String name, HashSet<RuinTemplate> targetList)
    {
        RuinTemplate r;
        Biome bgb;
        File[] listFiles = path.listFiles();
        if (listFiles != null)
        {
            for (File f : listFiles)
            {
                try
                {
                    r = new RuinTemplate(pw, f.getCanonicalPath(), f.getName());
                    targetList.add(r);
                    for (String biomeName : r.getBiomesToSpawnIn())
                    {
                        for (ResourceLocation rl : Biome.REGISTRY.getKeys())
                        {
                            bgb = Biome.REGISTRY.getObject(rl);
                            if (bgb != null && bgb.getBiomeName().equals(biomeName))
                            {
                                if (!biomeName.equals(name))
                                {
                                    templates.get(name).add(r);
                                }
                            }
                        }
                    }
                    // pw.println("Successfully loaded template " + f.getName() + " with weight " + r.getWeight() + ".");
                    templateCount++;
                }
                catch (Exception e)
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

    boolean allowsDimension(int dimensionId)
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

    private void copyGlobalOptionsTo(File dir) throws Exception
    {
        File copyfile = new File(dir, "ruins.txt");
        if (copyfile.exists())
        {
            return;
        }
        File configdir = new File(RuinsMod.getMinecraftBaseDir(), "config");
        File basefile = new File(configdir, "ruins.txt");
        if (!basefile.exists())
        {
            createDefaultGlobalOptions(configdir);
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
        // print all the biomes!
        Biome bgb;
        for (ResourceLocation rl : Biome.REGISTRY.getKeys())
        {
            bgb = Biome.REGISTRY.getObject(rl);
            if (bgb != null)
            {
                pw.println("specific_" + bgb.getBiomeName() + "=75");
            }
        }
        pw.flush();
        pw.close();
    }

}