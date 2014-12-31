package atomicstryker.ruins.common;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import com.mojang.authlib.GameProfile;

public class RuinTemplateRule
{
    private final Block[] blockIDs;
    private final int[] blockMDs;
    private final String[] blockStrings;
    private final SpecialFlags[] specialFlags;
    private int chance = 100;
    private int condition = 0;
    protected final RuinTemplate owner;
    private final PrintWriter debugPrinter;
    private final boolean excessiveDebugging;
    
    // just leave the field null for NONE
    public enum SpecialFlags
    {
        COMMANDBLOCK,
        ADDBONEMEAL
    }
    
    public RuinTemplateRule(PrintWriter dpw, RuinTemplate r, final String rule, boolean debug) throws Exception
    {
        debugPrinter = dpw;
        owner = r;
        excessiveDebugging = debug;
        String[] blockRules = rule.split(",");
        int numblocks = blockRules.length - 2;
        if (numblocks < 1)
        {
            throw new Exception("No blockIDs specified for rule [" + rule + "] in template " + owner.getName());
        }
        condition = Integer.parseInt(blockRules[0]);
        chance = Integer.parseInt(blockRules[1]);

        String[] data;
        
        // Command Block special case, contains basically any character that breaks this
        if (blockRules[2].startsWith("CommandBlock:"))
        {
            String[] commandrules = rule.split("CommandBlock:");
            int count = commandrules.length-1; // -1 because there is a prefix part we ignore
            blockIDs = new Block[count];
            blockMDs = new int[count];
            blockStrings = new String[count];
            specialFlags = new SpecialFlags[count];
            for (int i = 0; i < count; i++)
            {
                blockIDs[i] = null;
                blockMDs[i] = 0;
                specialFlags[i] = SpecialFlags.COMMANDBLOCK;
                // readd the splitout string for the parsing, offset by 1 because of the prefix string
                blockStrings[i] = commandrules[i+1];
                debugPrinter.println("template " + owner.getName()+" contains Command Block command: "+blockStrings[i]);
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
                if (data.length > 1) // has '-' in it, like "torch-5" or "planks-3"
                {
                    if (isNumber(data[0])) // torch-5
                    {
                        debugPrinter.println("Rule [" + rule + "] in template " + owner.getName()+" still uses numeric blockIDs! ERROR!");
                        blockIDs[i] = Blocks.air;
                        blockMDs[i] = 0;
                        blockStrings[i] = "";
                    }
                    else
                    // planks-3 or ChestGenHook:strongholdLibrary:5-2
                    {
                        blockStrings[i] = blockRules[i + 2];
                        blockIDs[i] = tryFindingBlockOfName(data[0]);
                        if (blockIDs[i] == Blocks.air)
                        {
                            if (!data[0].equals("air"))
                            {
                                blockIDs[i] = null;
                                if (!isKnownSpecialRule(blockStrings[i]))
                                {
                                    throw new Exception("Rule [" + rule + "] in template " + owner.getName()+" can absolutely not be mapped to anything known");
                                }
                            }
                        }
                        
                        // special case -addbonemeal
                        if (blockIDs[i] instanceof IGrowable)
                        {
                            if (data[data.length-1].equals("addbonemeal"))
                            {
                                specialFlags[i] = SpecialFlags.ADDBONEMEAL;
                                try
                                {
                                    blockMDs[i] = Integer.parseInt(data[data.length-2]);
                                }
                                catch (NumberFormatException ne)
                                {
                                    blockMDs[i] = 0;
                                }
                            }
                        }
                        // otherwise parse meta value
                        else
                        {
                            try
                            {
                                blockMDs[i] = Integer.parseInt(data[data.length-1]);
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
                        debugPrinter.println("Rule [" + rule + "] in template " + owner.getName()+" still uses numeric blockIDs! ERROR!");
                        blockIDs[i] = Blocks.air;
                        blockMDs[i] = 0;
                        blockStrings[i] = "";
                    }
                    else
                    {
                        blockIDs[i] = tryFindingBlockOfName(blockRules[i + 2]);
                        if (blockIDs[i] == Blocks.air && !data[0].equals("air"))
                        {
                            //debugPrinter.println("Rule [" + rule + "] in template " + owner.getName()+" has something special? Checking again later");
                            blockIDs[i] = null;
                        }
                    }
                    blockMDs[i] = 0;
                    blockStrings[i] = blockRules[i + 2];
                }
                
                if (excessiveDebugging)
                {
                    debugPrinter.printf("rule alternative: %d, blockIDs[%s], blockMDs[%s], blockStrings[%s], specialflags:[%s]\n", i+1, blockIDs[i], blockMDs[i], blockStrings[i], specialFlags[i]);
                }
            }
        }
    }
    
    public RuinTemplateRule(PrintWriter dpw, RuinTemplate r, final String rule) throws Exception
    {
        this(dpw, r, rule, false);
    }

    private Block cachedBlock;
    private Block tryFindingBlockOfName(String blockName)
    {
        cachedBlock = GameData.getBlockRegistry().getObject(blockName);
        // debugPrinter.printf("%s mapped to %s\n", blockName, cachedBlock);
        return cachedBlock;
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
        if ((condition <= 0 ? true : false) ^ owner.isIgnoredBlock(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock(), world, x, y - 1, z))
        {
            return;
        }
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void doAdjacentBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        if ((condition <= 0 ? true : false) ^ (
        // Are -all- adjacent blocks air?
                (owner.isIgnoredBlock(world.getBlockState(new BlockPos(x + 1, y, z)).getBlock(), world, x + 1, y, z)) 
                && (owner.isIgnoredBlock(world.getBlockState(new BlockPos(x, y, z + 1)).getBlock(), world, x, y, z + 1))
                && (owner.isIgnoredBlock(world.getBlockState(new BlockPos(x, y, z - 1)).getBlock(), world, x, y, z - 1)) 
                && (owner.isIgnoredBlock(world.getBlockState(new BlockPos(x - 1, y, z)).getBlock(), world, x - 1, y, z))))
        {
            return;
        }
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void doUnderBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        if ((condition <= 0 ? true : false) ^ owner.isIgnoredBlock(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock(), world, x, y + 1, z))
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
            debugPrinter.println("About to place blockID "+blockID+", meta "+blockMDs[blocknum]+" rotation "+rotate+", string: "+blockString);
        }
        if (blockID == null)
        {
            doSpecialBlock(world, random, x, y, z, blocknum, rotate, blockString);
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
            addCustomSpawner(world, x, y, z, dataString.substring(dataString.indexOf(":")+1));
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
            addEasyChest(world, random, x, y, z, rotateMetadata(Blocks.chest, blockMDs[blocknum], rotate), random.nextInt(3) + 3);
        }
        else if (dataString.startsWith("MediumChest"))
        {
            addMediumChest(world, random, x, y, z, rotateMetadata(Blocks.chest, blockMDs[blocknum], rotate), random.nextInt(4) + 3);
        }
        else if (dataString.startsWith("HardChest"))
        {
            addHardChest(world, random, x, y, z, rotateMetadata(Blocks.chest, blockMDs[blocknum], rotate), random.nextInt(5) + 3);
        }
        else if (dataString.startsWith("ChestGenHook:"))
        {
            String[] s = dataString.split(":");
            addChestGenChest(world, random, x, y, z, s[1], Integer.valueOf(s[2].split("-")[0]), rotateMetadata(Blocks.chest, blockMDs[blocknum], rotate));
        }
        else if (dataString.startsWith("IInventory;"))
        {
            String[] s = dataString.split(";");
            Object o = tryFindingObject(s[1]);
            if (o instanceof Block)
            {
                Block b = (Block) o;
                // need to strip meta '-x' value if present
                if (s[2].lastIndexOf("-") > s[2].length()-5)
                {
                    addIInventoryBlock(world, random, x, y, z, b, s[2].substring(0, s[2].lastIndexOf("-")-1), rotateMetadata(b, blockMDs[blocknum], rotate));
                }
                else
                {
                    addIInventoryBlock(world, random, x, y, z, b, s[2], rotateMetadata(b, blockMDs[blocknum], rotate));
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
                sender = dataString.substring(lastIdx+1, dataString.length());
            }
            addCommandBlock(world, x, y, z, command, sender, rotate);
        }
        else if (dataString.startsWith("StandingSign:"))
        {
            String[] splits = dataString.split(":");
            int meta = blockMDs[blocknum];
            if (rotate != RuinsMod.DIR_NORTH)
            {
                meta = rotateMetadata(Blocks.standing_sign, blockMDs[blocknum], rotate);
            }
            world.setBlockState(new BlockPos(x, y, z), Blocks.standing_sign.getStateFromMeta(meta), 2);
            TileEntitySign tes = (TileEntitySign) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
            if (tes != null && tes.signText != null)
            {
                for (int i = 0; i < tes.signText.length && i+1 < splits.length; i++)
                {
                    tes.signText[i] = (splits[i+1].split("-")[0].equals("null")) ? new ChatComponentText("") : new ChatComponentText(splits[i+1].split("-")[0]);
                }
            }
        }
        else if (dataString.startsWith("WallSign:"))
        {
            String[] splits = dataString.split(":");
            int meta = blockMDs[blocknum];
            if (rotate != RuinsMod.DIR_NORTH)
            {
                meta = rotateMetadata(Blocks.wall_sign, blockMDs[blocknum], rotate);
            }
            world.setBlockState(new BlockPos(x, y, z), Blocks.wall_sign.getStateFromMeta(meta), 2);
            TileEntitySign tes = (TileEntitySign) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
            if (tes != null && tes.signText != null)
            {
                for (int i = 0; i < tes.signText.length && i+1 < splits.length; i++)
                {
                	tes.signText[i] = (splits[i+1].split("-")[0].equals("null")) ? new ChatComponentText("") : new ChatComponentText(splits[i+1].split("-")[0]);
                }
            }
        }
        else if (dataString.startsWith("Skull:"))
        {
            // standard case Skull:2:8-3
            world.setBlockState(new BlockPos(x, y, z), Blocks.skull.getStateFromMeta(rotateFloorSkull(blockMDs[blocknum], rotate)), 2);
            String[] splits = dataString.split(":");
            TileEntitySkull tes = (TileEntitySkull) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
            ReflectionHelper.setPrivateValue(TileEntitySkull.class, tes, Integer.valueOf(splits[1]), 0);
            int rot = Integer.valueOf(splits[2].split("-")[0]); // skull te's rotate like standing sign blocks
            ReflectionHelper.setPrivateValue(TileEntitySkull.class, tes, rotateMetadata(Blocks.standing_sign, rot, rotate), 1);
            
            // is a player head saved?
            // looks like Skull:3:8:1b4d8438-e714-3553-a433-059f2d3b1fd2-AtomicStryker-3
            if (splits.length > 3)
            {
                // split segment like this: 1b4d8438-e714-3553-a433-059f2d3b1fd2-AtomicStryker-3
                String[] moresplits = splits[3].split("-");
                UUID id = UUID.fromString(moresplits[0]+"-"+moresplits[1]+"-"+moresplits[2]+"-"+moresplits[3]+"-"+moresplits[4]);
                GameProfile playerprofile = new GameProfile(id, moresplits[5]);
                ReflectionHelper.setPrivateValue(TileEntitySkull.class, tes, playerprofile, 2);
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
        return CustomRotationMapping.getMapping(Blocks.skull, meta, rot);
    }

    private int getBlockNum(Random random)
    {
        return random.nextInt(blockIDs.length);
    }

    private void spawnEnderCrystal(World world, int x, int y, int z)
    {
        EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
        entityendercrystal.setLocationAndAngles((x + 0.5F), y, (z + 0.5F), world.rand.nextFloat() * 360.0F, 0.0F);
        world.spawnEntityInWorld(entityendercrystal);
        world.setBlockState(new BlockPos(x, y, z), Blocks.bedrock.getDefaultState(), 2);
    }

    @SuppressWarnings("unchecked")
    private void addCustomSpawner(World world, int x, int y, int z, String id)
    {
        world.setBlockState(new BlockPos(x, y, z), Blocks.mob_spawner.getDefaultState(), 2);
        TileEntityMobSpawner mobspawner = (TileEntityMobSpawner) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (mobspawner != null)
        {
            Entity test = EntityList.createEntityByName(id, world);
            if (test == null)
            {
                System.err.println("Warning: Ruins Mod could not find an Entity ["+id+"] set for a Mob Spawner");
                for (String entString : (Collection<String>)EntityList.classToStringMapping.values())
                {
                    if (entString.contains(id))
                    {
                        id = entString;
                        System.err.println("Ruins Mod going close match ["+id+"]");
                        break;
                    }
                }
            }
            mobspawner.getSpawnerBaseLogic().setEntityName(id);
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
        world.setBlockState(new BlockPos(x, y, z), Blocks.chest.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            ItemStack stack = null;
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
        world.setBlockState(new BlockPos(x, y, z), Blocks.chest.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            ItemStack stack = null;
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
        world.setBlockState(new BlockPos(x, y, z), Blocks.chest.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            ItemStack stack = null;
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

    private void addChestGenChest(World world, Random random, int x, int y, int z, String gen, int items, int meta)
    {
        world.setBlockState(new BlockPos(x, y, z), Blocks.chest.getStateFromMeta(meta), 2);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (chest != null)
        {
            List<WeightedRandomChestContent> list = ChestGenHooks.getInfo(gen).getItems(random);
            if (WeightedRandom.getTotalWeight(list) > 0)
            {
            	WeightedRandomChestContent.generateChestContents(random, list, chest, items);
            }
        }
    }
    
    private void addIInventoryBlock(World world, Random random, int x, int y, int z, Block block, String itemData, int rotateMetadata)
    {
        world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(rotateMetadata), 2);
        TileEntity te = world.getTileEntity(new BlockPos(new BlockPos(x, y, z)));
        if (te instanceof IInventory)
        {
            if (excessiveDebugging)
            {
                debugPrinter.println("About to construct IInventory, itemData ["+itemData+"]");
            }
            handleIInventory((IInventory) te, itemData);
        }
        else
        {
            System.err.println("Ruins Mod could not find IInventory instance for [" + block + "] in Ruin template: " + owner.getName());
        }
    }
    
    private void handleIInventory(IInventory inv, String itemData)
    {
        ItemStack putItem;
        ItemStack slotItemPrev;
        String[] itemStrings = itemData.split(Pattern.quote("+"));
        String[] hashsplit;
        Object o;
        debugPrinter.println("itemStrings length: "+itemStrings.length);
        for (String itemstring : itemStrings)
        {
            debugPrinter.println("itemString: "+itemstring);
            hashsplit = itemstring.split("#");
            int itemStackSize = hashsplit.length > 1 ? Integer.valueOf(hashsplit[1]) : 1;
            int itemMeta = hashsplit.length > 2 ? Integer.valueOf(hashsplit[2]) : 0;
            int targetslot = hashsplit.length > 3 ? Integer.valueOf(hashsplit[3]) : -1;
            o = tryFindingObject(hashsplit[0]);
            
            putItem = null;
            if (o instanceof Block)
            {
                putItem = new ItemStack(((Block)o), itemStackSize, itemMeta);
            }
            else if (o instanceof Item)
            {
                putItem = new ItemStack(((Item)o), itemStackSize, itemMeta);
            }
            
            if (putItem != null)
            {
                if (targetslot != -1)
                {
                    slotItemPrev = inv.getStackInSlot(targetslot);
                    if (slotItemPrev == null)
                    {
                        inv.setInventorySlotContents(targetslot, putItem);
                        continue;
                    }
                    else if (slotItemPrev.isItemEqual(putItem))
                    {
                        int freeSize = slotItemPrev.getMaxStackSize() - slotItemPrev.stackSize;
                        if (freeSize >= putItem.stackSize)
                        {
                            slotItemPrev.stackSize += putItem.stackSize;
                        }
                        else
                        {
                            slotItemPrev.stackSize += freeSize;
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
                            int freeSize = slotItemPrev.getMaxStackSize() - slotItemPrev.stackSize;
                            if (freeSize >= putItem.stackSize)
                            {
                                slotItemPrev.stackSize += putItem.stackSize;
                                break;
                            }
                            else
                            {
                                slotItemPrev.stackSize += freeSize;
                                putItem.stackSize -= freeSize;
                                continue;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private Object tryFindingObject(String s)
    {
        Item item = GameData.getItemRegistry().getObject(s);
        if (item != null)
        {
            if (item instanceof ItemBlock)
            {
                return ((ItemBlock)item).block;
            }
            return item;
        }
        
        Block block = GameData.getBlockRegistry().getObject(s);
        if (block != Blocks.air)
        {
            return block;
        }
        return null;
    }

    private void addCommandBlock(World world, int x, int y, int z, String command, String sender, int rotate)
    {
        world.setBlockState(new BlockPos(x, y, z), Blocks.command_block.getDefaultState(), 2);
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
            /* regex pattern to find each coordinate triple with at least x and z relative (with tilde), save xz numbers and y in groups */
            final Pattern coordinates = Pattern.compile("~(-?\\d*) (~?-?\\d*) ~(-?\\d*)");
            final Matcher coordinateMatcher = coordinates.matcher(command);
            final StringBuffer stringBuffer = new StringBuffer();
            /* for each pattern match do */
            while (coordinateMatcher.find())
            {
                if (excessiveDebugging)
                {
                    debugPrinter.println("Found contained coordinate triple: "+coordinateMatcher.group());
                }
                if (rotate == RuinsMod.DIR_EAST)
                {
                    // z multiplied with -1 becomes x, x becomes z
                    coordinateMatcher.appendReplacement(stringBuffer, String.format("~%s %s ~%s",
                            (tryToInvert(coordinateMatcher.group(3))), coordinateMatcher.group(2), coordinateMatcher.group(1)));
                }
                else if (rotate == RuinsMod.DIR_WEST)
                {
                    // x multiplied with -1 becomes z, z becomes x
                    coordinateMatcher.appendReplacement(
                            stringBuffer,
                            String.format("~%s %s ~%s", coordinateMatcher.group(3), coordinateMatcher.group(2),
                                    tryToInvert(coordinateMatcher.group(1))));
                }
                else
                {
                    // case DIR_SOUTH, just swap x and z numbers
                    coordinateMatcher.appendReplacement(stringBuffer,
                            String.format("~%s %s ~%s", coordinateMatcher.group(3), coordinateMatcher.group(2), coordinateMatcher.group(1)));
                }
            }
            /* rebuild the pattern with the changes. we have the technology. we can make it better */
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
     * @param maybeNumber input string
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
            return new ItemStack(Items.bread);
        case 4:
        case 5:
            return new ItemStack(Items.wheat, random.nextInt(8) + 8);
        case 6:
            return new ItemStack(Items.iron_hoe);
        case 7:
            return new ItemStack(Items.iron_shovel);
        case 8:
        case 9:
            return new ItemStack(Items.string, random.nextInt(3) + 1);
        case 10:
        case 11:
        case 12:
            return new ItemStack(Items.wheat_seeds, random.nextInt(8) + 8);
        case 13:
        case 14:
        case 15:
            return new ItemStack(Items.bowl, random.nextInt(2) + 1);
        case 16:
            return new ItemStack(Items.bucket);
        case 17:
            return new ItemStack(Items.apple);
        case 18:
        case 19:
            return new ItemStack(Items.bone, random.nextInt(4) + 1);
        case 20:
        case 21:
            return new ItemStack(Items.egg, random.nextInt(2) + 1);
        case 22:
            return new ItemStack(Items.coal, random.nextInt(5) + 3);
        case 23:
            return new ItemStack(Items.iron_ingot, random.nextInt(5) + 3);
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
            return new ItemStack(Items.leather_boots);
        case 6:
        case 7:
            return new ItemStack(Items.leather_leggings);
        case 8:
        case 9:
            return new ItemStack(Items.flint_and_steel);
        case 10:
        case 11:
            return new ItemStack(Items.iron_axe);
        case 12:
            return new ItemStack(Items.iron_sword);
        case 13:
            return new ItemStack(Items.iron_pickaxe);
        case 14:
        case 15:
            return new ItemStack(Items.iron_helmet);
        case 16:
            return new ItemStack(Items.iron_chestplate);
        case 17:
        case 18:
            return new ItemStack(Items.book, random.nextInt(3) + 1);
        case 19:
            return new ItemStack(Items.compass);
        case 20:
            return new ItemStack(Items.clock);
        case 21:
            return new ItemStack(Items.redstone, random.nextInt(12) + 12);
        case 22:
            return new ItemStack(Items.golden_apple);
        case 23:
            return new ItemStack(Items.mushroom_stew, random.nextInt(2) + 1);
        case 24:
            return ChestGenHooks.getOneItem(ChestGenHooks.DUNGEON_CHEST, random);
        default:
            return new ItemStack(Items.diamond, random.nextInt(4));
        }
    }
    
    private int rotateMetadata(Block blockID, int metadata, int dir)
    {
        int result = rotateMetadata(blockID, metadata, dir, true);
        if (excessiveDebugging)
        {
            debugPrinter.println("Rotated blockID " + blockID + ", meta " + metadata + ", dir: " + dir + " result: " + result);
        }
        return result;
    }

    private int rotateMetadata(Block blockID, int metadata, int dir, boolean debugme)
    {
        // remember that, in this mod, NORTH is the default direction.
        // this method is unused if the direction is NORTH
        int tempdata = 0;
        
        if (blockID == Blocks.rail || blockID == Blocks.golden_rail || blockID == Blocks.detector_rail || blockID == Blocks.activator_rail)
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
        else if (blockID == Blocks.dark_oak_door || blockID == Blocks.iron_door)
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
                    return 0 + tempdata;
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
                    return 0 + tempdata;
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
                    return 0 + tempdata;
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
        else if (blockID == Blocks.torch || blockID == Blocks.stone_button || blockID == Blocks.wooden_button || blockID == Blocks.lever || blockID == Blocks.unlit_redstone_torch || blockID == Blocks.redstone_torch)
        {
            tempdata = 0;
            if (blockID == Blocks.lever || blockID == Blocks.stone_button || blockID == Blocks.wooden_button)
            {
                if (metadata - 8 > 0)
                {
                    tempdata += 8;
                    metadata -= 8;
                }
                // now see if it's a floor switch
                if (blockID == Blocks.lever && (metadata == 5 || metadata == 6))
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
        else if (blockID == Blocks.vine)
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
        else if (blockID == Blocks.pumpkin || blockID == Blocks.lit_pumpkin)
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
                    return 0 + tempdata;
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
                    return 0 + tempdata;
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
                    return 0 + tempdata;
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
        else if (blockID == Blocks.bed)
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
                    return 0 + tempdata;
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
                    return 0 + tempdata;
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
                    return 0 + tempdata;
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
        else if (blockID == Blocks.standing_sign)
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
         * Base NESW = 0 1 2 3 in 2 least significant bits
         * Additonal data unrelated to rotation in higher bits
         */
        else if (blockID == Blocks.unpowered_repeater || blockID == Blocks.unpowered_comparator || blockID == Blocks.powered_repeater || blockID == Blocks.powered_comparator)
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
                    return 0 | databits;
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
                    return 0 | databits;
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
                    return 0 | databits;
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
         * Least significant 2 bits cover rotation, rest is data
         * (connected to:) N E S W -> 1 2 0 3
         */
        if (blockID == Blocks.trapdoor)
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
                    return 0 | databits;
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
                    return 0 | databits;
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
                    return 0 | databits;
                }
            }
        }
        /*
         * Least significant 2 bits cover rotation, rest is data
         * (connected to:) N E S W -> 2 3 0 1
         */
        if (blockID == Blocks.tripwire_hook || blockID == Blocks.dark_oak_fence_gate || blockID == Blocks.end_portal_frame)
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
                    return 0 | databits;
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
                    return 0 | databits;
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
                    return 0 | databits;
                }
            }
        }
        /*
         * Least significant 2 bits cover rotation, rest is data
         * (connected to:) N E S W -> 0 1 2 3
         */
        if (blockID == Blocks.cocoa)
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
                    return 0 | databits;
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
                    return 0 | databits;
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
                    return 0 | databits;
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
        if (blockID == Blocks.anvil)
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
                return 0 | databits;
            }
            return metadata;
        }
        /*
         * 3 Orientations: UP/DOWN, N/S, E/W
         * various wood types: 0-3, 4-7, 8-11 and 'only bark' variants 12-15
         * can be simplified to 0 1 2 3
         */
        if (blockID == Blocks.log || blockID == Blocks.log2)
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