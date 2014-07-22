package atomicstryker.ruins.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import cpw.mods.fml.common.registry.GameData;

public class RuinTemplate
{

    private final String name;
    private Block[] acceptedSurfaces, deniedSurfaces;
    private int height = 0, width = 0, length = 0, overhang = 0, weight = 1, embed = 0, randomOffMin = 0, randomOffMax = 0;
    private int leveling = 2, lbuffer = 0, w_off = 0, l_off = 0;
    private boolean preserveWater = false, preserveLava = false;
    private final ArrayList<RuinTemplateRule> rules;
    private final ArrayList<RuinTemplateLayer> layers;
    private final HashSet<String> biomes;
    private final PrintWriter debugPrinter;
    private final boolean debugging;
    
    public RuinTemplate(PrintWriter out, String filename, String simpleName, boolean debug) throws Exception
    {
        // load in the given file as a template
        name = simpleName;
        debugPrinter = out;
        debugging = debug;
        ArrayList<String> lines = new ArrayList<String>();
        rules = new ArrayList<RuinTemplateRule>();
        layers = new ArrayList<RuinTemplateLayer>();
        biomes = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String read = br.readLine();
        while (read != null)
        {
            lines.add(read);
            read = br.readLine();
        }
        parseFile(lines);
        br.close();
    }

    public RuinTemplate(PrintWriter out, String filename, String simpleName) throws Exception
    {
        this(out, filename, simpleName, false);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof RuinTemplate)
        {
            return ((RuinTemplate) o).name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    public String getName()
    {
        return name;
    }

    public int getWeight()
    {
        return weight;
    }

    public HashSet<String> getBiomesToSpawnIn()
    {
        return biomes;
    }

    public int getMinDistance()
    {
        return (width > length ? width : length) + lbuffer;
    }

    public boolean isIgnoredBlock(Block blockID, World world, int x, int y, int z)
    {
        if (blockID == Blocks.air)
        {
            return true;
        }
        // treat snow and most plants as air too
        if (blockID == Blocks.snow || blockID == Blocks.web)
        {
            return true;
        }
        if (isPlant(blockID, world, x, y, z))
        {
            return true;
        }

        return preserveBlock(blockID, world, x, y, z);
    }
    
    private boolean isPlant(Block blockID, World world, int x, int y, int z)
    {
        return blockID instanceof IShearable || blockID instanceof BlockBush || blockID instanceof IPlantable || blockID.isLeaves(world, x, y, z)
                || blockID.isWood(world, x, y, z);
    }

    public boolean preserveBlock(Block blockID, World world, int x, int y, int z)
    {
        if (preserveWater)
        {
            if (blockID == Blocks.flowing_water)
            {
                return true;
            }
            if (blockID == Blocks.water)
            {
                return true;
            }
            if (blockID == Blocks.ice)
            {
                return true;
            }
        }
        if (preserveLava)
        {
            if (blockID == Blocks.flowing_lava)
            {
                return true;
            }
            if (blockID == Blocks.lava)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isAcceptableSurface(Block id)
    {
        for (Block b : deniedSurfaces)
        {
            if (id == b)
            {
                return false;
            }
        }
        
        if (acceptedSurfaces.length == 0)
        {
            return true;
        }
        
        for (Block b : acceptedSurfaces)
        {
            if (id == b)
            {
                return true;
            }
        }
        return false;
    }

    public int checkArea(World world, int xBase, int y, int zBase, int rotate)
    {
        // setup some variable defaults (north/south)
        int x = xBase + w_off;
        int z = zBase + l_off;
        int xDim = width;
        int zDim = length;

        // how are we oriented?
        if (rotate == RuinsMod.DIR_EAST || rotate == RuinsMod.DIR_WEST)
        {
            // reorient for east/west rotation
            x = xBase + l_off;
            z = zBase + w_off;
            xDim = length;
            zDim = width;
        }
        
        // guess the top Y coordinate of the structure box, for checking top to bottom
        final int topYguess = y + height - embed;
        
        // set a lowest height value at which surface search is aborted
        final int minimalCheckedY = y - height - embed;
        
        // surface heights of the proposed site, -1 means 'out of range, consider overhang'
        final int[][] heightMap = new int[xDim][zDim];
        
        Block curBlock;
        final int lastX = x+xDim;
        final int lastZ = z+zDim;
        
        for (int ix = x; ix < lastX; ix++)
        {
            for (int iz = z; iz < lastZ; iz++)
            {
                // check guessed structure box top to bottom, find surface
                boolean foundSurface = false;
                for (int iy = topYguess; iy >= minimalCheckedY; iy--)
                {
                    curBlock = world.getBlock(ix, iy, iz);
                    if (!isIgnoredBlock(curBlock, world, ix, iy, iz))
                    {
                        if (isAcceptableSurface(curBlock))
                        {
                            heightMap[ix-x][iz-z] = iy;
                            foundSurface = true;
                            break;
                        }
                        else
                        {
                            // ran into unwanted surface? abort
                            return -1;
                        }
                    }
                }
                if (!foundSurface)
                {
                    heightMap[ix-x][iz-z] = -1;
                }
            }
        }
        
        // now compute a better y for the structure from the found surface heights
        int sum = 0;
        int vals = 0;
        for (int[] row : heightMap)
        {
            for (int value : row)
            {
                if (value > 0)
                {
                    vals++;
                    sum += value;
                }
            }
        }
        final int newY = vals > 0 ? (int) Math.ceil(sum/vals) : y;
        if (newY > y)
        {
            // TODO if the structure box just moved up, should check the top end of the box again for new obstructions
        }
        
        // check if the resulting levelling and overhang in the build site surface is acceptable
        int localOverhang = overhang;
        for (int[] row : heightMap)
        {
            for (int value : row)
            {
                if (value < 0)
                {
                    if (--localOverhang < 0)
                    {
                        // too much overhang, abort
                        return -1;
                    }
                }
                else if (Math.abs(newY - value) > leveling)
                {
                    // too much surface noise, abort
                    return -1;
                }
            }
        }

        // looks like a good spot!
        return newY;
    }

    public RuinData getRuinData(int x, int y, int z, int rotate)
    {
        int add = lbuffer;
        int xMin = 0, xMax = 0, zMin = 0, zMax = 0;
        if ((rotate == RuinsMod.DIR_EAST) || (rotate == RuinsMod.DIR_WEST))
        {
            xMin = x + l_off - add;
            xMax = xMin + length + add;
            zMin = z + w_off - add;
            zMax = zMin + width + add;
        }
        else
        {
            xMin = x + w_off - add;
            xMax = xMin + width + add;
            zMin = z + l_off - add;
            zMax = zMin + length + add;
        }
        return new RuinData(xMin, xMax, y, y + height, zMin, zMax, name);
    }

    public void doBuild(World world, Random random, int xBase, int yBase, int zBase, int rotate)
    {
        /*
         * we need to shift the base coordinates and take care of any rotations
         * before we can begin creating the layers.
         */
        int x, z, xDim, zDim;
        boolean eastwest;
        RuinTemplateLayer curlayer;
        RuinTemplateRule curRule;
        
        // height sanity check
        final int y = Math.max(Math.min(yBase, world.getActualHeight()-height), 8);
        
        // initialize all these variables
        final ArrayList<RuinRuleProcess> laterun = new ArrayList<RuinRuleProcess>();
        final ArrayList<RuinRuleProcess> lastrun = new ArrayList<RuinRuleProcess>();
        final Iterator<RuinTemplateLayer> i = layers.iterator();
        
        int y_off = (1 - embed) + ((randomOffMax != randomOffMin) ? random.nextInt(randomOffMax - randomOffMin) : 0) + randomOffMin;

        if ((rotate == RuinsMod.DIR_EAST) || (rotate == RuinsMod.DIR_WEST))
        {
            eastwest = true;
            x = xBase + l_off;
            xDim = length;
            z = zBase + w_off;
            zDim = width;
        }
        else
        {
            eastwest = false;
            x = xBase + w_off;
            xDim = width;
            z = zBase + l_off;
            zDim = length;
        }

        // do any site leveling needed
        if (leveling > 0 && lbuffer >= 0)
        {
            levelSite(world, world.getBlock(xBase, y, zBase), xBase, y, zBase, eastwest);
        }

        int rulenum;
        // the main loop
        while (i.hasNext())
        {
            curlayer = i.next();
            for (int x1 = 0; x1 < xDim; x1++)
            {
                for (int z1 = 0; z1 < zDim; z1++)
                {
                    switch (rotate)
                    {
                    case RuinsMod.DIR_EAST:
                        // rulenum = curlayer.getRuleAt( z1, length - ( x1 + 1 )
                        // );
                        rulenum = curlayer.getRuleAt(z1, xDim - (x1 + 1));
                        break;
                    case RuinsMod.DIR_SOUTH:
                        rulenum = curlayer.getRuleAt(xDim - (x1 + 1), zDim - (z1 + 1));
                        break;
                    case RuinsMod.DIR_WEST:
                        // rulenum = curlayer.getRuleAt( width - ( z1 + 1 ), x1
                        // );
                        rulenum = curlayer.getRuleAt(zDim - (z1 + 1), x1);
                        break;
                    default:
                        rulenum = curlayer.getRuleAt(x1, z1);
                        break;
                    }
                    curRule = rules.get(rulenum);
                    if (curRule.runLater())
                    {
                        laterun.add(new RuinRuleProcess(curRule, x + x1, y + y_off, z + z1, rotate));
                        world.setBlock(x + x1, y + y_off, z + z1, Blocks.air, 0, 0);
                    }
                    else if (curRule.runLast())
                    {
                        lastrun.add(new RuinRuleProcess(curRule, x + x1, y + y_off, z + z1, rotate));
                        world.setBlock(x + x1, y + y_off, z + z1, Blocks.air, 0, 0);
                    }
                    else
                    {
                        curRule.doBlock(world, random, x + x1, y + y_off, z + z1, rotate);
                    }
                }
            }

            // we're done with this layer
            y_off++;
        }
        
        // get the late runs and finish up
        doLateRuns(world, random, laterun, lastrun);
        
        for (int x1 = 0; x1 < xDim; x1++)
        {
            for (int z1 = 0; z1 < zDim; z1++)
            {
                for (int y1 = 0; y1 < layers.size(); y1++)
                {
                    int xv = x+x1;
                    int yv = y+y1;
                    int zv = z+z1;
                    world.markBlockForUpdate(xv, yv, zv);
                    world.notifyBlockChange(xv, yv, zv, world.getBlock(xv, yv, zv));
                }
            }
        }
    }

    private void doLateRuns(World world, Random random, ArrayList<RuinRuleProcess> laterun, ArrayList<RuinRuleProcess> lastrun)
    {
        for (RuinRuleProcess rp : laterun)
        {
            rp.doBlock(world, random);
        }
        
        for (RuinRuleProcess rp : lastrun)
        {
            rp.doBlock(world, random);
        }
    }

    private void levelSite(World world, Block fillBlockID, int xBase, int y, int zBase, boolean eastwest)
    {
        /*
         * Add blocks around the build site to level it in as needed. setup some
         * variable defaults (north/south)
         */
        int x = xBase + w_off - lbuffer;
        int z = zBase + l_off - lbuffer;
        int xDim = width + 2 * lbuffer;
        int zDim = length + 2 * lbuffer;

        // how are we oriented?
        if (eastwest)
        {
            // reorient for east/west rotation
            x = xBase + l_off - lbuffer;
            z = zBase + w_off - lbuffer;
            xDim = length + 2 * lbuffer;
            zDim = width + 2 * lbuffer;
        }
        
        final int lastX = x+xDim;
        final int lastZ = z+zDim;
        final int lastY = y+leveling;
        for (int xi = x; xi < lastX; xi++)
        {
            for (int zi = z; zi < lastZ; zi++)
            {
                // fill holes
                for (int yi = y-leveling; yi <= y; yi++)
                {
                    if (isIgnoredBlock(world.getBlock(xi, yi, zi), world, xi, yi, zi))
                    {
                        world.setBlock(xi, yi, zi, fillBlockID, 0, 3);
                    }
                }
                // flatten bumps
                for (int yi = y+1; yi <= lastY; yi++)
                {
                    if (!isIgnoredBlock(world.getBlock(xi, yi, zi), world, xi, yi, zi))
                    {
                        world.setBlock(xi, yi, zi, Blocks.air, 0, 3);
                    }
                }
            }
        }
    }

    private void parseFile(ArrayList<String> lines) throws Exception
    {
        // first get the variables.
        parseVariables(lines);

        // the first rule added will always be the air block rule.
        // rules.add( new RuinTemplateRule( "0,100,0" ) );
        rules.add(new RuinRuleAir(debugPrinter, this, ""));

        // now get the rest of the data
        final Iterator<String> i = lines.iterator();
        String line;
        int ruleCount = -1;
        while (i.hasNext())
        {
            line = i.next();
            if (!line.startsWith("#") && !line.isEmpty())
            {
                if (line.startsWith("layer"))
                {
                    ruleCount = rules.size();
                    // add in data until we reach the end of the layer
                    ArrayList<String> layerlines = new ArrayList<String>();
                    line = i.next();
                    while (!line.startsWith("endlayer"))
                    {
                        if (line.charAt(0) != '#')
                        {
                            layerlines.add(line);
                        }
                        line = i.next();
                    }
                    layers.add(new RuinTemplateLayer(layerlines, width, length, ruleCount));
                }
                else if (line.startsWith("rule"))
                {
                    if (ruleCount != -1)
                    {
                        throw new Exception("Template file problem: A Rule was defined after a layer! Define all rules before the first layer!");
                    }
                    else
                    {
                        String[] parts = line.split("=");
                        rules.add(new RuinTemplateRule(debugPrinter, this, parts[1], debugging));
                    }
                }
            }
        }
    }

    private void parseVariables(ArrayList<String> variables) throws Exception
    {
        Iterator<String> i = variables.iterator();
        String line;
        while (i.hasNext())
        {
            line = i.next();
            if (!line.startsWith("#"))
            {
                if (line.startsWith("acceptable_target_blocks"))
                {
                    String[] check = line.split("=");
                    if (check.length > 1)
                    {
                        check = check[1].split(",");
                        final HashSet<Block> acceptables = new HashSet<Block>();
                        Block b;
                        for (int x = 0; x < check.length; x++)
                        {
                            b = GameData.getBlockRegistry().getObject(check[x]);
                            if (b != Blocks.air)
                            {
                                acceptables.add(b);
                            }
                        }
                        
                        acceptedSurfaces = new Block[acceptables.size()];
                        acceptedSurfaces = acceptables.toArray(acceptedSurfaces);
                    }
                }
                else if (line.startsWith("unacceptable_target_blocks"))
                {
                    String[] check = line.split("=");
                    if (check.length > 1)
                    {
                        check = check[1].split(",");
                        final HashSet<Block> inacceptables = new HashSet<Block>();
                        Block b;
                        for (int x = 0; x < check.length; x++)
                        {
                            b = GameData.getBlockRegistry().getObject(check[x]);
                            if (b != Blocks.air)
                            {
                                inacceptables.add(b);
                            }
                        }
                        
                        deniedSurfaces = new Block[inacceptables.size()];
                        deniedSurfaces = inacceptables.toArray(deniedSurfaces);
                    }
                }
                else if (line.startsWith("dimensions"))
                {
                    String[] check = line.split("=");
                    check = check[1].split(",");
                    height = Integer.parseInt(check[0]);
                    width = Integer.parseInt(check[1]);
                    length = Integer.parseInt(check[2]);
                }
                else if (line.startsWith("biomesToSpawnIn"))
                {
                    for (String s : line.split("=")[1].split(","))
                    {
                        biomes.add(s);
                    }
                }
                else if (line.startsWith("weight"))
                {
                    String[] check = line.split("=");
                    weight = Integer.parseInt(check[1]);
                }
                else if (line.startsWith("embed_into_distance"))
                {
                    String[] check = line.split("=");
                    embed = Integer.parseInt(check[1]);
                }
                else if (line.startsWith("allowable_overhang"))
                {
                    String[] check = line.split("=");
                    overhang = Integer.parseInt(check[1]);
                }
                else if (line.startsWith("max_leveling"))
                {
                    String[] check = line.split("=");
                    leveling = Integer.parseInt(check[1]);
                }
                else if (line.startsWith("leveling_buffer"))
                {
                    String[] check = line.split("=");
                    lbuffer = Integer.parseInt(check[1]);
                    if (lbuffer > 5)
                    {
                        lbuffer = 5;
                    }
                }
                else if (line.startsWith("preserve_water"))
                {
                    String[] check = line.split("=");
                    if (Integer.parseInt(check[1]) == 1)
                    {
                        preserveWater = true;
                    }
                }
                else if (line.startsWith("preserve_lava"))
                {
                    String[] check = line.split("=");
                    if (Integer.parseInt(check[1]) == 1)
                    {
                        preserveLava = true;
                    }
                }
                else if (line.startsWith("random_height_offset"))
                {
                    /*
                     * random_height_offset=-10,0 Moves the ruin down up to 10
                     * blocks.
                     */
                    String[] check = line.split("=");
                    String[] bounds = check[1].split(",");
                    randomOffMin = Integer.parseInt(bounds[0]);
                    randomOffMax = Math.max(randomOffMin, Integer.parseInt(bounds[1]));
                }
            }
        }
        
        if (acceptedSurfaces == null)
        {
            acceptedSurfaces = new Block[0];
        }
        if (deniedSurfaces == null)
        {
            deniedSurfaces = new Block[0];
        }
        
        if (width % 2 == 1)
        {
            w_off = 0 - (width - 1) / 2;
        }
        else
        {
            w_off = 0 - width / 2;
        }
        if (length % 2 == 1)
        {
            l_off = 0 - (length - 1) / 2;
        }
        else
        {
            l_off = 0 - length / 2;
        }
    }

}