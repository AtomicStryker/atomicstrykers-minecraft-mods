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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    boolean disableLogging = true;
    final File saveFolder;

    public float templateInstancesMinDistance = 256;
    float anyRuinsMinDistance = 64;
    public int anySpawnMinDistance = 32;
    public int anySpawnMaxDistance = Integer.MAX_VALUE;
    public boolean enableStick = true;
    public static boolean enableFixedWidthRuleIds = false;
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

            final File templPath = new File(basedir, RuinsMod.TEMPLATE_PATH_MC_EXTRACTED);
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
                        loadSpecificTemplates(pw, templPath, bgb.getRegistryName().getResourcePath());
                        // pw.println("Loaded " + bgb.biomeName + " ruins templates, biomeID " + bgb.biomeID);
                    }
                    catch (Exception e)
                    {
                        printErrorToLog(pw, e, "There was an error when loading the " + bgb.getRegistryName().getResourcePath() + " ruins templates:");
                    }
                }
            }

            // after all templates are loaded, calculate biome template counts and weights
            for (String bname: templates.keySet())
            {
                int[] val = new int[3];
                val[COUNT] = templates.get(bname).size();
                vars.put(bname, val);
                recalcBiomeWeight(bname);
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
        // if no template entry for this biome, create (empty) one
        // may already exist if this biome appeared in earlier biomesToSpawnIn list
        if (!templates.containsKey(bname))
        {
            templates.put(bname, new HashSet<>());
        }
        HashSet<RuinTemplate> set = templates.get(bname);
        addRuins(pw, path_biome, bname, set);
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

    private static final Pattern patternSpecificBiome = Pattern.compile("\\s*specific_(\\w+)\\s*=\\s*(\\w+)\\s*(?:#|$)");

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
            Matcher matcher = null;
            check = read.split("=");
            if (check[0].equals("tries_per_chunk_normal"))
            {
                triesPerChunkNormal = Integer.parseInt(check[1]);
                ruinsLog.println("tries_per_chunk_normal = " + triesPerChunkNormal);
            }
            else if (check[0].equals("chance_to_spawn_normal"))
            {
                chanceToSpawnNormal = Float.parseFloat(check[1]);
                ruinsLog.println("chance_to_spawn_normal = " + chanceToSpawnNormal);
            }
            else if (check[0].equals("tries_per_chunk_nether"))
            {
                triesPerChunkNether = Integer.parseInt(check[1]);
            }
            else if (check[0].equals("chance_to_spawn_nether"))
            {
                chanceToSpawnNether = Float.parseFloat(check[1]);
            }
            else if (check[0].equals("disableRuinSpawnCoordsLogging"))
            {
                disableLogging = Boolean.parseBoolean(check[1]);
            }
            else if (check[0].equals("templateInstancesMinDistance"))
            {
                templateInstancesMinDistance = Float.parseFloat(check[1]);
                ruinsLog.println("templateInstancesMinDistance = " + templateInstancesMinDistance);
            }
            else if (check[0].equals("anyRuinsMinDistance"))
            {
                anyRuinsMinDistance = Float.parseFloat(check[1]);
                ruinsLog.println("anyRuinsMinDistance = " + anyRuinsMinDistance);
            }
            else if (check[0].equals("anySpawnMinDistance"))
            {
                final int value = Integer.parseInt(check[1]);
                anySpawnMinDistance = value > 0 ? value : 0;
                ruinsLog.println("anySpawnMinDistance = " + anySpawnMinDistance);
            }
            else if (check[0].equals("anySpawnMaxDistance"))
            {
                final int value = Integer.parseInt(check[1]);
                anySpawnMaxDistance = value > 0 ? value : Integer.MAX_VALUE;
                ruinsLog.println("anySpawnMaxDistance = " + anySpawnMaxDistance);
            }
            else if (check[0].equals("enableStick"))
            {
                enableStick = Boolean.parseBoolean(check[1]);
            }
            else if (check[0].equals("allowedDimensions") && check.length > 1)
            {
                String[] ints = check[1].split(",");
                allowedDimensions = new int[ints.length];
                for (int i = 0; i < ints.length; i++)
                {
                    allowedDimensions[i] = Integer.parseInt(ints[i]);
                }
            }
            else if (dimension == 0 && check[0].equals("enableFixedWidthRuleIds"))
            {
                enableFixedWidthRuleIds = Boolean.parseBoolean(check[1]);
            }
            else if (check[0].equals("teblocks") && check.length > 1)
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
            else if ((matcher = patternSpecificBiome.matcher(read)).lookingAt())
            {
                boolean found = false;
                Biome bgb;
                for (ResourceLocation rl : Biome.REGISTRY.getKeys())
                {
                    bgb = Biome.REGISTRY.getObject(rl);
                    if (bgb != null && bgb.getRegistryName().getResourcePath().equals(matcher.group(1)))
                    {
                        int[] val = vars.get(bgb.getRegistryName().getResourcePath());
                        if (val != null)
                        {
                            val[CHANCE] = Integer.parseInt(matcher.group(2));
                            found = true;
                            vars.put(bgb.getRegistryName().getResourcePath(), val);
                            break;
                        }
                    }
                }

                if (!found && !disableLogging)
                {
                    System.out.println("Did not find Matching Biome for config string: [" + matcher.group(1) + "]");
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
                            if (bgb != null && bgb.getRegistryName().getResourcePath().equals(biomeName))
                            {
                                if (!biomeName.equals(name))
                                {
                                    // if no template entry for this biome, create (empty) one
                                    if (!templates.containsKey(biomeName))
                                    {
                                        templates.put(biomeName, new HashSet<>());
                                    }
                                    templates.get(biomeName).add(r);
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
        pw.println("# specific_<biome name> is the chance, out of 100, that a ruin spawning in the");
        pw.println("#     specified biome will be chosen from the biome specific folder.  If not,");
        pw.println("#     it will choose a generic ruin from the folder of the same name.");
        pw.println();
        pw.println("tries_per_chunk_normal=6");
        pw.println("chance_to_spawn_normal=10");
        pw.println();
        pw.println("tries_per_chunk_nether=6");
        pw.println("chance_to_spawn_nether=10");
        pw.println("# prevent a message from being logged every time a ruin is built");
        pw.println("disableRuinSpawnCoordsLogging=true");
        pw.println();
        pw.println("# minimum distance a template must have from instances of itself");
        pw.println("templateInstancesMinDistance=256");
        pw.println("# minimum distance a template must have from any other template");
        pw.println("anyRuinsMinDistance=64");
        pw.println("# min/max distances overworld templates can have from world spawn (0 = no limit)");
        pw.println("anySpawnMinDistance=32");
        pw.println("anySpawnMaxDistance=0");
        pw.println("# allow displaying a block's data by hitting it with a stick");
        pw.println("enableStick=true");
        pw.println("# dimension IDs whitelisted for ruins spawning, add custom dimensions IDs here as needed");
        pw.println("allowedDimensions=0,1,-1");
        pw.println();
        pw.println("# make /parseruin rule IDs line up nicely in template files");
        pw.println("# note: overworld (i.e., dimension 0) setting applies to all dimensions");
        pw.println("enableFixedWidthRuleIds=false");
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
                pw.println("specific_" + bgb.getRegistryName().getResourcePath() + "=75");
            }
        }
        pw.flush();
        pw.close();
    }

}