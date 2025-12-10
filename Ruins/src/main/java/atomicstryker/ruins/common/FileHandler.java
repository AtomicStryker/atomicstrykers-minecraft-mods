package atomicstryker.ruins.common;

import com.google.common.io.Files;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class FileHandler {

    private final static int WEIGHT = 0, CHANCE = 1;
    private final HashMap<String, HashSet<RuinTemplate>> templates = new HashMap<>();
    private final Identifier dimension;
    private final HashMap<String, double[]> vars = new HashMap<>();

    int triesPerChunkNormal = 6, triesPerChunkNether = 6;
    float chanceToSpawnNormal = 10, chanceToSpawnNether = 10;
    private String[] allowedDimensions = {"the_nether", "overworld", "the_end"};

    public boolean loaded;
    boolean disableLogging = true;
    final File saveFolder;

    public float templateInstancesMinDistance = 256;
    float anyRuinsMinDistance = 64;
    public int anySpawnMinDistance = 32;
    public int anySpawnMaxDistance = Integer.MAX_VALUE;
    public boolean enableStick = true;
    public static boolean enableFixedWidthRuleIds = false;

    private int templateCount;

    public FileHandler(File worldPath, Identifier dim) {
        saveFolder = worldPath;
        loaded = false;
        templateCount = 0;
        dimension = dim;
        new LoaderThread().start();
    }

    private class LoaderThread extends Thread {
        @Override
        public void run() {
            File basedir;
            try {
                basedir = RuinsMod.getMinecraftBaseDir();
            } catch (Exception e) {
                System.err.println("Could not access the main Minecraft directory; error: " + e);
                System.err.println("The ruins mod could not be loaded.");
                e.printStackTrace();
                loaded = true;
                return;
            }
            try {
                File log = new File(basedir, "logs/ruins_log_dim_" + dimension + ".txt");
                if (log.exists()) {
                    if (!log.delete() || !log.createNewFile()) {
                        throw new RuntimeException("Ruins crashed trying to access file: " + log.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                System.err.println("There was an error when creating the log file.");
                System.err.println("The ruins mod could not be loaded.");
                e.printStackTrace();
                loaded = true;
                return;
            }

            final File templPath = new File(basedir, RuinsMod.TEMPLATE_PATH_MC_EXTRACTED);
            if (!templPath.exists()) {
                System.out.println("Could not access the resources path for the ruins templates, file doesn't exist!");
                System.err.println("The ruins mod could not be loaded.");
                loaded = true;
                return;
            }

            try {
                // load in the generic templates
                // pw.println("Loading the generic ruins templates...");
                HashSet<RuinTemplate> set = new HashSet<>();
                templates.put(RuinsMod.BIOME_ANY, set);
                addRuins(new File(templPath, RuinsMod.BIOME_ANY), RuinsMod.BIOME_ANY, set);
            } catch (Exception e) {
                RuinsMod.LOGGER.error("There was an error when loading the generic ruins templates:", e);
            }

            /*
             * dynamic Biome config loader, gets all information straight from
             * Biome
             */
            HolderLookup.RegistryLookup<Biome> biomeRegistryLookup = RuinsMod.getInstance().getLastLoadedLevel().registryAccess().lookupOrThrow(Registries.BIOME);
            Set<Holder.Reference<Biome>> biomeSet = biomeRegistryLookup.listElements().collect(Collectors.toSet());
            for (Holder.Reference<Biome> biomeReference : biomeSet) {
                try {
                    loadSpecificTemplates(templPath, biomeReference.getKey().identifier().getPath());
                    // pw.println("Loaded " + bgb.biomeName + " ruins templates, biomeID " + bgb.biomeID);
                } catch (Exception e) {
                    RuinsMod.LOGGER.error("There was an error when loading the {} ruins templates:", biomeReference.getKey().identifier().getPath(), e);
                }
            }

            // after all templates are loaded, calculate biome template weights
            for (String bname : templates.keySet()) {
                double[] val = new double[2];
                vars.put(bname, val);
                recalcBiomeWeight(bname);
            }

            /*
             * Now load in the main options file. All of these will revert to
             * defaults if the file could not be loaded.
             */
            try {
                RuinsMod.LOGGER.info("Loading options from: {}", saveFolder.getCanonicalPath());
                readPerWorldOptions(saveFolder);
            } catch (Exception e) {
                RuinsMod.LOGGER.error("There was an error when loading the options file.  Defaults will be used instead.", e);
            }

            loaded = true;
            RuinsMod.LOGGER.info("Ruins mod loaded successfully for world {} template files: {}", saveFolder, templateCount);
        }
    }

    RuinTemplate getTemplate(RandomSource random, String biome) {
        try {
            double rand = random.nextDouble() * vars.get(biome)[WEIGHT];
            RuinTemplate retval = null;
            for (RuinTemplate ruinTemplate : templates.get(biome)) {
                retval = ruinTemplate;
                if ((rand -= retval.getWeight()) < 0) {
                    break;
                }
            }
            return retval;
        } catch (Exception e) {
            return null;
        }
    }

    boolean useGeneric(RandomSource random, String biome) {
        double[] val = vars.get(biome);
        return RuinsMod.BIOME_ANY.equals(biome) || val == null || random.nextDouble() >= val[CHANCE];
    }

    private void loadSpecificTemplates(File dir, String bname) throws Exception {
        // pw.println("Loading the " + bname + " ruins templates...");
        File path_biome = new File(dir, bname);
        // if no template entry for this biome, create (empty) one
        // may already exist if this biome appeared in earlier biomesToSpawnIn list
        if (!templates.containsKey(bname)) {
            templates.put(bname, new HashSet<>());
        }
        HashSet<RuinTemplate> set = templates.get(bname);
        addRuins(path_biome, bname, set);
    }

    private void recalcBiomeWeight(String biomeName) {
        final Iterator<RuinTemplate> i = templates.get(biomeName).iterator();
        double[] val = vars.get(biomeName);
        val[WEIGHT] = 0;
        while (i.hasNext()) {
            val[WEIGHT] += i.next().getWeight();
        }
        vars.put(biomeName, val);
    }

    private static final Pattern patternSpecificBiome = Pattern.compile("specific_([^=]++)=(.++)");

    private void readPerWorldOptions(File dir) throws Exception {
        final File file = new File(dir, "ruins.txt");
        if (!file.exists()) {
            copyGlobalOptionsTo(dir);
        }
        final BufferedReader br = new BufferedReader(new FileReader(file));
        String read = br.readLine();
        String[] check;
        while (read != null) {
            Matcher matcher = null;
            check = read.split("=");
            if (check[0].equals("tries_per_chunk_normal")) {
                triesPerChunkNormal = Integer.parseInt(check[1]);
            } else if (check[0].equals("chance_to_spawn_normal")) {
                chanceToSpawnNormal = Float.parseFloat(check[1]);
            } else if (check[0].equals("tries_per_chunk_nether")) {
                triesPerChunkNether = Integer.parseInt(check[1]);
            } else if (check[0].equals("chance_to_spawn_nether")) {
                chanceToSpawnNether = Float.parseFloat(check[1]);
            } else if (check[0].equals("disableRuinSpawnCoordsLogging")) {
                disableLogging = Boolean.parseBoolean(check[1]);
            } else if (check[0].equals("templateInstancesMinDistance")) {
                templateInstancesMinDistance = Float.parseFloat(check[1]);
            } else if (check[0].equals("anyRuinsMinDistance")) {
                anyRuinsMinDistance = Float.parseFloat(check[1]);
            } else if (check[0].equals("anySpawnMinDistance")) {
                final int value = Integer.parseInt(check[1]);
                anySpawnMinDistance = Math.max(value, 0);
            } else if (check[0].equals("anySpawnMaxDistance")) {
                final int value = Integer.parseInt(check[1]);
                anySpawnMaxDistance = value > 0 ? value : Integer.MAX_VALUE;
            } else if (check[0].equals("enableStick")) {
                enableStick = Boolean.parseBoolean(check[1]);
            } else if (check[0].equals("allowedDimensions") && check.length > 1) {
                String[] strings = check[1].split(",");
                allowedDimensions = new String[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    allowedDimensions[i] = strings[i];
                }
            } else if (dimension.getPath().equals("the_nether") && check[0].equals("enableFixedWidthRuleIds")) {
                enableFixedWidthRuleIds = Boolean.parseBoolean(check[1]);
            } else if ((matcher = patternSpecificBiome.matcher(read)).matches()) {
                boolean found = false;
                HolderLookup.RegistryLookup<Biome> biomeRegistryLookup = RuinsMod.getInstance().getLastLoadedLevel().registryAccess().lookupOrThrow(Registries.BIOME);
                Set<Holder.Reference<Biome>> biomeSet = biomeRegistryLookup.listElements().collect(Collectors.toSet());
                for (Holder.Reference<Biome> biomeReference : biomeSet) {
                    Biome bgb = biomeReference.value();
                    Identifier rl = biomeReference.getKey().identifier();
                    if (bgb != null && rl.getPath().equals(matcher.group(1))) {
                        double[] val = vars.get(rl.getPath());
                        if (val != null) {
                            val[CHANCE] = Math.min(Math.max(Double.parseDouble(matcher.group(2)) / 100, 0), 1);
                            found = true;
                            vars.put(rl.getPath(), val);
                            break;
                        }
                    }
                }

                if (!found && !disableLogging) {
                    System.out.println("Did not find Matching Biome for config string: [" + matcher.group(1) + "]");
                }
            }

            read = br.readLine();
        }
        br.close();
    }

    private void addRuins(File path, String name, HashSet<RuinTemplate> targetList) {
        RuinTemplate r;
        File[] listFiles = path.listFiles();

        String dimensionName = dimension.getPath();
        if (listFiles != null) {
            for (File f : listFiles) {
                try {
                    r = new RuinTemplate(f.getCanonicalPath(), f.getName());
                    if (!r.acceptsDimension(dimensionName)) {
                        continue;
                    }
                    targetList.add(r);
                    HolderLookup.RegistryLookup<Biome> biomeRegistryLookup = RuinsMod.getInstance().getLastLoadedLevel().registryAccess().lookupOrThrow(Registries.BIOME);
                    Set<Holder.Reference<Biome>> biomeSet = biomeRegistryLookup.listElements().collect(Collectors.toSet());
                    for (String biomeName : r.getBiomesToSpawnIn()) {
                        for (Holder.Reference<Biome> biomeReference : biomeSet) {
                            Identifier rl = biomeReference.getKey().identifier();
                            if (rl.getPath().equals(biomeName)) {
                                if (!biomeName.equals(name)) {
                                    // if no template entry for this biome, create (empty) one
                                    if (!templates.containsKey(biomeName)) {
                                        templates.put(biomeName, new HashSet<>());
                                    }
                                    templates.get(biomeName).add(r);
                                }
                            }
                        }
                    }
                    RuinsMod.LOGGER.info("Successfully loaded template " + f.getName() + " with weight " + r.getWeight() + ".");
                    templateCount++;
                } catch (RuinTemplate.IncompatibleModException e) {
                    RuinsMod.LOGGER.error("IncompatibleModException", e);
                } catch (Exception e) {
                    RuinsMod.LOGGER.error("There was a problem loading the file: " + f.getName(), e);
                }
            }
        } else {
            RuinsMod.LOGGER.info("Did not find any Building data for {}, creating empty folder for it: {}", path, (path.mkdir() ? "success" : "failed"));
        }
    }

    boolean allowsDimension(String dimensionId) {
        for (String i : allowedDimensions) {
            if (i.equalsIgnoreCase(dimensionId)) {
                return true;
            }
        }
        return false;
    }

    private void copyGlobalOptionsTo(File dir) throws Exception {
        File copyfile = new File(dir, "ruins.txt");
        if (copyfile.exists()) {
            return;
        }
        File configdir = new File(RuinsMod.getMinecraftBaseDir(), "config");
        File basefile = new File(configdir, "ruins.txt");
        if (!basefile.exists()) {
            createDefaultGlobalOptions(configdir);
        }
        Files.copy(basefile, copyfile);
    }

    private void createDefaultGlobalOptions(File dir) throws Exception {
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
        pw.println("#");
        pw.println("tries_per_chunk_normal=6");
        pw.println("chance_to_spawn_normal=10");
        pw.println("tries_per_chunk_nether=6");
        pw.println("chance_to_spawn_nether=10");
        pw.println("#");
        pw.println("# prevent a message from being logged every time a ruin is built");
        pw.println("disableRuinSpawnCoordsLogging=true");
        pw.println("#");
        pw.println("# minimum distance a template must have from instances of itself");
        pw.println("templateInstancesMinDistance=256");
        pw.println("#");
        pw.println("# minimum distance a template must have from any other template");
        pw.println("anyRuinsMinDistance=64");
        pw.println("#");
        pw.println("# min/max distances overworld templates can have from world spawn (0 = no limit)");
        pw.println("anySpawnMinDistance=32");
        pw.println("anySpawnMaxDistance=0");
        pw.println("#");
        pw.println("# allow displaying a block's data by hitting it with a stick");
        pw.println("enableStick=true");
        pw.println("#");
        pw.println("# dimension IDs whitelisted for ruins spawning, add custom dimensions IDs here as needed");
        pw.println("allowedDimensions=overworld,the_end,the_nether");
        pw.println("#");
        pw.println("# make /parseruin rule IDs line up nicely in template files");
        pw.println("# note: overworld (i.e., dimension 0) setting applies to all dimensions");
        pw.println("enableFixedWidthRuleIds=false");
        pw.println("#");
        pw.println("# tileentity blocks, those (nonvanilla)blocks which cannot function without storing their nbt data, full name as stick dictates, seperated by commata");
        pw.println("teblocks=");
        pw.println();
        // print all the biomes!
        HolderLookup.RegistryLookup<Biome> biomeRegistryLookup = RuinsMod.getInstance().getLastLoadedLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        Set<Holder.Reference<Biome>> biomeSet = biomeRegistryLookup.listElements().collect(Collectors.toSet());
        for (Holder.Reference<Biome> biomeReference : biomeSet) {
            pw.println("specific_" + biomeReference.getKey().identifier().getPath() + "=75");
        }
        pw.flush();
        pw.close();
    }

}