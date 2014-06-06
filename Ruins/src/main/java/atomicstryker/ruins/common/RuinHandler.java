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

import net.minecraft.world.biome.BiomeGenBase;

public class RuinHandler
{
    private final static int COUNT = 0, WEIGHT = 1, CHANCE = 2;
    private final ArrayList<HashSet<RuinTemplate>> templates = new ArrayList<HashSet<RuinTemplate>>();
    private final ArrayList<Exclude> excluded = new ArrayList<Exclude>();
    protected int[][] vars;

    protected int triesPerChunkNormal = 6, triesPerChunkNether = 6;
    protected float chanceToSpawnNormal = 10, chanceToSpawnNether = 10, chanceForSiteNormal = 15, chanceForSiteNether = 15;

    public boolean loaded;
    public boolean disableLogging;
    public File saveFolder;
    
    public float templateInstancesMinDistance = 75f;
    public float anyRuinsMinDistance = 0f;

    public RuinHandler(File worldPath)
    {
        saveFolder = worldPath;
        loaded = false;
        new LoaderThread().start();
    }

    private class LoaderThread extends Thread
    {
        @Override
        public void run()
        {
            // create the vars array fitting to the number of Biomes present
            int biomeAmountPlusOne = RuinsMod.BIOME_NONE + 1;
            vars = new int[3][biomeAmountPlusOne];
            for (int j = 0; j < vars[0].length; j++)
            {
                vars[CHANCE][j] = 75;
            }

            // fill up the template arraylist
            for (int fill = 0; fill < biomeAmountPlusOne; fill++)
            {
                templates.add(new HashSet<RuinTemplate>());
            }

            PrintWriter pw;
            File basedir = null;
            try
            {
                basedir = RuinsMod.getMinecraftBaseDir();
                basedir = new File(basedir, "mods");
            }
            catch (Exception e)
            {
                System.err.println("Could not access the main Minecraft mods directory; error: " + e);
                System.err.println("The ruins mod could not be loaded.");
                e.printStackTrace();
                loaded = true;
                return;
            }
            try
            {
                File log = new File(basedir, "ruins_log.txt");
                if (log.exists())
                {
                    log.delete();
                    log.createNewFile();
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

            final File templPath = new File(basedir, "resources/ruins");
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
                pw.println("Loading the generic ruins templates...");
                addRuins(pw, new File(templPath, "generic"), RuinsMod.BIOME_NONE);
                vars[COUNT][RuinsMod.BIOME_NONE] = templates.get(RuinsMod.BIOME_NONE).size();
                recalcBiomeWeight(RuinsMod.BIOME_NONE);
            }
            catch (Exception e)
            {
                printErrorToLog(pw, e, "There was an error when loading the generic ruins templates:");
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
                        loadSpecificTemplates(pw, templPath, bgb.biomeID, bgb.biomeName);
                        pw.println("Loaded " + bgb.biomeName + " ruins templates, biomeID " + bgb.biomeID);
                    }
                    catch (Exception e)
                    {
                        printErrorToLog(pw, e, "There was an error when loading the " + bgb.biomeName + " ruins templates:");
                    }
                }
            }

            /*
             * Find and load the excluded file. If this does not exist, no
             * worries.
             */
            try
            {
                pw.println();
                pw.println("Loading excluded list from: " + saveFolder.getCanonicalPath());
                readExclusions(saveFolder, pw);
            }
            catch (Exception e)
            {
                pw.println("No exclusions found for this world.");
            }

            /*
             * Now load in the main options file. All of these will revert to
             * defaults if the file could not be loaded.
             */
            try
            {
                pw.println();
                pw.println("Loading options from: " + saveFolder.getCanonicalPath());
                readGlobalOptions(saveFolder);
            }
            catch (Exception e)
            {
                printErrorToLog(pw, e, "There was an error when loading the options file.  Defaults will be used instead.");
            }

            loaded = true;
            pw.println("Ruins mod loaded.");
            pw.flush();
            pw.close();
        }
    }

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
        catch (Exception e)
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

    public void writeExclusions(File dir) throws Exception
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
        pw.println();
        bname = bname.toLowerCase();
        pw.println("Loading the " + bname + " ruins templates...");
        pw.flush();
        File path_biome = new File(dir, bname);
        addRuins(pw, path_biome, biome);
        vars[COUNT][biome] = templates.get(biome).size();
        recalcBiomeWeight(biome);
    }

    private void printErrorToLog(PrintWriter pw, Exception e, String msg)
    {
        pw.println();
        pw.println(msg);
        e.printStackTrace(pw);
        pw.flush();
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

    private void readGlobalOptions(File dir) throws Exception
    {
        final File file = new File(dir, "ruins.txt");
        if (!file.exists())
        {
            RuinsMod.copyGlobalOptionsTo(dir);
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
            }
            if (check[0].equals("chance_to_spawn_normal"))
            {
                chanceToSpawnNormal = Float.parseFloat(check[1]);
            }
            if (check[0].equals("chance_for_site_normal"))
            {
                chanceForSiteNormal = Float.parseFloat(check[1]);
            }
            if (check[0].equals("tries_per_chunk_nether"))
            {
                triesPerChunkNether = Integer.parseInt(check[1]);
            }
            if (check[0].equals("chance_to_spawn_nether"))
            {
                chanceToSpawnNether = Float.parseFloat(check[1]);
            }
            if (check[0].equals("chance_for_site_nether"))
            {
                chanceForSiteNether = Float.parseFloat(check[1]);
            }
            if (check[0].equals("disableRuinSpawnCoordsLogging"))
            {
                disableLogging = Boolean.parseBoolean(check[1]);
            }
            if (check[0].equals("templateInstancesMinDistance"))
            {
                templateInstancesMinDistance = Float.parseFloat(check[1]);
            }
            if (check[0].equals("anyRuinsMinDistance"))
            {
                anyRuinsMinDistance = Float.parseFloat(check[1]);
            }

            if (read.startsWith("specific_"))
            {
                read = read.split("_")[1];
                check = read.split("=");
                boolean found = false;
                for (int i = 0; i < BiomeGenBase.getBiomeGenArray().length; i++)
                {
                    if (BiomeGenBase.getBiomeGenArray()[i] != null && BiomeGenBase.getBiomeGenArray()[i].biomeName.equalsIgnoreCase(check[0]))
                    {
                        vars[CHANCE][i] = Integer.parseInt(check[1]);
                        if (!disableLogging)
                        {
                            System.out.println("Parsed config line [" + read + "], vars[CHANCE][" + i + "] set to " + Integer.parseInt(check[1]));
                        }
                        found = true;
                        break;
                    }
                }

                if (!found && !disableLogging)
                {
                    System.out.println("Did not find Matching Biome for config string: [" + check[0] + "]");
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
                check = check[1].split(";");
                int biome = Integer.parseInt(check[0]);
                removeTemplate(check[1], biome);
                pw.println("Excluded from biome " + BiomeGenBase.getBiomeGenArray()[biome].biomeName + ": " + check[1]);
            }
            read = br.readLine();
        }
        br.close();
    }

    private void addRuins(PrintWriter pw, File path, int biomeID) throws Exception
    {
        final HashSet<RuinTemplate> targetList = templates.get(biomeID);
        RuinTemplate r;
        if (path.listFiles() != null)
        {
            for (File f : path.listFiles())
            {
                try
                {
                    switch (checkFileType(f.getName()))
                    {
                    case RuinsMod.FILE_TEMPLATE:
                        r = new RuinTemplate(pw, f.getCanonicalPath(), f.getName());
                        targetList.add(r);

                        String candidate;
                        BiomeGenBase bgb;
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
                                            pw.println("template " + f.getName() + "also registered for Biome " + bgb.biomeName);
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        pw.println("Successfully loaded template " + f.getName() + " with weight " + r.getWeight() + ".");
                        break;
                    default:
                        if (!f.isDirectory())
                        {
                            pw.println("Ignoring unknown file type: " + f.getName());
                        }
                        break;
                    }
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

    private static int checkFileType(String s)
    {
        int mid = s.lastIndexOf(".");
        String ext = s.substring(mid + 1, s.length());
        if (ext.equals(RuinsMod.TEMPLATE_EXT))
        {
            return RuinsMod.FILE_TEMPLATE;
        }
        return -1;
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
}