package atomicstryker.ruins.common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class RuinTemplateRule
{
    private final Block[] blockIDs;
    private final int[] blockMDs;
    private final String[] blockStrings;
    private final SpecialFlags[] specialFlags;
    private int chance = 100;
    private int condition = 0;
    final RuinTemplate owner;
    private final PrintWriter debugPrinter;
    private final boolean excessiveDebugging;

    // just leave the field null for NONE
    public enum SpecialFlags
    {
        COMMANDBLOCK, ADDBONEMEAL
    }

    public RuinTemplateRule(PrintWriter dpw, RuinTemplate r, String rule, boolean debug) throws Exception
    {
        debugPrinter = dpw;
        owner = r;
        excessiveDebugging = debug;

        ArrayList<String> nbttags = new ArrayList<>(5);
        rule = replaceNBTTags(rule, nbttags);

        String[] blockRules = rule.split(",");
        int numblocks = blockRules.length - 2;
        if (numblocks < 1)
        {
            throw new Exception("No blockIDs specified for rule [" + rule + "] in template " + owner.getName());
        }
        condition = Integer.parseInt(blockRules[0]);
        chance = Integer.parseInt(blockRules[1]);

        String[] data;

        // Command Block special case, contains basically any character that
        // breaks this
        if (blockRules[2].startsWith("CommandBlock:"))
        {
            String[] commandrules = rule.split("CommandBlock:");
            int count = commandrules.length - 1; // -1 because there is a prefix
                                                 // part we ignore
            blockIDs = new Block[count];
            blockMDs = new int[count];
            blockStrings = new String[count];
            specialFlags = new SpecialFlags[count];
            for (int i = 0; i < count; i++)
            {
                blockIDs[i] = null;
                blockMDs[i] = RuinsMod.DIR_NORTH;
                if (commandrules[i + 1].charAt(commandrules[i + 1].length() - 2) == '-') // case
                                                                                         // meta
                                                                                         // value
                                                                                         // "-n"
                                                                                         // present
                                                                                         // (impulse
                                                                                         // command
                                                                                         // block)
                {
                    String meta = "" + commandrules[i + 1].charAt(commandrules[i + 1].length() - 1); // needed
                                                                                                     // because
                                                                                                     // char
                                                                                                     // to
                                                                                                     // int
                                                                                                     // conversion
                                                                                                     // is
                                                                                                     // bad
                                                                                                     // here
                    blockMDs[i] = Integer.valueOf(meta);
                    // strip the last 2 chars from the string or else parsing
                    // the command will fail
                    commandrules[i + 1] = commandrules[i + 1].substring(0, commandrules[i + 1].length() - 3);
                }
                specialFlags[i] = SpecialFlags.COMMANDBLOCK;
                // readd the splitout string for the parsing, offset by 1
                // because of the prefix string
                blockStrings[i] = commandrules[i + 1];
                blockStrings[i] = restoreNBTTags(blockStrings[i], nbttags);
                debugPrinter.println("template " + owner.getName() + " contains Command Block command: " + blockStrings[i] + " with meta: " + blockMDs[i]);
            }
        }
        // not command blocks
        else
        {
            blockIDs = new Block[numblocks];
            blockMDs = new int[numblocks];
            blockStrings = new String[numblocks];
            specialFlags = new SpecialFlags[numblocks];
            for (int i = 0; i < numblocks; i++)
            {
                data = blockRules[i + 2].split("-");
                if (data.length > 1) // has '-' in it, like "torch-5" or
                                     // "planks-3"
                {
                    if (isNumber(data[0])) // torch-5
                    {
                        debugPrinter.println("Rule [" + rule + "] in template " + owner.getName() + " still uses numeric blockIDs! ERROR!");
                        blockIDs[i] = Blocks.AIR;
                        blockMDs[i] = 0;
                        blockStrings[i] = "";
                    }
                    else
                    // planks-3 or ChestGenHook:strongholdLibrary:5-2
                    {
                        blockStrings[i] = blockRules[i + 2];
                        blockStrings[i] = restoreNBTTags(blockStrings[i], nbttags);

                        blockIDs[i] = tryFindingBlockOfName(data[0]);
                        if (blockIDs[i] == Blocks.AIR)
                        {
                            if (!data[0].equals("air"))
                            {
                                blockIDs[i] = null;
                                if (!isKnownSpecialRule(blockStrings[i]))
                                {
                                    throw new Exception(
                                            "Rule [" + rule + "], blockString [" + blockStrings[i] + "] in template " + owner.getName() + " can absolutely not be mapped to anything known");
                                }
                            }
                        }

                        // special case -addbonemeal
                        if (blockIDs[i] instanceof IGrowable && data[data.length - 1].equals("addbonemeal"))
                        {
                            specialFlags[i] = SpecialFlags.ADDBONEMEAL;
                            try
                            {
                                blockMDs[i] = Integer.parseInt(data[data.length - 2]);
                            }
                            catch (NumberFormatException ne)
                            {
                                blockMDs[i] = 0;
                            }
                        }
                        // otherwise parse meta value
                        else
                        {
                            try
                            {
                                blockMDs[i] = Integer.parseInt(data[data.length - 1]);
                            }
                            catch (NumberFormatException ne)
                            {
                                blockMDs[i] = 0;
                            }
                        }
                    }
                }
                else
                // does not have metadata specified, aka "50"
                {
                    if (isNumber(blockRules[i + 2]))
                    {
                        debugPrinter.println("Rule [" + rule + "] in template " + owner.getName() + " still uses numeric blockIDs! ERROR!");
                        blockIDs[i] = Blocks.AIR;
                        blockMDs[i] = 0;
                        blockStrings[i] = "";
                    }
                    else
                    {
                        blockIDs[i] = tryFindingBlockOfName(blockRules[i + 2]);
                        if (blockIDs[i] == Blocks.AIR && !data[0].equals("air"))
                        {
                            // debugPrinter.println("Rule [" + rule + "] in
                            // template " + owner.getName()+" has something
                            // special? Checking again later");
                            blockIDs[i] = null;
                        }
                    }
                    blockMDs[i] = 0;
                    blockStrings[i] = blockRules[i + 2];
                    blockStrings[i] = restoreNBTTags(blockStrings[i], nbttags);
                }

                if (excessiveDebugging)
                {
                    debugPrinter.printf("rule alternative: %d, blockIDs[%s], blockMDs[%s], blockStrings[%s], specialflags:[%s]\n", i + 1, blockIDs[i], blockMDs[i], blockStrings[i], specialFlags[i]);
                }
            }
        }
    }

    RuinTemplateRule(PrintWriter dpw, RuinTemplate r, final String rule) throws Exception
    {
        this(dpw, r, rule, false);
    }

    /**
     * Since NBT contents, especially books, wreak havoc with all the legacy and
     * hardcoded string splitting going on, we replace them in their entirety
     * for slightly less problematic hardcoded strings.
     *
     * @param rule
     *            which may or may not contain nbt tags
     * @param nbttags
     *            a non null array list which after execution contains each nbt
     *            tag in order of occurence
     * @return the input string except all nbt tags have been replaced with
     *         NBT1, NBT2 ... etc
     */
    private String replaceNBTTags(String rule, ArrayList<String> nbttags)
    {
        int openingIndex = rule.indexOf('{');
        while (openingIndex != -1)
        {
            int closingIndex = openingIndex + 1;
            int bracketCounter = 1;
            for (;; closingIndex++)
            {
                if (closingIndex == rule.length())
                {
                    System.err.println("Unbalanced brackets in Ruins template, offending rule: " + rule);
                    return rule;
                }
                if (rule.charAt(closingIndex) == '{')
                {
                    bracketCounter++;
                }
                else if (rule.charAt(closingIndex) == '}')
                {
                    bracketCounter--;
                    if (bracketCounter == 0)
                    {
                        break;
                    }
                }
            }
            String capture = rule.substring(openingIndex, closingIndex + 1);
            nbttags.add(capture);
            debugPrinter.println("template " + owner.getName() + " contains nbt tag: " + capture);
            String pre = rule.substring(0, openingIndex);
            String post = rule.substring(closingIndex + 1, rule.length());
            rule = pre + "NBT" + nbttags.size() + post;
            openingIndex = rule.indexOf('{');
        }
        return rule;
    }

    /**
     * And the reverse, restore the glorious NBT tags into a string loaded with
     * their placeholders
     *
     * @param str
     *            string with nbt placeholders
     * @param nbttags
     *            list with stored nbt tags
     * @return original string with nbt tags
     */
    private String restoreNBTTags(String str, ArrayList<String> nbttags)
    {
        int nbtidx = 1;
        for (String capture : nbttags)
        {
            str = str.replace("NBT" + nbtidx++, capture);
        }
        return str;
    }

    private Block tryFindingBlockOfName(String blockName)
    {
        // debugPrinter.printf("%s mapped to %s\n", blockName, cachedBlock);
        return Block.REGISTRY.getObject(new ResourceLocation(blockName));
    }

    @SuppressWarnings("unused")
    private boolean isNumber(String s)
    {
        if (s == null || s.equals(""))
        {
            return false;
        }
        try
        {
            int n = Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public void doBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        // check to see if we can create this block
        if (random.nextInt(100) < chance)
        {
            // we're cleared, pass it off to the correct conditional.
            switch (condition)
            {
            case 1:
                doAboveBlock(world, random, x, y, z, rotate);
                break;
            case 2:
                doAdjacentBlock(world, random, x, y, z, rotate);
                break;
            case 3:
                doUnderBlock(world, random, x, y, z, rotate);
                break;
            case -1:
                doAboveBlock(world, random, x, y, z, rotate);
                break;
            case -2:
                doAdjacentBlock(world, random, x, y, z, rotate);
                break;
            case -3:
                doUnderBlock(world, random, x, y, z, rotate);
                break;
            default:
                doNormalBlock(world, random, x, y, z, rotate);
                break;
            }
        }
    }

    public boolean runLater()
    {
        switch (condition <= 0 ? 0 - condition : condition)
        {
        case 1:
            return true; // Reorder
        case 2:
            return true; // Unchanged
        default:
            return false;

        }
    }

    public boolean runLast()
    {
        switch (condition <= 0 ? 0 - condition : condition)
        {
        case 3:
            return true; // Unchanged
        case 7:
            return true; // 7 for ALWAYS LAST
        default:
            return false;

        }
    }

    private void doNormalBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void doAboveBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        BlockPos pos = new BlockPos(x, y - 1, z);
        if ((condition <= 0) ^ owner.isIgnoredBlock(world.getBlockState(pos).getBlock(), world, pos))
        {
            return;
        }
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void doAdjacentBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        BlockPos a = new BlockPos(x + 1, y, z);
        BlockPos b = new BlockPos(x, y, z + 1);
        BlockPos c = new BlockPos(x, y, z - 1);
        BlockPos d = new BlockPos(x - 1, y, z);
        if ((condition <= 0) ^ (
        // Are -all- adjacent blocks air?
        (owner.isIgnoredBlock(world.getBlockState(a).getBlock(), world, a)) && (owner.isIgnoredBlock(world.getBlockState(b).getBlock(), world, b))
                && (owner.isIgnoredBlock(world.getBlockState(c).getBlock(), world, c)) && (owner.isIgnoredBlock(world.getBlockState(d).getBlock(), world, d))))
        {
            return;
        }
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void doUnderBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        BlockPos pos = new BlockPos(x, y + 1, z);
        if ((condition <= 0) ^ owner.isIgnoredBlock(world.getBlockState(pos).getBlock(), world, pos))
        {
            return;
        }

        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void handleBlockSpawning(World world, Random random, int x, int y, int z, int blocknum, int rotate, String blockString)
    {
        Block blockID = blockIDs[blocknum];
        if (excessiveDebugging)
        {
            debugPrinter.println("About to place blockID " + blockID + ", meta " + blockMDs[blocknum] + " rotation " + rotate + ", string: " + blockString);
        }
        if (blockID == null)
        {
            doSpecialBlock(world, random, x, y, z, blocknum, rotate, StringEscapeUtils.unescapeJava(blockString));
        }
        else
        {
            placeBlock(world, blocknum, x, y, z, rotate);
        }
    }

    private void placeBlock(World world, int blocknum, int x, int y, int z, int rotate)
    {
        int metadata = rotate != RuinsMod.DIR_NORTH ? rotateMetadata(blockIDs[blocknum], blockMDs[blocknum], rotate) : blockMDs[blocknum];
        world.setBlockState(new BlockPos(x, y, z), blockIDs[blocknum].getStateFromMeta(metadata), 2);

        if (specialFlags[blocknum] != null)
        {
            switch (specialFlags[blocknum])
            {
            case ADDBONEMEAL:
                owner.markBlockForBonemeal(x, y, z);
                break;
            default:
                break;
            }
        }
    }

    private boolean isKnownSpecialRule(final String dataString)
    {
        if (dataString.equals("preserveBlock"))
        {
            return true;
        }
        else if (dataString.startsWith("MobSpawner:"))
        {
            return true;
        }
        else if (dataString.equals("UprightMobSpawn"))
        {
            return true;
        }
        else if (dataString.equals("EasyMobSpawn"))
        {
            return true;
        }
        else if (dataString.equals("MediumMobSpawn"))
        {
            return true;
        }
        else if (dataString.equals("HardMobSpawn"))
        {
            return true;
        }
        else if (dataString.startsWith("EasyChest"))
        {
            return true;
        }
        else if (dataString.startsWith("MediumChest"))
        {
            return true;
        }
        else if (dataString.startsWith("HardChest"))
        {
            return true;
        }
        else if (dataString.startsWith("ChestGenHook:"))
        {
            return true;
        }
        else if (dataString.startsWith("IInventory;"))
        {
            return tryFindingObject(dataString.split(";")[1]) instanceof Block;
        }
        else if (dataString.equals("EnderCrystal"))
        {
            return true;
        }
        else if (dataString.startsWith("CommandBlock:"))
        {
            return true;
        }
        else if (dataString.startsWith("StandingSign:"))
        {
            return true;
        }
        else if (dataString.startsWith("WallSign:"))
        {
            return true;
        }
        else if (dataString.startsWith("Skull:"))
        {
            return true;
        }
        else if (dataString.startsWith("teBlock;"))
        {
            return true;
        }
        return false;
    }

    private void doSpecialBlock(World world, Random random, int x, int y, int z, int blocknum, int rotate, final String dataString)
    {
        if (dataString.equals("preserveBlock"))
        {
            // NOOP
        }
        else if (dataString.startsWith("MobSpawner:"))
        {
            addCustomSpawner(world, x, y, z, dataString.substring(dataString.indexOf(":") + 1));
        }
        else if (dataString.equals("UprightMobSpawn"))
        {
            addUprightMobSpawn(world, random, x, y, z);
        }
        else if (dataString.equals("EasyMobSpawn"))
        {
            addEasyMobSpawn(world, random, x, y, z);
        }
        else if (dataString.equals("MediumMobSpawn"))
        {
            addMediumMobSpawn(world, random, x, y, z);
        }
        else if (dataString.equals("HardMobSpawn"))
        {
            addHardMobSpawn(world, random, x, y, z);
        }
        else if (dataString.startsWith("EasyChest"))
        {
            addEasyChest(world, random, x, y, z, rotateMetadata(Blocks.CHEST, blockMDs[blocknum], rotate), random.nextInt(3) + 3);
        }
        else if (dataString.startsWith("MediumChest"))
        {
            addMediumChest(world, random, x, y, z, rotateMetadata(Blocks.CHEST, blockMDs[blocknum], rotate), random.nextInt(4) + 3);
        }
        else if (dataString.startsWith("HardChest"))
        {
            addHardChest(world, random, x, y, z, rotateMetadata(Blocks.CHEST, blockMDs[blocknum], rotate), random.nextInt(5) + 3);
        }
        else if (dataString.startsWith("ChestGenHook:"))
        {
            String[] s = dataString.split(":");
            int targetCount = s.length > 1 ? Integer.valueOf(s[2].split("-")[0]) : 0;
            addChestGenChest(world, random, x, y, z, s[1], targetCount, rotateMetadata(Blocks.CHEST, blockMDs[blocknum], rotate));
        }
        else if (dataString.startsWith("IInventory;"))
        {
            ArrayList<String> nbttags = new ArrayList<>(5);
            String dataWithoutNBT = replaceNBTTags(dataString, nbttags);
            String[] s = dataWithoutNBT.split(";");
            Object o = tryFindingObject(s[1]);
            if (o instanceof Block)
            {
                Block b = (Block) o;
                // need to strip meta '-x' value if present
                if (s[2].lastIndexOf("-") > s[2].length() - 5)
                {
                    addIInventoryBlock(world, random, x, y, z, b, s[2].substring(0, s[2].lastIndexOf("-")), nbttags, rotateMetadata(b, blockMDs[blocknum], rotate));
                }
                else
                {
                    addIInventoryBlock(world, random, x, y, z, b, s[2], nbttags, rotateMetadata(b, blockMDs[blocknum], rotate));
                }
            }
            else
            {
                System.err.println("Ruins Mod could not determine what IInventory block to spawn for [" + s[1] + "] in Ruin template: " + owner.getName());
            }
        }
        else if (dataString.equals("EnderCrystal"))
        {
            spawnEnderCrystal(world, x, y, z);
        }
        else if (specialFlags[blocknum] == SpecialFlags.COMMANDBLOCK)
        {
            int lastIdx = dataString.lastIndexOf(":");
            String sender;
            String command;
            if (lastIdx < 0)
            {
                command = dataString;
                sender = "@";
            }
            else
            {
                command = dataString.substring(0, lastIdx);
                sender = dataString.substring(lastIdx + 1, dataString.length());
            }
            addCommandBlock(world, x, y, z, command, sender, blockMDs[blocknum], rotate);
        }
        else if (dataString.startsWith("StandingSign:"))
        {
            String[] splits = dataString.split(":");
            int meta = blockMDs[blocknum];
            if (rotate != RuinsMod.DIR_NORTH)
            {
                meta = rotateMetadata(Blocks.STANDING_SIGN, blockMDs[blocknum], rotate);
            }
            world.setBlockState(new BlockPos(x, y, z), Blocks.STANDING_SIGN.getStateFromMeta(meta), 2);
            TileEntitySign tes = (TileEntitySign) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
            if (tes != null && tes.signText != null)
            {
                for (int i = 0; i < tes.signText.length && i + 1 < splits.length; i++)
                {
                    tes.signText[i] = (splits[i + 1].split("-")[0].equals("null")) ? new TextComponentTranslation("") : new TextComponentTranslation(splits[i + 1].split("-")[0]);
                }
            }
        }
        else if (dataString.startsWith("WallSign:"))
        {
            String[] splits = dataString.split(":");
            int meta = blockMDs[blocknum];
            if (rotate != RuinsMod.DIR_NORTH)
            {
                meta = rotateMetadata(Blocks.WALL_SIGN, blockMDs[blocknum], rotate);
            }
            world.setBlockState(new BlockPos(x, y, z), Blocks.WALL_SIGN.getStateFromMeta(meta), 2);
            TileEntitySign tes = (TileEntitySign) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
            if (tes != null && tes.signText != null)
            {
                for (int i = 0; i < tes.signText.length && i + 1 < splits.length; i++)
                {
                    tes.signText[i] = (splits[i + 1].split("-")[0].equals("null")) ? new TextComponentTranslation("") : new TextComponentTranslation(splits[i + 1].split("-")[0]);
                }
            }
        }
        else if (dataString.startsWith("Skull:"))
        {
            // standard case Skull:2:8-3
            world.setBlockState(new BlockPos(x, y, z), Blocks.SKULL.getStateFromMeta(rotateFloorSkull(blockMDs[blocknum], rotate)), 2);
            String[] splits = dataString.split(":");
            TileEntitySkull tes = (TileEntitySkull) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
            ReflectionHelper.setPrivateValue(TileEntitySkull.class, tes, Integer.valueOf(splits[1]), 0);
            int rot = Integer.valueOf(splits[2].split("-")[0]); // skull te's
                                                                // rotate like
                                                                // standing sign
                                                                // blocks
            ReflectionHelper.setPrivateValue(TileEntitySkull.class, tes, rotateMetadata(Blocks.STANDING_SIGN, rot, rotate), 1);

            // is a player head saved?
            // looks like
            // Skull:3:8:1b4d8438-e714-3553-a433-059f2d3b1fd2-AtomicStryker-3
            if (splits.length > 3)
            {
                // split segment like this:
                // 1b4d8438-e714-3553-a433-059f2d3b1fd2-AtomicStryker-3
                String[] moresplits = splits[3].split("-");
                UUID id = UUID.fromString(moresplits[0] + "-" + moresplits[1] + "-" + moresplits[2] + "-" + moresplits[3] + "-" + moresplits[4]);
                GameProfile playerprofile = new GameProfile(id, moresplits[5]);
                ReflectionHelper.setPrivateValue(TileEntitySkull.class, tes, playerprofile, 2);
            }
        }
        else if (dataString.startsWith("teBlock;"))
        {
            debugPrinter.println("teBlock about to be placed: " + dataString);
            // examples: teBlock;minecraft:trapped_chest;{...nbt json...},
            // teBlock;minecraft:trapped_chest;{...nbt json...}-4
            String[] in = dataString.split(";");
            Block b = Block.REGISTRY.getObject(new ResourceLocation(in[1]));
            debugPrinter.println("teBlock object from [" + in[1] + "]: " + b);
            if (b != Blocks.AIR)
            {
                BlockPos p = new BlockPos(x, y, z);
                try
                {
                    NBTTagCompound tc = JsonToNBT.getTagFromJson(in[2].substring(0, in[2].lastIndexOf('}') + 1));
                    debugPrinter.println("teBlock read, decoded nbt tag: " + tc.toString());
                    world.setBlockState(p, b.getStateFromMeta(blockMDs[blocknum]), rotate);
                    TileEntity tenew = TileEntity.create(world, tc);
                    world.removeTileEntity(p);
                    world.setTileEntity(p, tenew);
                }
                catch (NBTException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            System.err.println("Ruins Mod could not determine what to spawn for [" + dataString + "] in Ruin template: " + owner.getName());
        }
    }

    private int rotateFloorSkull(int meta, int rot)
    {
        if (meta == 1)
        {
            return 1;
        }
        return CustomRotationMapping.getMapping(Blocks.SKULL, meta, rot);
    }

    private int getBlockNum(Random random)
    {
        return random.nextInt(blockIDs.length);
    }

    private void spawnEnderCrystal(World world, int x, int y, int z)
    {
        EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
        entityendercrystal.setLocationAndAngles((x + 0.5F), y, (z + 0.5F), world.rand.nextFloat() * 360.0F, 0.0F);
        world.spawnEntity(entityendercrystal);
        world.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.getDefaultState(), 2);
    }

    private void addCustomSpawner(World world, int x, int y, int z, String id)
    {
        ResourceLocation rsl = new ResourceLocation(id);
        world.setBlockState(new BlockPos(x, y, z), Blocks.MOB_SPAWNER.getDefaultState(), 2);
        TileEntityMobSpawner mobspawner = (TileEntityMobSpawner) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (mobspawner != null)
        {
            Entity test = EntityList.createEntityByIDFromName(rsl, world);
            if (test == null)
            {
                System.err.println("Warning: Ruins Mod could not find an Entity [" + id + "] set for a Mob Spawner");
                for (ResourceLocation entString : EntityList.getEntityNameList())
                {
                    if (entString.getResourcePath().contains(id))
                    {
                        rsl = entString;
                        System.err.println("Ruins Mod going with containing match [" + id + "]");
                        break;
                    }
                }
            }
            mobspawner.getSpawnerBaseLogic().setEntityId(rsl);
        }
    }

    private void addEasyMobSpawn(World world, Random random, int x, int y, int z)
    {
        switch (random.nextInt(2))
        {
        case 0:
            addCustomSpawner(world, x, y, z, "Skeleton");
            break;
        default:
            addCustomSpawner(world, x, y, z, "Zombie");
            break;
        }
    }

    private void addMediumMobSpawn(World world, Random random, int x, int y, int z)
    {
        switch (random.nextInt(4))
        {
        case 0:
            addCustomSpawner(world, x, y, z, "Spider");
            break;
        case 1:
            addCustomSpawner(world, x, y, z, "Skeleton");
            break;
        case 2:
            addCustomSpawner(world, x, y, z, "CaveSpider");
            break;
        default:
            addCustomSpawner(world, x, y, z, "Zombie");
            break;
        }
    }

    private void addHardMobSpawn(World world, Random random, int x, int y, int z)
    {
        switch (random.nextInt(4))
        {
        case 0:
            addCustomSpawner(world, x, y, z, "Creeper");
            break;
        case 1:
            addCustomSpawner(world, x, y, z, "CaveSpider");
            break;
        case 2:
            addCustomSpawner(world, x, y, z, "Skeleton");
            break;
        default:
            addCustomSpawner(world, x, y, z, "Blaze");
            break;
        }
    }

    private void addUprightMobSpawn(World world, Random random, int x, int y, int z)
    {
        switch (random.nextInt(3))
        {
        case 0:
            addCustomSpawner(world, x, y, z, "Creeper");
            break;
        case 1:
            addCustomSpawner(world, x, y, z, "Skeleton");
            break;
        default:
            addCustomSpawner(world, x, y, z, "Zombie");
            break;
        }
    }

    private void addEasyChest(World world, Random random, int x, int y, int z, int meta, int items)
    {
        world.setBlockState(new BlockPos(x, y, z), Blocks.CHEST.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            ItemStack stack;
            for (int i = 0; i < items; i++)
            {
                stack = getNormalStack(random);
                if (stack != null)
                {
                    chest.setInventorySlotContents(random.nextInt(chest.getSizeInventory()), stack);
                }
            }
        }
    }

    private void addMediumChest(World world, Random random, int x, int y, int z, int meta, int items)
    {
        world.setBlockState(new BlockPos(x, y, z), Blocks.CHEST.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            ItemStack stack;
            for (int i = 0; i < items; i++)
            {
                if (random.nextInt(20) < 19)
                {
                    stack = getNormalStack(random);
                }
                else
                {
                    stack = getLootStack(random);
                }
                if (stack != null)
                {
                    chest.setInventorySlotContents(random.nextInt(chest.getSizeInventory()), stack);
                }
            }
        }
    }

    private void addHardChest(World world, Random random, int x, int y, int z, int meta, int items)
    {
        world.setBlockState(new BlockPos(x, y, z), Blocks.CHEST.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            ItemStack stack;
            for (int i = 0; i < items; i++)
            {
                if (random.nextInt(10) < 9)
                {
                    stack = getNormalStack(random);
                }
                else
                {
                    stack = getLootStack(random);
                }
                if (stack != null)
                {
                    chest.setInventorySlotContents(random.nextInt(chest.getSizeInventory()), stack);
                }
            }
        }
    }

    private void addChestGenChest(World world, Random random, int x, int y, int z, String gen, int targetCount, int meta)
    {
        world.setBlockState(new BlockPos(x, y, z), Blocks.CHEST.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            ResourceLocation lootTable = new ResourceLocation("minecraft", gen);
            // chest.setLoot(lootTable, random.nextLong());

            LootTable loottable = world.getLootTableManager().getLootTableFromLocation(lootTable);

            LootContext.Builder lootContextBuilder = new LootContext.Builder((WorldServer) world);

            List<ItemStack> lootFromPools = loottable.generateLootForPools(random, lootContextBuilder.build());
            int toRemove = lootFromPools.size() - targetCount;
            debugPrinter.println("addChestGenChest running with gen[" + gen + "], loot pool size " + lootFromPools.size() + ", targetCount " + targetCount);
            if (targetCount > 0 && toRemove > 0)
            {
                Collections.shuffle(lootFromPools);
                Iterator<ItemStack> iter = lootFromPools.iterator();
                while (toRemove > 0)
                {
                    iter.next();
                    iter.remove();
                    toRemove--;
                }
            }
            debugPrinter.println("addChestGenChest post removal loot pool size " + lootFromPools.size());

            List<Integer> emptySlotsRandomized = getEmptySlotsRandomized(chest, random);
            shuffleItems(lootFromPools, emptySlotsRandomized.size(), random);

            for (ItemStack itemstack : lootFromPools)
            {
                if (emptySlotsRandomized.isEmpty())
                {
                    return;
                }

                if (itemstack == null)
                {
                    chest.setInventorySlotContents(emptySlotsRandomized.remove(emptySlotsRandomized.size() - 1), null);
                }
                else
                {
                    chest.setInventorySlotContents(emptySlotsRandomized.remove(emptySlotsRandomized.size() - 1), itemstack);
                }
            }
        }
    }

    private List<Integer> getEmptySlotsRandomized(IInventory inventory, Random rand)
    {
        List<Integer> list = Lists.<Integer> newArrayList();

        for (int i = 0; i < inventory.getSizeInventory(); ++i)
        {
            if (inventory.getStackInSlot(i) == null)
            {
                list.add(i);
            }
        }

        Collections.shuffle(list, rand);
        return list;
    }

    private void shuffleItems(List<ItemStack> stacks, int targetSize, Random rand)
    {
        List<ItemStack> list = Lists.<ItemStack> newArrayList();
        Iterator<ItemStack> iterator = stacks.iterator();

        while (iterator.hasNext())
        {
            ItemStack itemstack = iterator.next();

            if (itemstack.getCount() <= 0)
            {
                iterator.remove();
            }
            else if (itemstack.getCount() > 1)
            {
                list.add(itemstack);
                iterator.remove();
            }
        }

        targetSize = targetSize - stacks.size();

        while (targetSize > 0 && list.size() > 0)
        {
            ItemStack itemstack2 = list.remove(MathHelper.getInt(rand, 0, list.size() - 1));
            int i = MathHelper.getInt(rand, 1, itemstack2.getCount() / 2);
            itemstack2.shrink(i);
            ItemStack itemstack1 = itemstack2.copy();
            itemstack1.setCount(i);

            if (itemstack2.getCount() > 1 && rand.nextBoolean())
            {
                list.add(itemstack2);
            }
            else
            {
                stacks.add(itemstack2);
            }

            if (itemstack1.getCount() > 1 && rand.nextBoolean())
            {
                list.add(itemstack1);
            }
            else
            {
                stacks.add(itemstack1);
            }
        }

        stacks.addAll(list);
        Collections.shuffle(stacks, rand);
    }

    private void addIInventoryBlock(World world, Random random, int x, int y, int z, Block block, String itemDataWithoutNBT, ArrayList<String> nbtTags, int rotateMetadata)
    {
        world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(rotateMetadata), 2);
        TileEntity te = world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (te instanceof IInventory)
        {
            if (excessiveDebugging)
            {
                debugPrinter.println("About to construct IInventory, itemData [" + itemDataWithoutNBT + "]");
            }

            if (itemDataWithoutNBT.startsWith("ChestGenHook:")) // ChestGenHook:dungeonChest:5
            {
                String[] input = itemDataWithoutNBT.split(":");
                ResourceLocation lootTable = new ResourceLocation("minecraft", input[1]);
                LootTable loottable = world.getLootTableManager().getLootTableFromLocation(lootTable);
                LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) world);
                loottable.fillInventory((IInventory) te, random, lootcontext$builder.build());
            }
            else
            {
                handleIInventory((IInventory) te, itemDataWithoutNBT, nbtTags);
            }
        }
        else
        {
            System.err.println("Ruins Mod could not find IInventory instance for [" + block + "] in Ruin template: " + owner.getName());
        }
    }

    private void handleIInventory(IInventory inv, String itemDataWithoutNBT, ArrayList<String> nbtTags)
    {
        // example string:
        // minecraft:stone#1#4#0+minecraft:written_book#NBT1#0#1+minecraft:chest#1#0#2
        ItemStack putItem;
        ItemStack slotItemPrev;
        String[] itemStrings = itemDataWithoutNBT.split(Pattern.quote("+"));
        String[] hashsplit;
        Object o;
        debugPrinter.println("itemStrings length: " + itemStrings.length);
        for (String itemstring : itemStrings)
        {
            debugPrinter.println("itemString: " + itemstring);
            hashsplit = itemstring.split("#");

            int itemStackSize = 1;
            boolean nbtdata = false;
            if (hashsplit.length > 1)
            {
                nbtdata = hashsplit[1].startsWith("NBT");
                if (!nbtdata)
                {
                    itemStackSize = Integer.valueOf(hashsplit[1]);
                }
            }

            int itemMeta = hashsplit.length > 2 ? Integer.valueOf(hashsplit[2]) : 0;
            int targetslot = hashsplit.length > 3 ? Integer.valueOf(hashsplit[3]) : -1;
            o = tryFindingObject(hashsplit[0]);
            debugPrinter.println(hashsplit[0] + " resolved to object " + o);

            putItem = null;
            if (o instanceof Block)
            {
                putItem = new ItemStack(((Block) o), itemStackSize, itemMeta);
            }
            else if (o instanceof Item)
            {
                putItem = new ItemStack(((Item) o), itemStackSize, itemMeta);
            }
            debugPrinter.println("itemstack instance: " + putItem);

            if (putItem != null)
            {
                if (nbtdata)
                {
                    try
                    {
                        hashsplit[1] = restoreNBTTags(hashsplit[1], nbtTags);
                        putItem.setTagCompound(JsonToNBT.getTagFromJson(hashsplit[1]));
                        debugPrinter.println("nbt tag applied: " + hashsplit[1]);
                    }
                    catch (NBTException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (targetslot != -1)
                {
                    slotItemPrev = inv.getStackInSlot(targetslot);
                    if (slotItemPrev == null)
                    {
                        inv.setInventorySlotContents(targetslot, putItem);
                    }
                    else if (slotItemPrev.isItemEqual(putItem))
                    {
                        int freeSize = slotItemPrev.getMaxStackSize() - slotItemPrev.getCount();
                        if (freeSize >= putItem.getCount())
                        {
                            slotItemPrev.grow(putItem.getCount());
                        }
                        else
                        {
                            slotItemPrev.grow(freeSize);
                        }
                    }
                }
                else
                {
                    for (int slot = 0; slot < inv.getSizeInventory(); slot++)
                    {
                        slotItemPrev = inv.getStackInSlot(slot);
                        if (slotItemPrev == null)
                        {
                            inv.setInventorySlotContents(slot, putItem);
                            break;
                        }
                        else if (slotItemPrev.isItemEqual(putItem))
                        {
                            int freeSize = slotItemPrev.getMaxStackSize() - slotItemPrev.getCount();
                            if (freeSize >= putItem.getCount())
                            {
                                slotItemPrev.grow(putItem.getCount());
                                break;
                            }
                            else
                            {
                                slotItemPrev.grow(freeSize);
                                putItem.shrink(freeSize);
                            }
                        }
                    }
                }
            }
        }
    }

    private Object tryFindingObject(String s)
    {
        ResourceLocation rl = new ResourceLocation(s);
        Item item = Item.REGISTRY.getObject(rl);
        if (item != null)
        {
            if (item instanceof ItemBlock)
            {
                return ((ItemBlock) item).block;
            }
            return item;
        }

        Block block = Block.REGISTRY.getObject(rl);
        if (block != Blocks.AIR)
        {
            return block;
        }
        return null;
    }

    private void addCommandBlock(World world, int x, int y, int z, String command, String sender, int meta, int rotate)
    {
        meta = rotate != RuinsMod.DIR_NORTH ? rotateMetadata(Blocks.COMMAND_BLOCK, meta, rotate) : meta;
        world.setBlockState(new BlockPos(x, y, z), Blocks.COMMAND_BLOCK.getStateFromMeta(meta), 2);
        command = findAndRotateRelativeCommandBlockCoords(command, rotate);
        TileEntityCommandBlock tecb = (TileEntityCommandBlock) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (tecb != null)
        {
            tecb.getCommandBlockLogic().setCommand(command);
            tecb.getCommandBlockLogic().setName(sender);
        }
    }

    private String findAndRotateRelativeCommandBlockCoords(String command, int rotate)
    {
        if (rotate != RuinsMod.DIR_NORTH)
        {
            if (excessiveDebugging)
            {
                debugPrinter.println("About to parse command block command [" + command + "] for relative coordinates and try to rotate them");
            }
            /*
             * regex pattern to find each coordinate triple with at least x and
             * z relative (with tilde), save xz numbers and y in groups
             */
            final Pattern coordinates = Pattern.compile("~(-?\\d*) (~?-?\\d*) ~(-?\\d*)");
            final Matcher coordinateMatcher = coordinates.matcher(command);
            final StringBuffer stringBuffer = new StringBuffer();
            /* for each pattern match do */
            while (coordinateMatcher.find())
            {
                if (excessiveDebugging)
                {
                    debugPrinter.println("Found contained coordinate triple: " + coordinateMatcher.group());
                }
                if (rotate == RuinsMod.DIR_EAST)
                {
                    // z multiplied with -1 becomes x, x becomes z
                    coordinateMatcher.appendReplacement(stringBuffer, String.format("~%s %s ~%s", (tryToInvert(coordinateMatcher.group(3))), coordinateMatcher.group(2), coordinateMatcher.group(1)));
                }
                else if (rotate == RuinsMod.DIR_WEST)
                {
                    // x multiplied with -1 becomes z, z becomes x
                    coordinateMatcher.appendReplacement(stringBuffer, String.format("~%s %s ~%s", coordinateMatcher.group(3), coordinateMatcher.group(2), tryToInvert(coordinateMatcher.group(1))));
                }
                else
                {
                    // case DIR_SOUTH, just swap x and z numbers
                    coordinateMatcher.appendReplacement(stringBuffer, String.format("~%s %s ~%s", coordinateMatcher.group(3), coordinateMatcher.group(2), coordinateMatcher.group(1)));
                }
            }
            /*
             * rebuild the pattern with the changes. we have the technology. we
             * can make it better
             */
            coordinateMatcher.appendTail(stringBuffer);
            final String result = stringBuffer.toString();
            if (excessiveDebugging)
            {
                debugPrinter.println("Command Block command with rotated coords: [" + result + "]");
            }
            return result;
        }
        return command;
    }

    /**
     * Possible inputs are "", "x", "-x" with int x
     *
     * @param maybeNumber
     *            input string
     * @return "-input" or "" if not applicable
     */
    private String tryToInvert(String maybeNumber)
    {
        if (!maybeNumber.isEmpty())
        {
            return Integer.toString(Integer.parseInt(maybeNumber) * -1);
        }
        return "";
    }

    private ItemStack getNormalStack(Random random)
    {
        int rand = random.nextInt(25);
        switch (rand)
        {
        case 0:
        case 1:
            return null;
        case 2:
        case 3:
            return new ItemStack(Items.BREAD);
        case 4:
        case 5:
            return new ItemStack(Items.WHEAT, random.nextInt(8) + 8);
        case 6:
            return new ItemStack(Items.IRON_HOE);
        case 7:
            return new ItemStack(Items.IRON_SHOVEL);
        case 8:
        case 9:
            return new ItemStack(Items.STRING, random.nextInt(3) + 1);
        case 10:
        case 11:
        case 12:
            return new ItemStack(Items.WHEAT_SEEDS, random.nextInt(8) + 8);
        case 13:
        case 14:
        case 15:
            return new ItemStack(Items.BOWL, random.nextInt(2) + 1);
        case 16:
            return new ItemStack(Items.BUCKET);
        case 17:
            return new ItemStack(Items.APPLE);
        case 18:
        case 19:
            return new ItemStack(Items.BONE, random.nextInt(4) + 1);
        case 20:
        case 21:
            return new ItemStack(Items.EGG, random.nextInt(2) + 1);
        case 22:
            return new ItemStack(Items.COAL, random.nextInt(5) + 3);
        case 23:
            return new ItemStack(Items.IRON_INGOT, random.nextInt(5) + 3);
        default:
            return getLootStack(random);
        }
    }

    private ItemStack getLootStack(Random random)
    {
        int rand = random.nextInt(25);
        switch (rand)
        {
        case 0:
        case 1:
        case 2:
        case 3:
            return null;
        case 4:
        case 5:
            return new ItemStack(Items.LEATHER_BOOTS);
        case 6:
        case 7:
            return new ItemStack(Items.LEATHER_LEGGINGS);
        case 8:
        case 9:
            return new ItemStack(Items.FLINT_AND_STEEL);
        case 10:
        case 11:
            return new ItemStack(Items.IRON_AXE);
        case 12:
            return new ItemStack(Items.IRON_SWORD);
        case 13:
            return new ItemStack(Items.IRON_PICKAXE);
        case 14:
        case 15:
            return new ItemStack(Items.IRON_HELMET);
        case 16:
            return new ItemStack(Items.IRON_CHESTPLATE);
        case 17:
        case 18:
            return new ItemStack(Items.BOOK, random.nextInt(3) + 1);
        case 19:
            return new ItemStack(Items.COMPASS);
        case 20:
            return new ItemStack(Items.CLOCK);
        case 21:
            return new ItemStack(Items.REDSTONE, random.nextInt(12) + 12);
        case 22:
            return new ItemStack(Items.GOLDEN_APPLE);
        case 23:
            return new ItemStack(Items.MUSHROOM_STEW, random.nextInt(2) + 1);
        default:
            return new ItemStack(Items.DIAMOND, random.nextInt(4));
        }
    }

    private int rotateMetadata(Block blockID, int metadata, int dir)
    {
        // remember that, in this mod, NORTH is the default direction.
        // this method is unused if the direction is NORTH
        int tempdata = 0;

        if (blockID == Blocks.RAIL || blockID == Blocks.GOLDEN_RAIL || blockID == Blocks.DETECTOR_RAIL || blockID == Blocks.ACTIVATOR_RAIL)
        {
            // minecart tracks
            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                // flat tracks
                if (metadata == 0)
                {
                    return 1;
                }
                if (metadata == 1)
                {
                    return 0;
                }
                // ascending tracks
                if (metadata == 2)
                {
                    return 5;
                }
                if (metadata == 3)
                {
                    return 4;
                }
                if (metadata == 4)
                {
                    return 2;
                }
                if (metadata == 5)
                {
                    return 3;
                }
                // curves
                if (metadata == 6)
                {
                    return 7;
                }
                if (metadata == 7)
                {
                    return 8;
                }
                if (metadata == 8)
                {
                    return 9;
                }
                if (metadata == 9)
                {
                    return 6;
                }
            case RuinsMod.DIR_SOUTH:
                // flat tracks
                if (metadata == 0)
                {
                    return 0;
                }
                if (metadata == 1)
                {
                    return 1;
                }
                // ascending tracks
                if (metadata == 2)
                {
                    return 3;
                }
                if (metadata == 3)
                {
                    return 2;
                }
                if (metadata == 4)
                {
                    return 5;
                }
                if (metadata == 5)
                {
                    return 4;
                }
                // curves
                if (metadata == 6)
                {
                    return 8;
                }
                if (metadata == 7)
                {
                    return 9;
                }
                if (metadata == 8)
                {
                    return 6;
                }
                if (metadata == 9)
                {
                    return 7;
                }
            case RuinsMod.DIR_WEST:
                // flat tracks
                if (metadata == 0)
                {
                    return 1;
                }
                if (metadata == 1)
                {
                    return 0;
                }
                // ascending tracks
                if (metadata == 2)
                {
                    return 4;
                }
                if (metadata == 3)
                {
                    return 5;
                }
                if (metadata == 4)
                {
                    return 3;
                }
                if (metadata == 5)
                {
                    return 2;
                }
                // curves
                if (metadata == 6)
                {
                    return 9;
                }
                if (metadata == 7)
                {
                    return 6;
                }
                if (metadata == 8)
                {
                    return 7;
                }
                if (metadata == 9)
                {
                    return 8;
                }
            }
        }
        else if (blockID == Blocks.DARK_OAK_DOOR || blockID == Blocks.IRON_DOOR)
        {
            // doors
            if (metadata - 8 >= 0)
            {
                // the top half of the door
                tempdata += 8;
                metadata -= 8;
            }
            if (metadata - 4 >= 0)
            {
                // the door has swung counterclockwise around its hinge
                tempdata += 4;
                metadata -= 4;
            }
            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (metadata == 0)
                {
                    return 1 + tempdata;
                }
                if (metadata == 1)
                {
                    return 2 + tempdata;
                }
                if (metadata == 2)
                {
                    return 3 + tempdata;
                }
                if (metadata == 3)
                {
                    return tempdata;
                }
            case RuinsMod.DIR_SOUTH:
                if (metadata == 0)
                {
                    return 2 + tempdata;
                }
                if (metadata == 1)
                {
                    return 3 + tempdata;
                }
                if (metadata == 2)
                {
                    return tempdata;
                }
                if (metadata == 3)
                {
                    return 1 + tempdata;
                }
            case RuinsMod.DIR_WEST:
                if (metadata == 0)
                {
                    return 3 + tempdata;
                }
                if (metadata == 1)
                {
                    return tempdata;
                }
                if (metadata == 2)
                {
                    return 1 + tempdata;
                }
                if (metadata == 3)
                {
                    return 2 + tempdata;
                }
            }
        }
        else if (blockID == Blocks.TORCH || blockID == Blocks.STONE_BUTTON || blockID == Blocks.WOODEN_BUTTON || blockID == Blocks.LEVER || blockID == Blocks.UNLIT_REDSTONE_TORCH
                || blockID == Blocks.REDSTONE_TORCH)
        {
            tempdata = 0;
            if (blockID == Blocks.LEVER || blockID == Blocks.STONE_BUTTON || blockID == Blocks.WOODEN_BUTTON)
            {
                if (metadata - 8 > 0)
                {
                    tempdata += 8;
                    metadata -= 8;
                }
                // now see if it's a floor switch
                if (blockID == Blocks.LEVER && (metadata == 5 || metadata == 6))
                {
                    // we'll leave this as-is
                    return metadata + tempdata;
                }
            }
            else
            {
                // torches on the floor.
                if (metadata == 5)
                {
                    return metadata;
                }
            }
            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (metadata == 1)
                {
                    return 3 + tempdata;
                }
                if (metadata == 2)
                {
                    return 4 + tempdata;
                }
                if (metadata == 3)
                {
                    return 2 + tempdata;
                }
                if (metadata == 4)
                {
                    return 1 + tempdata;
                }
            case RuinsMod.DIR_SOUTH:
                if (metadata == 1)
                {
                    return 2 + tempdata;
                }
                if (metadata == 2)
                {
                    return 1 + tempdata;
                }
                if (metadata == 3)
                {
                    return 4 + tempdata;
                }
                if (metadata == 4)
                {
                    return 3 + tempdata;
                }
            case RuinsMod.DIR_WEST:
                if (metadata == 1)
                {
                    return 4 + tempdata;
                }
                if (metadata == 2)
                {
                    return 3 + tempdata;
                }
                if (metadata == 3)
                {
                    return 1 + tempdata;
                }
                if (metadata == 4)
                {
                    return 2 + tempdata;
                }
            }
        }
        else if (blockID == Blocks.VINE)
        {
            /*
             * meta readout N: 8 E: 1 S: 2 W: 4
             */
            // Vines
            switch (dir)
            {
            case RuinsMod.DIR_EAST: // turn one right
                if (metadata == 8)
                {
                    return 1;
                }
                if (metadata == 1)
                {
                    return 2;
                }
                if (metadata == 2)
                {
                    return 4;
                }
                if (metadata == 4)
                {
                    return 8;
                }
            case RuinsMod.DIR_SOUTH: // run 2 right
                if (metadata == 8)
                {
                    return 2;
                }
                if (metadata == 1)
                {
                    return 4;
                }
                if (metadata == 2)
                {
                    return 8;
                }
                if (metadata == 4)
                {
                    return 1;
                }
            case RuinsMod.DIR_WEST: // turn 1 left
                if (metadata == 8)
                {
                    return 4;
                }
                if (metadata == 1)
                {
                    return 8;
                }
                if (metadata == 2)
                {
                    return 1;
                }
                if (metadata == 4)
                {
                    return 2;
                }
            }
        }
        /*
         * pumpkins NESW - 2 3 0 1
         */
        else if (blockID == Blocks.PUMPKIN || blockID == Blocks.LIT_PUMPKIN)
        {
            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (metadata == 0)
                {
                    return 1 + tempdata;
                }
                if (metadata == 1)
                {
                    return 2 + tempdata;
                }
                if (metadata == 2)
                {
                    return 3 + tempdata;
                }
                if (metadata == 3)
                {
                    return tempdata;
                }
            case RuinsMod.DIR_SOUTH:
                if (metadata == 0)
                {
                    return 2 + tempdata;
                }
                if (metadata == 1)
                {
                    return 3 + tempdata;
                }
                if (metadata == 2)
                {
                    return tempdata;
                }
                if (metadata == 3)
                {
                    return 1 + tempdata;
                }
            case RuinsMod.DIR_WEST:
                if (metadata == 0)
                {
                    return 3 + tempdata;
                }
                if (metadata == 1)
                {
                    return tempdata;
                }
                if (metadata == 2)
                {
                    return 1 + tempdata;
                }
                if (metadata == 3)
                {
                    return 2 + tempdata;
                }
            }
        }
        else if (blockID == Blocks.BED)
        {
            if (metadata - 8 >= 0)
            {
                // this is the foot of the bed block.
                tempdata += 8;
                metadata -= 8;
            }
            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (metadata == 0)
                {
                    return 1 + tempdata;
                }
                if (metadata == 1)
                {
                    return 2 + tempdata;
                }
                if (metadata == 2)
                {
                    return 3 + tempdata;
                }
                if (metadata == 3)
                {
                    return tempdata;
                }
            case RuinsMod.DIR_SOUTH:
                if (metadata == 0)
                {
                    return 2 + tempdata;
                }
                if (metadata == 1)
                {
                    return 3 + tempdata;
                }
                if (metadata == 2)
                {
                    return tempdata;
                }
                if (metadata == 3)
                {
                    return 1 + tempdata;
                }
            case RuinsMod.DIR_WEST:
                if (metadata == 0)
                {
                    return 3 + tempdata;
                }
                if (metadata == 1)
                {
                    return tempdata;
                }
                if (metadata == 2)
                {
                    return 1 + tempdata;
                }
                if (metadata == 3)
                {
                    return 2 + tempdata;
                }
            }
        }
        else if (blockID == Blocks.STANDING_SIGN)
        {
            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (metadata == 0)
                {
                    return 4;
                }
                if (metadata == 1)
                {
                    return 5;
                }
                if (metadata == 2)
                {
                    return 6;
                }
                if (metadata == 3)
                {
                    return 7;
                }
                if (metadata == 4)
                {
                    return 8;
                }
                if (metadata == 5)
                {
                    return 9;
                }
                if (metadata == 6)
                {
                    return 10;
                }
                if (metadata == 7)
                {
                    return 11;
                }
                if (metadata == 8)
                {
                    return 12;
                }
                if (metadata == 9)
                {
                    return 13;
                }
                if (metadata == 10)
                {
                    return 14;
                }
                if (metadata == 11)
                {
                    return 15;
                }
                if (metadata == 12)
                {
                    return 0;
                }
                if (metadata == 13)
                {
                    return 1;
                }
                if (metadata == 14)
                {
                    return 2;
                }
                if (metadata == 15)
                {
                    return 3;
                }
            case RuinsMod.DIR_SOUTH:
                if (metadata == 0)
                {
                    return 8;
                }
                if (metadata == 1)
                {
                    return 9;
                }
                if (metadata == 2)
                {
                    return 10;
                }
                if (metadata == 3)
                {
                    return 11;
                }
                if (metadata == 4)
                {
                    return 12;
                }
                if (metadata == 5)
                {
                    return 13;
                }
                if (metadata == 6)
                {
                    return 14;
                }
                if (metadata == 7)
                {
                    return 15;
                }
                if (metadata == 8)
                {
                    return 0;
                }
                if (metadata == 9)
                {
                    return 1;
                }
                if (metadata == 10)
                {
                    return 2;
                }
                if (metadata == 11)
                {
                    return 3;
                }
                if (metadata == 12)
                {
                    return 4;
                }
                if (metadata == 13)
                {
                    return 5;
                }
                if (metadata == 14)
                {
                    return 6;
                }
                if (metadata == 15)
                {
                    return 7;
                }
            case RuinsMod.DIR_WEST:
                if (metadata == 0)
                {
                    return 12;
                }
                if (metadata == 1)
                {
                    return 13;
                }
                if (metadata == 2)
                {
                    return 14;
                }
                if (metadata == 3)
                {
                    return 15;
                }
                if (metadata == 4)
                {
                    return 0;
                }
                if (metadata == 5)
                {
                    return 1;
                }
                if (metadata == 6)
                {
                    return 2;
                }
                if (metadata == 7)
                {
                    return 3;
                }
                if (metadata == 8)
                {
                    return 4;
                }
                if (metadata == 9)
                {
                    return 5;
                }
                if (metadata == 10)
                {
                    return 6;
                }
                if (metadata == 11)
                {
                    return 7;
                }
                if (metadata == 12)
                {
                    return 8;
                }
                if (metadata == 13)
                {
                    return 9;
                }
                if (metadata == 14)
                {
                    return 10;
                }
                if (metadata == 15)
                {
                    return 11;
                }
            }
        }
        /*
         * Base NESW = 0 1 2 3 in 2 least significant bits Additonal data
         * unrelated to rotation in higher bits
         */
        else if (blockID == Blocks.UNPOWERED_REPEATER || blockID == Blocks.UNPOWERED_COMPARATOR || blockID == Blocks.POWERED_REPEATER || blockID == Blocks.POWERED_COMPARATOR)
        {
            int rotbits = metadata & 0x03;
            int databits = metadata & 0xFC;

            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (rotbits == 0)
                {
                    return 1 | databits;
                }
                if (rotbits == 1)
                {
                    return 2 | databits;
                }
                if (rotbits == 2)
                {
                    return 3 | databits;
                }
                if (rotbits == 3)
                {
                    return databits;
                }
            case RuinsMod.DIR_SOUTH:
                if (rotbits == 0)
                {
                    return 2 | databits;
                }
                if (rotbits == 1)
                {
                    return 3 | databits;
                }
                if (rotbits == 2)
                {
                    return databits;
                }
                if (rotbits == 3)
                {
                    return 1 | databits;
                }
            case RuinsMod.DIR_WEST:
                if (rotbits == 0)
                {
                    return 3 | databits;
                }
                if (rotbits == 1)
                {
                    return databits;
                }
                if (rotbits == 2)
                {
                    return 1 | databits;
                }
                if (rotbits == 3)
                {
                    return 2 | databits;
                }
            }
        }
        /*
         * Least significant 2 bits cover rotation, rest is data (connected to:)
         * N E S W -> 1 2 0 3
         */
        if (blockID == Blocks.TRAPDOOR)
        {
            int rotbits = metadata & 0x03;
            int databits = metadata & 0xFC;

            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (rotbits == 1)
                {
                    return 2 | databits;
                }
                if (rotbits == 2)
                {
                    return databits;
                }
                if (rotbits == 0)
                {
                    return 3 | databits;
                }
                if (rotbits == 3)
                {
                    return 1 | databits;
                }
            case RuinsMod.DIR_SOUTH:
                if (rotbits == 1)
                {
                    return databits;
                }
                if (rotbits == 2)
                {
                    return 3 | databits;
                }
                if (rotbits == 0)
                {
                    return 1 | databits;
                }
                if (rotbits == 3)
                {
                    return 2 | databits;
                }
            case RuinsMod.DIR_WEST:
                if (rotbits == 1)
                {
                    return 3 | databits;
                }
                if (rotbits == 2)
                {
                    return 1 | databits;
                }
                if (rotbits == 0)
                {
                    return 2 | databits;
                }
                if (rotbits == 3)
                {
                    return databits;
                }
            }
        }
        /*
         * Least significant 2 bits cover rotation, rest is data (connected to:)
         * N E S W -> 2 3 0 1
         */
        if (blockID == Blocks.TRIPWIRE_HOOK || blockID == Blocks.DARK_OAK_FENCE_GATE || blockID == Blocks.END_PORTAL_FRAME)
        {
            int rotbits = metadata & 0x03;
            int databits = metadata & 0xFC;

            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (rotbits == 2)
                {
                    return 3 | databits;
                }
                if (rotbits == 3)
                {
                    return databits;
                }
                if (rotbits == 0)
                {
                    return 1 | databits;
                }
                if (rotbits == 1)
                {
                    return 2 | databits;
                }
            case RuinsMod.DIR_SOUTH:
                if (rotbits == 2)
                {
                    return databits;
                }
                if (rotbits == 3)
                {
                    return 1 | databits;
                }
                if (rotbits == 0)
                {
                    return 2 | databits;
                }
                if (rotbits == 1)
                {
                    return 3 | databits;
                }
            case RuinsMod.DIR_WEST:
                if (rotbits == 2)
                {
                    return 1 | databits;
                }
                if (rotbits == 3)
                {
                    return 2 | databits;
                }
                if (rotbits == 0)
                {
                    return 3 | databits;
                }
                if (rotbits == 1)
                {
                    return databits;
                }
            }
        }
        /*
         * Least significant 2 bits cover rotation, rest is data (connected to:)
         * N E S W -> 0 1 2 3
         */
        if (blockID == Blocks.COCOA)
        {
            int rotbits = metadata & 0x03;
            int databits = metadata & 0xFC;

            switch (dir)
            {
            case RuinsMod.DIR_EAST:
                if (rotbits == 0)
                {
                    return 1 | databits;
                }
                if (rotbits == 1)
                {
                    return 2 | databits;
                }
                if (rotbits == 2)
                {
                    return 3 | databits;
                }
                if (rotbits == 3)
                {
                    return databits;
                }
            case RuinsMod.DIR_SOUTH:
                if (rotbits == 0)
                {
                    return 2 | databits;
                }
                if (rotbits == 1)
                {
                    return 3 | databits;
                }
                if (rotbits == 2)
                {
                    return databits;
                }
                if (rotbits == 3)
                {
                    return 1 | databits;
                }
            case RuinsMod.DIR_WEST:
                if (rotbits == 0)
                {
                    return 3 | databits;
                }
                if (rotbits == 1)
                {
                    return databits;
                }
                if (rotbits == 2)
                {
                    return 1 | databits;
                }
                if (rotbits == 3)
                {
                    return 2 | databits;
                }
            }
        }
        /*
         * Least significant bit covers rotation NS or EW, rest is data
         */
        if (blockID == Blocks.ANVIL)
        {
            int rotbits = metadata & 0x01;
            int databits = metadata & 0xFE;

            switch (dir)
            {
            case RuinsMod.DIR_EAST:
            case RuinsMod.DIR_WEST:
                if (rotbits == 0)
                {
                    return 1 | databits;
                }
                return databits;
            }
            return metadata;
        }
        /*
         * 3 Orientations: UP/DOWN, N/S, E/W various wood types: 0-3, 4-7, 8-11
         * and 'only bark' variants 12-15 can be simplified to 0 1 2 3
         */
        if (blockID == Blocks.LOG || blockID == Blocks.LOG2)
        {
            int offset = metadata % 4; // for wood type
            int rot = metadata / 4; // simplify to meta-meta
            if (rot == 0 || rot > 2) // up/down or just bark, no rotation
            {
                return metadata;
            }

            switch (dir)
            {
            case RuinsMod.DIR_EAST:
            case RuinsMod.DIR_WEST:
                if (rot == 1)
                {
                    // go from N/S to E/W
                    return 8 + offset;
                }
                // go from E/W to N/S
                return 4 + offset;
            }
            return metadata;
        }

        return CustomRotationMapping.getMapping(blockID, metadata, dir);
    }

}