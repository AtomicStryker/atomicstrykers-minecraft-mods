package atomicstryker.ruins.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;

public class RuinTemplate
{
    private final String name;
    private Block[] acceptedSurfaces, deniedSurfaces;
    private int height = 0, width = 0, length = 0, overhang = 0, weight = 1, embed = 0, randomOffMin = 0, randomOffMax = 0;
    private int leveling = 2, lbuffer = 0, w_off = 0, l_off = 0;
    public int uniqueMinDistance = 0;
    private boolean preserveWater = false, preserveLava = false;
    private final VariantRuleset variantRuleset;
    private final ArrayList<RuinTemplateLayer> layers;
    private final HashSet<String> biomes;
    private final PrintWriter debugPrinter;
    private final boolean debugging;
    private boolean preventRotation = false;
    private final ArrayList<Integer> bonemealMarkers;
    private final ArrayList<AdjoiningTemplateData> adjoiningTemplates;

    private class AdjoiningTemplateData
    {
        RuinTemplate adjoiningTemplate;
        int relativeX;
        int acceptableY;
        int relativeZ;
        float spawnchance;
    }

    public RuinTemplate(PrintWriter out, String filename, String simpleName, boolean debug) throws Exception
    {
        // load in the given file as a template
        name = simpleName;
        debugPrinter = out;
        debugging = debug;
        ArrayList<String> lines = new ArrayList<>();
        variantRuleset = new VariantRuleset();
        layers = new ArrayList<>();
        biomes = new HashSet<>();
        bonemealMarkers = new ArrayList<>();
        adjoiningTemplates = new ArrayList<>();

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
        return o instanceof RuinTemplate && ((RuinTemplate) o).name.equals(name);
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

    public boolean isIgnoredBlock(Block blockID, World world, BlockPos pos)
    {
        return blockID == Blocks.AIR || blockID == Blocks.SNOW_LAYER || blockID == Blocks.WEB || isPlant(blockID, world, pos) || preserveBlock(blockID);
    }

    private boolean isPlant(Block blockID, World world, BlockPos pos)
    {
        return blockID instanceof IShearable || blockID instanceof BlockBush || blockID instanceof IPlantable || blockID.isLeaves(world.getBlockState(pos), world, pos)
                || blockID.isWood(world, pos);
    }

    public boolean preserveBlock(Block blockID)
    {
        if (preserveWater)
        {
            if (blockID == Blocks.FLOWING_WATER)
            {
                return true;
            }
            if (blockID == Blocks.WATER)
            {
                return true;
            }
            if (blockID == Blocks.ICE)
            {
                return true;
            }
        }
        if (preserveLava)
        {
            if (blockID == Blocks.FLOWING_LAVA)
            {
                return true;
            }
            if (blockID == Blocks.LAVA)
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
        return checkArea(world, xBase, y, zBase, rotate, 0);
    }

    private int checkArea(World world, int xBase, int y, int zBase, int rotate, int additionalYRangeChecked)
    {
        // setup some variable defaults (north/south)
        int x = xBase + w_off;
        int z = zBase + l_off;
        int xDim = width;
        int zDim = length;

        // how are we oriented?
        if (!preventRotation && (rotate == RuinsMod.DIR_EAST || rotate == RuinsMod.DIR_WEST))
        {
            // reorient for east/west rotation
            x = xBase + l_off;
            z = zBase + w_off;
            xDim = length;
            zDim = width;
        }

        // guess the top Y coordinate of the structure box, for checking top to bottom
        final int topYguess = y + height - embed + additionalYRangeChecked;

        // set a lowest height value at which surface search is aborted
        final int minimalCheckedY = y - height - embed - additionalYRangeChecked;

        // surface heights of the proposed site, -1 means 'out of range, consider overhang'
        final int[][] heightMap = new int[xDim][zDim];

        Block curBlock;
        final int lastX = x + xDim;
        final int lastZ = z + zDim;

        for (int ix = x; ix < lastX; ix++)
        {
            for (int iz = z; iz < lastZ; iz++)
            {
                // check guessed structure box top to bottom, find surface
                boolean foundSurface = false;
                for (int iy = topYguess; iy >= minimalCheckedY; iy--)
                {
                    BlockPos pos = new BlockPos(ix, iy, iz);
                    curBlock = world.getBlockState(pos).getBlock();
                    if (!isIgnoredBlock(curBlock, world, pos))
                    {
                        if (isAcceptableSurface(curBlock))
                        {
                            heightMap[ix - x][iz - z] = iy;
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
                    heightMap[ix - x][iz - z] = -1;
                }
            }
        }

        // now compute a better y for the structure from the found surface heights
        double sum = 0;
        double vals = 0;
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
        final int newY = vals > 0 ? (int) Math.ceil(sum / vals) : y;

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
        int add = lbuffer > 0 ? lbuffer : 0;
        int xMin, xMax, zMin, zMax;
        if ((rotate == RuinsMod.DIR_EAST) || (rotate == RuinsMod.DIR_WEST))
        {
            xMin = x + l_off - add;
            xMax = xMin + length - 1 + add;
            zMin = z + w_off - add;
            zMax = zMin + width - 1 + add;
        }
        else
        {
            xMin = x + w_off - add;
            xMax = xMin + width - 1 + add;
            zMin = z + l_off - add;
            zMax = zMin + length - 1 + add;
        }
        return new RuinData(xMin, xMax, y, y + height - 1, zMin, zMax, name);
    }

    /**
     * @return the finalized y value of the embedded template or -1 if there was an exception
     */
    public int doBuild(World world, Random random, int xBase, int yBase, int zBase, int rotate, boolean is_player)
    {
        try
        {
            return doBuildNested(world, random, xBase, yBase, zBase, rotate, is_player);
        }
        catch (Exception e)
        {
            debugPrinter.printf("An Exception was thrown while building Ruin: %s\n", getName());
            System.err.println("Faulty Template name: " + getName());
            e.printStackTrace();
            return -1;
        }
    }

    private int doBuildNested(World world, Random random, int xBase, int yBase, int zBase, int rotate, boolean is_player)
    {
        /*
         * we need to shift the base coordinates and take care of any rotations
         * before we can begin creating the layers.
         */
        int x, z, xDim, zDim;
        boolean eastwest;
        RuinTemplateLayer curlayer;
        RuinTemplateRule curRule;

        // initialize all these variables
        final ArrayList<RuinRuleProcess> laterun = new ArrayList<>();
        final ArrayList<RuinRuleProcess> lastrun = new ArrayList<>();
        final Iterator<RuinTemplateLayer> layeriter = layers.iterator();

        int y_off = (1 - embed) + ((randomOffMax > randomOffMin) ? (random.nextInt(randomOffMax - randomOffMin) + randomOffMin) : 0);

        // height sanity check
        final int yReturn = Math.max(Math.min(yBase + y_off, world.getActualHeight() - height), 8);
        final int y = yReturn - y_off;

        // override rotation wishes if its locked by template
        if (preventRotation)
        {
            rotate = RuinsMod.DIR_NORTH;
        }

        // post pre-build event after y position and rotation are resolved
        if (MinecraftForge.EVENT_BUS.post(new EventRuinTemplateSpawn(world, this, xBase, yReturn, zBase, rotate, is_player, true)))
        {
            debugPrinter.printf("Forge Event came back negative, no spawn\n");
            return -1;
        }

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

        // resolve all the variant rules
        final ArrayList<RuinTemplateRule> rules = variantRuleset.getVariants(random);

        // do any site leveling needed
        if (leveling > 0 && lbuffer >= 0)
        {
            levelSite(world, world.getBlockState(new BlockPos(xBase, yReturn, zBase)).getBlock(), xBase, yReturn, zBase, eastwest, random, rotate, rules.get(0));
        }

        int rulenum;
        // the main loop
        while (layeriter.hasNext())
        {
            curlayer = layeriter.next();
            for (int x1 = 0; x1 < xDim; x1++)
            {
                for (int z1 = 0; z1 < zDim; z1++)
                {
                    switch (rotate)
                    {
                    case RuinsMod.DIR_EAST:
                        rulenum = curlayer.getRuleAt(z1, xDim - (x1 + 1));
                        break;
                    case RuinsMod.DIR_SOUTH:
                        rulenum = curlayer.getRuleAt(xDim - (x1 + 1), zDim - (z1 + 1));
                        break;
                    case RuinsMod.DIR_WEST:
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
                        rules.get(0).doBlock(world, random, x + x1, y + y_off, z + z1, rotate);
                    }
                    else if (curRule.runLast())
                    {
                        lastrun.add(new RuinRuleProcess(curRule, x + x1, y + y_off, z + z1, rotate));
                        rules.get(0).doBlock(world, random, x + x1, y + y_off, z + z1, rotate);
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

        int xv, yv, zv;
        for (int x1 = 0; x1 < xDim; x1++)
        {
            for (int z1 = 0; z1 < zDim; z1++)
            {
                for (int y1 = 0; y1 < layers.size(); y1++)
                {
                    xv = x + x1;
                    yv = yReturn + y1;
                    zv = z + z1;
                    BlockPos pos = new BlockPos(xv, yv, zv);
                    world.markAndNotifyBlock(pos, null, Blocks.AIR.getDefaultState(), world.getBlockState(pos), 2);
                }
            }
        }

        for (int b = 0; b < bonemealMarkers.size(); b += 3)
        {
            int xi = bonemealMarkers.get(b);
            int yi = bonemealMarkers.get(b + 1);
            int zi = bonemealMarkers.get(b + 2);
            IBlockState state = world.getBlockState(new BlockPos(xi, yi, zi));
            Block growable = state.getBlock();
            debugPrinter.printf("Now considering bonemeal flag at [%d|%d|%d], block: %s\n", xi, yi, zi, growable);
            // verbatim rip of ItemDye.applyBonemeal method
            if (growable instanceof IGrowable)
            {
                IGrowable igrowable = (IGrowable) growable;
                BlockPos pos = new BlockPos(xi, yi, zi);
                if (igrowable.canGrow(world, pos, state, world.isRemote))
                {
                    igrowable.grow(world, world.rand, pos, state);
                    debugPrinter.printf("Applied bonemeal at [%d|%d|%d], block: %s\n", xi, yi, zi, growable);
                }
                else
                {
                    debugPrinter.printf("... but first, CAN_STILL_GROW, Bonemeal boolean was negative\n");
                }
            }
        }
        bonemealMarkers.clear();

        for (AdjoiningTemplateData ad : adjoiningTemplates)
        {
            debugPrinter.printf("Considering to spawn adjoining %s of Ruin %s...\n", ad.adjoiningTemplate.getName(), getName());
            float randres = (world.rand.nextFloat() * 100);
            if (randres < ad.spawnchance)
            {
                int newrot = world.rand.nextInt(4);
                int targetX = xBase + ad.relativeX;
                int targetZ = zBase + ad.relativeZ;
                int targetY = ad.adjoiningTemplate.checkArea(world, targetX, yReturn, targetZ, newrot, ad.acceptableY);
                if (targetY >= 0 && Math.abs(yReturn - targetY) <= ad.acceptableY)
                {
                    debugPrinter.printf("Creating adjoining %s of Ruin %s at [%d|%d|%d], rot:%d\n", ad.adjoiningTemplate.getName(), getName(), targetX, targetY, targetZ, newrot);
                    int finalY = ad.adjoiningTemplate.doBuild(world, random, targetX, targetY, targetZ, newrot, false);
                }
                else
                {
                    debugPrinter.printf("Adjoining area around [%d|%d|%d] was rejected, targetY:%d, diff:%d\n", targetX, yReturn, targetZ, targetY, Math.abs(yReturn - targetY));
                }
            }
            else
            {
                debugPrinter.printf("Spawnchance [%.2f] too low. Random got [%.2f], no spawn\n", ad.spawnchance, randres);
            }
        }

        MinecraftForge.EVENT_BUS.post(new EventRuinTemplateSpawn(world, this, xBase, yReturn, zBase, rotate, is_player, false));
        return yReturn;
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

    private void levelSite(World world, Block fillBlockID, int xBase, int y, int zBase, boolean eastwest, Random random, int rotate, RuinTemplateRule rule0)
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

        final int lastX = x + xDim;
        final int lastZ = z + zDim;
        final int lastY = y + leveling;
        for (int xi = x; xi < lastX; xi++)
        {
            for (int zi = z; zi < lastZ; zi++)
            {
                // fill holes
                for (int yi = y - leveling; yi <= y; yi++)
                {
                    BlockPos pos = new BlockPos(xi, yi, zi);
                    if (isIgnoredBlock(world.getBlockState(pos).getBlock(), world, pos))
                    {
                        world.setBlockState(pos, fillBlockID.getDefaultState(), 2);
                    }
                }
                // flatten bumps
                for (int yi = y + 1; yi <= lastY; yi++)
                {
                    rule0.doBlock(world, random, xi, yi, zi, rotate);
                }
            }
        }
    }

    private static final Pattern patternRuleRaw = Pattern.compile("(?:[^*=^]*\\*)?(?:rule[^=^]*)?[=^]");
    private static final Pattern patternRule = Pattern.compile("(?:([1-9]\\d{0,4})\\*)?(rule[^=^]*)?([=^])(?:([1-9]\\d{0,4})\\*)?(.*)");
    private enum ParserState { PRE_RULE_PHASE, RULE_PHASE, POST_RULE_PHASE }

    private void parseFile(ArrayList<String> lines) throws Exception
    {
        // first get the variables.
        parseVariables(lines);

        // now get the rest of the data
        final Iterator<String> i = lines.iterator();
        int lineIndex = 0;
        int ruleIndex = 0;
        int groupIndex = 0;
        int groupSize = 0;
        int repeatCountPrevious = 0;
        int variantIndex = 0;
        String line;
        ParserState parserState = ParserState.PRE_RULE_PHASE;
        Matcher matcher;
        while (i.hasNext())
        {
            line = i.next();
            ++lineIndex;
            if (!line.startsWith("#") && !line.isEmpty())
            {
                if (line.startsWith("layer"))
                {
                    if (repeatCountPrevious > 1)
                    {
                        final int ruleIndexPrevious = ruleIndex;
                        ruleIndex += (repeatCountPrevious - 1)*(ruleIndex - groupIndex + 1);
                        if (debugging)
                        {
                            debugPrinter.printf("template [%s] line [%d]: duplicating group from rules #%d-#%d (%d times) to create rules #%d-#%d\n", name, lineIndex, groupIndex, ruleIndexPrevious, repeatCountPrevious, ruleIndexPrevious + 1, ruleIndex);
                        }
                        repeatCountPrevious = 0;
                    }
                    parserState = ParserState.POST_RULE_PHASE;
                    // add in data until we reach the end of the layer
                    ArrayList<String> layerlines = new ArrayList<>();
                    line = i.next();
                    while (!line.startsWith("endlayer"))
                    {
                        if (line.charAt(0) != '#')
                        {
                            layerlines.add(line);
                        }
                        line = i.next();
                    }
                    layers.add(new RuinTemplateLayer(layerlines, width, length, variantRuleset.size()));
                }
                else if ((matcher = patternRule.matcher(line)).matches())
                {
                    if (parserState == ParserState.POST_RULE_PHASE)
                    {
                        throw new Exception("Template file problem: A Rule was defined after a layer! Define all rules before the first layer!");
                    }

                    final boolean isFirstRule = parserState == ParserState.PRE_RULE_PHASE;
                    final boolean hasRepeatCount = matcher.group(1) != null;
                    final int repeatCount = hasRepeatCount ? Integer.parseInt(matcher.group(1)) : 1;
                    final boolean hasName = matcher.group(2) != null;
                    final boolean isVariant = matcher.group(3).equals("^");
                    final int weight = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 1;
                    final RuinTemplateRule rule = new RuinTemplateRule(debugPrinter, this, matcher.group(5), debugging);

                    parserState = ParserState.RULE_PHASE;
                    if (isVariant)
                    {
                        if (isFirstRule)
                        {
                            debugPrinter.printf("template [%s] line [%d]: first rule must start a new variant group (i.e., use = instead of ^)\n", name, lineIndex);
                            throw new Exception("Template file problem: First rule cannot join nonexistent variant group!");
                        }
                        if (hasRepeatCount)
                        {
                            debugPrinter.printf("template [%s] line [%d]: only first rule variant in a group can have repeat count\n", name, lineIndex);
                            throw new Exception("Template file problem: Unexpected repeat count before variant rule group member!");
                        }

                        if (hasName)
                        {
                            ++ruleIndex;
                            variantIndex = 1;
                            if (debugging)
                            {
                                debugPrinter.printf("template [%s] line [%d]: adding new rule #%d to variant group with rule #%d\n", name, lineIndex, ruleIndex, groupIndex);
                            }
                            variantRuleset.addVariantRule(weight, rule);
                        }
                        else
                        {
                            if (ruleIndex == groupIndex)
                            {
                                ++groupSize;
                            }
                            else if (variantIndex == groupSize && debugging)
                            {
                                debugPrinter.printf("template [%s] line [%d]: rule #%d has more variants than first rule in group (rule #%d with %d variants); excess will be ignored\n", name, lineIndex, ruleIndex, groupIndex, groupSize);
                            }
                            ++variantIndex;
                            if (debugging)
                            {
                                debugPrinter.printf("template [%s] line [%d]: adding variant #%d to rule #%d\n", name, lineIndex, variantIndex, ruleIndex);
                            }
                            variantRuleset.addVariant(weight, rule);
                        }
                    }
                    else
                    {
                        if (repeatCountPrevious > 1)
                        {
                            final int ruleIndexPrevious = ruleIndex;
                            ruleIndex += (repeatCountPrevious - 1)*(ruleIndex - groupIndex + 1);
                            if (debugging)
                            {
                                debugPrinter.printf("template [%s] line [%d]: duplicating variant group from rules #%d-#%d (%d times) to create rules #%d-#%d\n", name, lineIndex, groupIndex, ruleIndexPrevious, repeatCountPrevious, ruleIndexPrevious + 1, ruleIndex);
                            }
                        }
                        groupIndex = ++ruleIndex;
                        groupSize = 1;
                        repeatCountPrevious = repeatCount;
                        variantIndex = 1;
                        if (isFirstRule)
                        {
                            if (!hasName)
                            {
                                if (hasRepeatCount)
                                {
                                    debugPrinter.printf("template [%s] line [%d]: unnamed rule (alternate rule0) cannot have repeat count\n", name, lineIndex);
                                    throw new Exception("Template file problem: Unexpected repeat count before unnamed rule!");
                                }
                                groupIndex = ruleIndex = 0;
                                if (debugging)
                                {
                                    debugPrinter.printf("template [%s] line [%d]: alternate rule #0 specified\n", name, lineIndex);
                                }
                            }
                            else
                            {
                                if (debugging)
                                {
                                    debugPrinter.printf("template [%s] line [%d]: creating default (preserving air) rule #0\n", name, lineIndex);
                                }
                                variantRuleset.addVariantGroup(1, 1, new RuinTemplateRule(debugPrinter, this, "0,100,?air"));
                            }
                        }
                        else
                        {
                            if (!hasName)
                            {
                                debugPrinter.printf("template [%s] line [%d]: unnamed rule (alternate rule0) must appear before all other rules\n", name, lineIndex);
                                throw new Exception("Template file problem: Rule name missing!");
                            }
                        }
                        if (debugging)
                        {
                            debugPrinter.printf("template [%s] line [%d]: creating new rule #%d\n", name, lineIndex, ruleIndex);
                        }
                        variantRuleset.addVariantGroup(repeatCount, weight, rule);
                    }
                }
                else if (patternRuleRaw.matcher(line).lookingAt())
                {
                    debugPrinter.printf("template [%s] line [%d]: invalid rule syntax\n", name, lineIndex);
                    throw new Exception("Template file problem: Cannot parse rule!");
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
                        final HashSet<Block> acceptables = new HashSet<>();
                        Block b;
                        for (String aCheck : check)
                        {
                            b = Block.REGISTRY.getObject(new ResourceLocation(aCheck));
                            if (b != Blocks.AIR)
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
                        final HashSet<Block> inacceptables = new HashSet<>();
                        Block b;
                        for (String aCheck : check)
                        {
                            b = Block.REGISTRY.getObject(new ResourceLocation(aCheck));
                            if (b != Blocks.AIR)
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
                    Collections.addAll(biomes, line.split("=")[1].split(","));
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
                     * Blocks.
                     */
                    String[] check = line.split("=");
                    String[] bounds = check[1].split(",");
                    randomOffMin = Integer.parseInt(bounds[0]);
                    randomOffMax = Math.max(randomOffMin, Integer.parseInt(bounds[1]));
                }
                else if (line.startsWith("uniqueMinDistance"))
                {
                    String[] check = line.split("=");
                    uniqueMinDistance = Integer.parseInt(check[1]);
                }
                else if (line.startsWith("preventRotation"))
                {
                    preventRotation = Integer.parseInt(line.split("=")[1]) == 1;
                }
                else if (line.startsWith("adjoining_template"))
                {
                    // syntax: adjoining_template=<template>;<relativeX>;<allowedYdifference>;<relativeZ>[;<spawnchance>]
                    String[] vals = line.split("=")[1].split(";");

                    File file = new File(RuinsMod.getMinecraftBaseDir(), RuinsMod.TEMPLATE_PATH_MC_EXTRACTED + vals[0] + ".tml");
                    if (file.exists() && file.canRead())
                    {
                        RuinTemplate adjTempl = new RuinTemplate(debugPrinter, file.getCanonicalPath(), file.getName(), false);
                        AdjoiningTemplateData data = new AdjoiningTemplateData();
                        data.adjoiningTemplate = adjTempl;
                        data.relativeX = Integer.parseInt(vals[1]);
                        data.acceptableY = Integer.parseInt(vals[2]);
                        data.relativeZ = Integer.parseInt(vals[3]);
                        data.spawnchance = vals.length > 4 ? Float.parseFloat(vals[4]) : 100f;

                        adjoiningTemplates.add(data);
                    }
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

    /**
     * Marks coordinates to be applied bonemeal to after spawning has finished and a block update was pushed
     */
    public void markBlockForBonemeal(int x, int y, int z)
    {
        bonemealMarkers.add(x);
        bonemealMarkers.add(y);
        bonemealMarkers.add(z);
    }

    // these 2 methods are only here so mockito can intercept calls
    protected Block tryFindingBlockOfName(String blockName)
    {
        // debugPrinter.printf("%s mapped to %s\n", blockName, cachedBlock);
        return Block.REGISTRY.getObject(new ResourceLocation(blockName));
    }

    public Block getAirBlock()
    {
        return Blocks.AIR;
    }

    // A VariantRuleset is a list of template rules, some of which may have a number of variant versions from which the
    // actual rule definitions for a particular structure are randomly drawn. This allows per-structure randomization
    // (in addition to regular per-block randomization) without the need to create separate templates.
    private class VariantRuleset
    {
        private final ArrayList<VariantGroup> variantGroups;

        // create a new empty ruleset
        VariantRuleset()
        {
            variantGroups = new ArrayList<>();
        }

        // add another variant to the most-recently added VariantRule in this set
        public void addVariant(final int weight, final RuinTemplateRule variant)
        {
            variantGroups.get(variantGroups.size() - 1).addVariant(weight,  variant);
        }

        // add another VariantRule to the most-recently added VariantGroup in this set, given its first variant
        public void addVariantRule(final int weight, final RuinTemplateRule variant)
        {
            variantGroups.get(variantGroups.size() - 1).addVariantRule(weight,  variant);
        }

        // add another VariantGroup to this set, given the first variant of its first VariantRule
        public void addVariantGroup(final int repeatCount, final int weight, final RuinTemplateRule variant)
        {
            variantGroups.add(new VariantGroup(repeatCount, weight, variant));
        }

        // resolve all the VariantRules in this group by selecting one variant for each
        public ArrayList<RuinTemplateRule> getVariants(Random random)
        {
            final ArrayList<RuinTemplateRule> variants = new ArrayList<>();
            for (final VariantGroup variantGroup: variantGroups)
            {
                variants.addAll(variantGroup.getVariants(random, variants.size()));
            }
            return variants;
        }

        // get the total number of RuinTemplateRules in this set
        public int size()
        {
            int total = 0;
            for (final VariantGroup variantGroup: variantGroups)
            {
                total += variantGroup.size();
            }
            return total;
        }

        // A VariantGroup object contains an array of "coordinated" VariantRule objects (see VariantRule class comments
        // below). When a template containing a VariantGroup is used to build a structure, randomly selected variants
        // of each of its constituent rules are chosen based on a common unweighted "selector" drawn from the variants
        // of the first VariantRule of that group. Whew! For example:
        //     rule3=0,100,stone
        //     ^0,100,cobblestone
        //     rule4^0,100,dirt
        //     ^0,100,gravel
        // Note rule4 has a name, so it defines a separate rule, but it uses a caret instead of an equals sign, which
        // indicates it belongs to the same group as the rule above it (i.e., rule3). That means if a structure uses
        // variant #1 of rule3 (stone), it will also use variant #1 of rule4 (dirt). Likewise, if it uses variant #2
        // of rule3 (cobblestone), it also uses variant #2 of rule4 (gravel).
        //
        // The name of the first rule of a VariantGroup may be preceded by a repeat count of the form "n*" to produce
        // duplicates of that entire group, exactly as though all its rules and variants were cut and pasted into the
        // template file n times. While this can be a useful shortcut in certain situations, mind the effect it has on
        // how rules are numbered.
        private class VariantGroup
        {
            private final int repeatCount;
            private final ArrayList<VariantRule> variantRules;

            // create a new VariantGroup, given the first variant of its first VariantRule
            VariantGroup(final int repeatCountSpec, final int weight, final RuinTemplateRule variant)
            {
                repeatCount = repeatCountSpec;
                variantRules = new ArrayList<>();
                addVariantRule(weight, variant);
            }

            // add another variant to the most-recently added VariantRule in this group
            public void addVariant(final int weight, final RuinTemplateRule variant)
            {
                variantRules.get(variantRules.size() - 1).addVariant(weight,  variant);
            }

            // add another VariantRule to this group, given its first variant
            public void addVariantRule(final int weight, final RuinTemplateRule variant)
            {
                variantRules.add(new VariantRule(weight, variant));
            }

            // resolve all the VariantRules in this group by selecting one variant for each
            public ArrayList<RuinTemplateRule> getVariants(Random random, final int ruleIndexInitial)
            {
                final ArrayList<RuinTemplateRule> variants = new ArrayList<>();
                for (int i = 0; i < repeatCount; ++i)
                {
                    // selector is an unweighted random index; the same value is used for all members of the group
                    final int selector = variantRules.get(0).getRandomSelector(random, ruleIndexInitial + variants.size());
                    for (final VariantRule variantRule: variantRules)
                    {
                        variants.add(variantRule.getVariant(selector, ruleIndexInitial + variants.size()));
                    }
                }
                return variants;
            }

            // get the total number of VariantRules in this group
            public int size()
            {
                return repeatCount*variantRules.size();
            }

            // A VariantRule object contains an array of different RuinTemplateRule objects assigned to the same rule
            // index. In a template file, it looks like this:
            //     rule1=0,100,dirt
            //     ^0,100,gravel
            //     ^0,100,cobblestone
            //     ^0,100,stone
            // A missing rule name and caret (^) instead of equals sign (=) defines a variant of the rule above it
            // instead of a separate rule.
            //
            // Variants within a VariantRule can be weighted to affect the probabilities of their selection by adding
            // a "n*" prefix to the variant specification, where n is an integer from 1 to 99999, inclusive. For
            // example:
            //     rule2=2*0,100,dirt
            //     ^0,100,gravel
            //     ^4*0,100,cobblestone
            //     ^3*0,100,stone
            // Weights are optional; where none is specified (as in the gravel variant of rule2--or all four variants
            // of rule1--in the examples above), a weight of 1 is assumed.
            private class VariantRule
            {
                private final ArrayList<Integer> weights;
                private int weightsTotal;
                private final ArrayList<RuinTemplateRule> variants;

                // create a new VariantRule, given its first variant
                public VariantRule(final int weight, final RuinTemplateRule variant)
                {
                    weights = new ArrayList<>();
                    weightsTotal = 0;
                    variants = new ArrayList<>();
                    addVariant(weight, variant);
                }

                // add another variant to this VariantRule
                public void addVariant(final int weight, final RuinTemplateRule variant)
                {
                    weights.add(weight);
                    weightsTotal += weight;
                    variants.add(variant);
                }

                // generate a random selector based on the variants in this VariantRule
                public int getRandomSelector(Random random, final int ruleIndex)
                {
                    final int selector = random.nextInt(weightsTotal);
                    if (debugging && weightsTotal > 1)
                    {
                        debugPrinter.printf("template [%s] rule [%d]: group selector drawn (from 1-%d) = %d\n", name, ruleIndex, weightsTotal, selector + 1);
                    }
                    return selector;
                }

                // get the variant associated with a given selector
                public RuinTemplateRule getVariant(final int selectorInitial, final int ruleIndex)
                {
                    int index = variants.size() - 1;
                    if (selectorInitial < weightsTotal)
                    {
                        index = 0;
                        for (int selector = selectorInitial; (selector -= weights.get(index)) >= 0; ++index);
                    }
                    if (debugging && weightsTotal > 1)
                    {
                        if (selectorInitial < weightsTotal)
                        {
                            debugPrinter.printf("template [%s] rule [%d]: variant #%d with weight %d chosen by selector = %d\n", name, ruleIndex, index + 1, weights.get(index), selectorInitial + 1);
                        }
                        else
                        {
                            debugPrinter.printf("template [%s] rule [%d]: last variant #%d chosen by selector = %d\n", name, ruleIndex, index + 1, selectorInitial + 1);
                        }
                    }
                    return variants.get(index);
                }
            }
        }
    }
}
