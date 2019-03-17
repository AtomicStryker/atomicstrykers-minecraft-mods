package atomicstryker.ruins.common;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

class World2TemplateParser extends Thread {

    /**
     * Block upon which the parser is started, is used to define the template
     * area and disregarded when detecting Blocks. Can be used for support in
     * templates.
     */
    private final BlockData templateHelperBlock;
    private final BlockData nothing = new BlockData(Blocks.AIR.getDefaultState(), 0, null);

    /**
     * Starting point for the template parse scan
     */
    private final int x, y, z;

    /**
     * These values denote the template size and location
     */
    private int lowestX, lowestZ, xLength, zLength;

    /**
     * This keeps track how high above the baseplate the first template content
     * appeared
     */
    private int yPadding;

    /**
     * Blocks and metas found while parsing the template. Each different
     * instance will be made a rule
     */
    private final ArrayList<BlockData> usedBlocks;

    /**
     * Threedimensional Layerdata, starts with the first layer containing a
     * Block
     */
    private ArrayList<BlockData[][]> layerData;

    /**
     * World instance
     */
    private final World world;

    /**
     * Template target filename
     */
    private final String fileName;

    /**
     * Player that executed the command
     */
    private final EntityPlayer player;

    /**
     * Counts in the Background to stop near-finite loops by accident
     */
    private int blocker;

    /**
     * Prepares the conversion of an ingame construction to Ruins template file.
     * The player must select any block instance of a rectangular plate of the
     * same Block which defines the template size. Any different Blocks found
     * above this plate are considered to make up the template.
     */
    public World2TemplateParser(EntityPlayer p, int a, int b, int c, String fName) {
        player = p;
        world = p.world;
        x = a;
        y = b;
        z = c;
        fileName = fName;
        IBlockState state = world.getBlockState(new BlockPos(a, b, c));
        templateHelperBlock = new BlockData(state, 0, null);
        usedBlocks = new ArrayList<>();
        layerData = new ArrayList<>();
    }

    @Override
    public void run() {

        if (templateHelperBlock.blockState.getBlock() == Blocks.AIR) {
            player.sendMessage(new TextComponentTranslation("Template Parse fail, chosen Block was air WTF?!"));
            return;
        }

        blocker = 0;
        lowestX = x - 1;
        while (templateHelperBlock.matchesBlock(world, lowestX, y, z)) {
            lowestX--;
            checkLockup();
        }
        lowestX++;

        blocker = 0;
        lowestZ = z - 1;
        while (templateHelperBlock.matchesBlock(world, x, y, lowestZ)) {
            lowestZ--;
            checkLockup();
        }
        lowestZ++;

        blocker = 0;
        int xmax = x + 1;
        while (templateHelperBlock.matchesBlock(world, xmax, y, z)) {
            xmax++;
            checkLockup();
        }
        xmax--;
        xLength = 1 + xmax - lowestX;

        blocker = 0;
        int zmax = z + 1;
        while (templateHelperBlock.matchesBlock(world, x, y, zmax)) {
            zmax++;
            checkLockup();
        }
        zmax--;
        zLength = 1 + zmax - lowestZ;

        readBlocks(world);
        player.sendMessage(new TextComponentTranslation("Block reading finished. Rules: " + usedBlocks.size() + ", layers: " + layerData.size() + ", xlen: " + xLength + ", zlen: " + zLength));

        File templateFile = new File(RuinsMod.getMinecraftBaseDir(), RuinsMod.TEMPLATE_PATH_MC_EXTRACTED + "templateparser/" + fileName + ".tml");
        toFile(templateFile);

        player.sendMessage(new TextComponentTranslation("Success writing templatefile " + templateFile));
    }

    private void checkLockup() {
        if (blocker++ > 1024) {
            throw new IndexOutOfBoundsException("Runaway loop detected! Did you hit the ground?!");
        }
    }

    private void readBlocks(World world) {
        yPadding = 0;
        int highestY = y + 1;
        BlockData temp = nothing.copy();
        BlockData currentinstance;
        BlockData[][] currentLayer;
        blocker = 0;

        for (int yi = y + 1; true; yi++) {
            currentLayer = new BlockData[xLength][zLength];
            layerData.add(currentLayer);

            if (yi > (highestY + 100)) {
                // strip off the empty layers again
                layerData = new ArrayList<>(layerData.subList(yPadding, highestY - y));
                return;
            }
            checkLockup();

            int blockx, blocky, blockz;
            for (int xi = 0; xi < xLength; xi++) {
                for (int zi = 0; zi < zLength; zi++) {
                    blockx = xi + lowestX;
                    blocky = yi;
                    blockz = zi + lowestZ;

                    BlockPos pos = new BlockPos(blockx, blocky, blockz);
                    temp.blockState = world.getBlockState(pos);
                    temp.spawnRule = 0;
                    temp.tileEntity = world.getTileEntity(pos);

                    if (temp.blockState.getBlock() == Blocks.AIR || temp.equals(templateHelperBlock)) {
                        currentLayer[xi][zi] = nothing;
                        continue;
                    }

                    if (highestY == -1) {
                        yPadding = yi - y;
                    }
                    highestY = yi;

                    /* TODO Is any of that still necessary?
                    TileEntity te = world.getTileEntity(new BlockPos(new BlockPos(blockx, blocky, blockz)));
                    // handle special blocks
                    if (temp.blockState.getBlock() == Blocks.TORCH || temp.blockState.getBlock() == Blocks.REDSTONE_TORCH) {
                        // if meta says FLOOR, add FLOOR dependency, alse
                        // ADJACENT dependency
                        temp.spawnRule = (temp.meta == 0 || temp.meta == 5) ? SPAWN_RULE_EXISTSBELOW : SPAWN_RULE_EXISTSADJACENT;
                    } else if (temp.block == Blocks.PISTON_HEAD || temp.block == Blocks.PISTON_EXTENSION) {
                        temp.spawnRule = SPAWN_RULE_EXISTSADJACENT;
                    } else if (temp.block == Blocks.WOODEN_BUTTON || temp.block == Blocks.STONE_BUTTON) {
                        // if meta says FLOOR, add FLOOR dependency, alse
                        // ADJACENT dependency
                        temp.spawnRule = temp.meta == 5 ? SPAWN_RULE_EXISTSBELOW : SPAWN_RULE_EXISTSADJACENT;
                    } else if (te instanceof IInventory && !isIInventoryEmpty((IInventory) te)) {
                        IInventory inventory = (IInventory) te;
                        final ArrayList<ItemStack> invItems = new ArrayList<>();
                        final ArrayList<Integer> slots = new ArrayList<>();
                        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                            if (inventory.getStackInSlot(slot) != ItemStack.EMPTY) {
                                invItems.add(inventory.getStackInSlot(slot));
                                slots.add(slot);
                            }
                        }

                        StringBuilder sb = new StringBuilder("IInventory;");
                        sb.append(Block.REGISTRY.getNameForObject(temp.block));
                        sb.append(';');
                        for (int index = 0; index < invItems.size(); index++) {
                            ItemStack stack = invItems.get(index);
                            String ident;
                            if (stack.getItem() instanceof ItemBlock) {
                                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                                ident = Block.REGISTRY.getNameForObject(itemBlock.getBlock()).toString();
                            } else {
                                ident = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
                            }
                            if (ident != null) {
                                sb.append(ident);
                            } else {
                                sb.append(stack.getUnlocalizedName());
                            }
                            sb.append('#');
                            if (stack.getTagCompound() != null) {
                                sb.append(stack.getTagCompound().toString());
                            } else {
                                sb.append(stack.getCount());
                            }
                            sb.append('#');
                            sb.append(stack.getItemDamage());
                            sb.append('#');
                            sb.append(slots.get(index));
                            sb.append('+');
                        }

                        temp.data = sb.toString();
                        int iLastSep = temp.data.lastIndexOf("+");
                        if (iLastSep != -1) {
                            temp.data = temp.data.substring(0, iLastSep) + "-" + temp.meta;
                        } else {
                            temp.data = temp.data + "-" + temp.meta;
                        }
                    } else if (temp.block == Blocks.CHEST) {
                        temp.data = "ChestGenHook:chests/simple_dungeon:5-" + temp.meta;
                    } else if (temp.block == Blocks.COMMAND_BLOCK) {
                        TileEntityCommandBlock tec = (TileEntityCommandBlock) te;
                        if (tec != null) {
                            temp.data = "CommandBlock:" + tec.getCommandBlockLogic().getCommand() + ":" + tec.getCommandBlockLogic().getName() + "-" + temp.meta;
                        }
                    } else if (temp.block == Blocks.STANDING_SIGN) {
                        TileEntitySign tes = (TileEntitySign) te;
                        temp.data = convertSignStrings("StandingSign:", tes) + "-" + temp.meta;
                        temp.spawnRule = SPAWN_RULE_EXISTSBELOW;
                    } else if (temp.block == Blocks.WALL_SIGN) {
                        TileEntitySign tes = (TileEntitySign) te;
                        temp.data = convertSignStrings("WallSign:", tes) + "-" + temp.meta;
                        temp.spawnRule = SPAWN_RULE_EXISTSADJACENT;
                    } else if (temp.block == Blocks.SKULL) {
                        TileEntitySkull tes = (TileEntitySkull) te;
                        int skulltype = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 0);
                        int rot = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 1);
                        String specialType = "";
                        GameProfile playerhead = ReflectionHelper.getPrivateValue(TileEntitySkull.class, tes, 2);
                        if (playerhead != null) {
                            specialType = playerhead.getId().toString() + "-" + playerhead.getName();
                        }
                        temp.data = "Skull:" + skulltype + ":" + rot + ((specialType.equals("")) ? "" : ":" + specialType) + "-" + temp.meta;
                    }
                    */

                    int indexInList = usedBlocks.indexOf(temp);
                    if (indexInList == -1) {
                        currentinstance = temp.copy();
                        usedBlocks.add(currentinstance);
                    } else {
                        currentinstance = usedBlocks.get(indexInList);
                    }
                    currentLayer[xi][zi] = currentinstance;
                }
            }
        }
    }

    /*
    private boolean isIInventoryEmpty(IInventory inventory) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (inventory.getStackInSlot(slot) != ItemStack.EMPTY) {
                return false;
            }
        }
        return true;
    }

    private String convertSignStrings(String prefix, TileEntitySign sign) {
        String a = sign.signText[0].getUnformattedText();
        if (a.equals("")) {
            a = "null";
        }
        String b = sign.signText[1].getUnformattedText();
        if (b.equals("")) {
            b = "null";
        }
        String c = sign.signText[2].getUnformattedText();
        if (c.equals("")) {
            c = "null";
        }
        String d = sign.signText[3].getUnformattedText();
        if (d.equals("")) {
            d = "null";
        }
        return prefix + a + ":" + b + ":" + c + ":" + d;
    }
    */

    private void toFile(File file) {
        try {
            if (file.exists()) {
                if (!file.delete()) {
                    throw new RuntimeException("Ruins crashed trying to access file " + file);
                }
            } else {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Ruins crashed trying to access file " + file);
                }
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            pw.println();
            pw.println("# Created by Ruins mod version " + RuinsMod.modversion + " Ingame Parser");
            pw.println("# authoring Player: " + player.getName());
            pw.println();

            pw.println("# TEMPLATE PARAMETER SETTINGS:");
            pw.println("#");
            pw.println("# criterion expression specifying mod ids required to load template");
            pw.println("# may include plus (AND), minus (AND NOT), comma (OR), and parentheses");
            pw.println("# all required mods listed must be present; no prohibited mods can be");
            pw.println("requiredMods=");
            pw.println("#");
            pw.println("# likelihood this template will be chosen relative to all others");
            pw.println("# e.g., a weight=6 template is chosen 3X as often as one with weight=2");
            pw.println("weight=1");
            pw.println("#");
            pw.println("# list of dimensions in which this template may spawn, even if generic");
            pw.println("# one or more dimension names, separated by commas (blank = all)");
            pw.println("dimensionsToSpawnIn=");
            pw.println("#");
            pw.println("# list of other biomes in which this template may spawn");
            pw.println("# biome corresponding to directory is always assumed, listed or not");
            pw.println("# generic templates should leave this list empty");
            pw.println("biomesToSpawnIn=");
            pw.println("#");
            pw.println("# criterion expression specifying types of biome where template spawns");
            pw.println("# this is in addition to those explicitly listed as biomesToSpawnIn");
            pw.println("# generic templates should leave this list empty");
            pw.println("biomeTypesToSpawnIn=");
            pw.println("#");
            pw.println("# list of biomes in which this template may not spawn");
            pw.println("# takes precedence over biomeTypesToSpawnIn, but NOT biomesToSpawnIn");
            pw.println("# should be empty if biomeTypesToSpawnIn is empty");
            pw.println("biomesToNotSpawnIn=");
            pw.println("#");
            pw.println("# depth template is pushed down into the surface when built");
            pw.println("# offset is min/max range of random additional bury depth");
            pw.println("embed_into_distance=" + yPadding);
            pw.println("random_height_offset=0,0");
            pw.println("#");
            pw.println("# whitelist/blacklist of block types on which template may be built");
            pw.println("# specify one, not both; leave the other empty (both empty = allow all)");
            pw.println("acceptable_target_blocks=");
            pw.println("unacceptable_target_blocks=flowing_water,water,flowing_lava,lava");
            pw.println("#");
            pw.println("# size of template (#layers, #rows per layer, #blocks per row)");
            pw.println("dimensions=" + layerData.size() + "," + xLength + "," + zLength);
            pw.println("#");
            pw.println("# max number of missing blocks allowed in surface beneath template");
            pw.println("allowable_overhang=0");
            pw.println("#");
            pw.println("# depth/height limit affected by terrain leveling");
            pw.println("# also maximum bumpiness tolerated in surface beneath template");
            pw.println("max_leveling=2");
            pw.println("#");
            pw.println("# padding applied to all sides of template horizontal footprint");
            pw.println("# expands area affected by terrain leveling (-1 = no leveling)");
            pw.println("leveling_buffer=0");
            pw.println("#");
            pw.println("# do not rotate template randomly when built (1 = no rotation)");
            pw.println("preventRotation=0");
            pw.println("#");
            pw.println("# treat water/lava blocks as air and protect them from rule0 (1 = yes)");
            pw.println("preserve_water=0");
            pw.println("preserve_lava=0");
            pw.println("#");
            pw.println("# minimum distance this template must have from instances of itself");
            pw.println("uniqueMinDistance=0");
            pw.println("#");
            pw.println("# min/max distances this template can be from world spawn (0 = no limit)");
            pw.println("# only applies to overworld--i.e., dimension 0");
            pw.println("spawnMinDistance=0");
            pw.println("spawnMaxDistance=0");
            pw.println("#");
            pw.println("# other template built nearby whenever this one is");
            pw.println("# syntax: <name>;<relativeX>;<maxYdifference>;<relativeZ>[;<chance>]");
            pw.println("# may be used more than once to specify multiple neighbors");
            pw.println("adjoining_template=");
            pw.println();

            NumberFormat id_formatter = NumberFormat.getIntegerInstance();
            if (FileHandler.enableFixedWidthRuleIds && id_formatter instanceof DecimalFormat) {
                int count = usedBlocks.size();
                ((DecimalFormat) id_formatter).applyPattern(count < 10 ? "0" : count < 100 ? "00" : count < 1000 ? "000" : "0");
            }

            int rulenum = 1;
            for (BlockData bd : usedBlocks) {
                pw.println("rule" + id_formatter.format(rulenum) + "=" + bd.toString());
                rulenum++;
            }

            pw.println();

            for (BlockData[][] layer : layerData) {
                pw.println("layer");

                for (BlockData[] aLayer : layer) {
                    /* have to invert this for some reason */
                    for (int j = 0, j2 = layer[0].length - 1; j < layer[0].length; j++, j2--) {
                        /*
                         * since 'nothing' is not contained, it returns -1 + 1 =
                         * 0, which is the default preserveBlock rule
                         */
                        pw.print(id_formatter.format(usedBlocks.indexOf(aLayer[j2]) + 1));

                        if (j < layer[0].length - 1) {
                            pw.print(",");
                        }
                    }
                    pw.println();
                }

                pw.println("endlayer");
                pw.println();
            }

            pw.close();

            CommandTestTemplate.parsedRuin = new RuinTemplate(new PrintWriter(System.out), file.getCanonicalPath(), file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(new TextComponentTranslation("Something broke! See server logfile for exception message and get it to AtomicStryker."));
            player.sendMessage(new TextComponentTranslation("First line of stacktrace: " + e.getMessage()));
        }
    }

    private class BlockData {
        IBlockState blockState;
        int spawnRule;
        TileEntity tileEntity;

        BlockData(IBlockState state, int sr, TileEntity te) {
            blockState = state;
            spawnRule = sr;
            tileEntity = te;
        }

        BlockData copy() {
            return new BlockData(blockState, spawnRule, tileEntity);
        }

        boolean matchesBlock(World w, int x, int y, int z) {
            return w.getBlockState(new BlockPos(x, y, z)) == blockState;
        }

        @Override
        public String toString() {
            return String.format("%s,100,%s", spawnRule, RuleStringNbtHelper.StringFromBlockState(blockState, tileEntity));
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof BlockData && o.toString().equals(this.toString());
        }
    }
}
