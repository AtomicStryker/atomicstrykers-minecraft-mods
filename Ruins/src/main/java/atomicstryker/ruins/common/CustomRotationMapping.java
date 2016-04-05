package atomicstryker.ruins.common;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Class to save and retrieve custom Rotational Mappings into. A rotation is
 * determined by blockID, desired direction, and current metadata value. For
 * full coverage, all possible combinations of direction and current metadata
 * need to be mapped.
 * <p>
 * See RuinTemplateRule.rotateMetadata for how it works.
 *
 * @author AtomicStryker
 */
class CustomRotationMapping
{

    private static CustomRotationMapping instance;

    /**
     * Maps a blockID to another Map containing the direction Map
     */
    @SuppressWarnings("rawtypes")
    private final TreeMap<Block, TreeMap[]> blockIDMap;

    private final ArrayList<Block> currentBlockIDs;

    public CustomRotationMapping(File fRuinsResources)
    {
        instance = this;
        blockIDMap = new TreeMap<>(new BlockComparator());
        currentBlockIDs = new ArrayList<>();

        File f = new File(fRuinsResources, "rotation_mappings.txt");
        if (!f.exists())
        {
            System.err.println("Did not find a custom mappings file " + f.getAbsolutePath());
        }
        else
        {
            loadCustomMappings(f, new PrintWriter(System.out, true));
        }
    }

    private class BlockComparator implements Comparator<Block>
    {
        @Override
        public int compare(Block b1, Block b2)
        {
            return b1.getUnlocalizedName().compareTo(b2.getUnlocalizedName());
        }
    }

    @SuppressWarnings("unchecked")
    public static int getMapping(Block blockID, int metadata, int dir)
    {
        TreeMap<Integer, Integer>[] bIdMap = instance.blockIDMap.get(blockID);
        if (bIdMap == null)
        {
            return metadata;
        }
        Integer i = bIdMap[dir].get(metadata);
        if (i == null)
        {
            return metadata;
        }

        return i;
    }

    private void loadCustomMappings(File mappingsFile, PrintWriter ruinsLogger)
    {
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(mappingsFile));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null)
            {
                strLine = strLine.trim();
                if (strLine.equals("") || strLine.startsWith("#"))
                {
                    continue;
                }
                if (strLine.startsWith("BlockID="))
                {
                    currentBlockIDs.clear();
                    String data = strLine.split("=")[1];
                    ruinsLogger.printf("Now reading mappings for [%s]\n", data);
                    String[] ids = data.split(";");
                    Block b;
                    for (String s : ids)
                    {
                        b = tryFindingBlockOfName(new ResourceLocation(s));
                        if (b != Blocks.air)
                        {
                            currentBlockIDs.add(b);
                        }
                        else
                        {
                            ruinsLogger.printf("[%s] was determined to be an invalid blockRegistry key?! FIX THIS\n", s);
                        }
                    }
                }
                else
                {
                    String[] val = strLine.split("-");
                    int dir = RuinsMod.DIR_NORTH;
                    if ("EAST".equals(val[0]))
                    {
                        dir = RuinsMod.DIR_EAST;
                    }
                    else if ("SOUTH".equals(val[0]))
                    {
                        dir = RuinsMod.DIR_SOUTH;
                    }
                    else if ("WEST".equals(val[0]))
                    {
                        dir = RuinsMod.DIR_WEST;
                    }

                    int metadata = Integer.parseInt(val[1]);
                    int result = Integer.parseInt(val[2]);
                    ruinsLogger.printf("Saving Mapping DIR[%d] FROM[%d] TO[%d]\n", dir, metadata, result);

                    for (Block i : currentBlockIDs)
                    {
                        putMapping(i, metadata, dir, result);
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            ruinsLogger.println("Error parsing custom mappings: " + e.getMessage());
            e.printStackTrace(ruinsLogger);
        }
    }

    @SuppressWarnings("unchecked")
    private void putMapping(Block blockID, int metadata, int dir, int result)
    {
        TreeMap<Integer, Integer>[] bIdMap = blockIDMap.get(blockID);
        if (bIdMap == null)
        {
            bIdMap = new TreeMap[4];
            for (int i = 0; i < bIdMap.length; i++)
            {
                bIdMap[i] = new TreeMap<>();
            }
            blockIDMap.put(blockID, bIdMap);
        }
        bIdMap[dir].put(metadata, result);
    }

    private Block tryFindingBlockOfName(ResourceLocation blockName)
    {
        // it returns Blocks.air when nothing is found, ok
        return Block.blockRegistry.getObject(blockName);
    }

}
