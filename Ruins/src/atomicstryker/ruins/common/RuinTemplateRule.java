package atomicstryker.ruins.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import cpw.mods.fml.common.registry.GameData;

public class RuinTemplateRule
{
    private final Block[] blockIDs;
    private final int[] blockMDs;
    private final String[] blockStrings;
    private int chance = 100;
    private int condition = 0;
    private final RuinIBuildable owner;

    public RuinTemplateRule(RuinIBuildable r, String rule) throws Exception
    {
        owner = r;
        String[] items = rule.split(",");
        int numblocks = items.length - 2;
        if (numblocks < 1)
        {
            throw new Exception("No blockIDs specified for rule [" + rule + "] in template " + owner.getName());
        }
        condition = Integer.parseInt(items[0]);
        chance = Integer.parseInt(items[1]);
        blockIDs = new Block[numblocks];
        blockMDs = new int[numblocks];
        blockStrings = new String[numblocks];
        String[] data;
        for (int i = 0; i < numblocks; i++)
        {
            data = items[i + 2].split("-");
            if (data.length > 1) // has '-' in it, like "50-5" or "planks-3"
            {
                if (isNumber(data[0])) // 50-5
                {
                    System.err.println("Rule [" + rule + "] in template " + owner.getName()+" still uses numeric blockIDs! ERROR!");
                    blockIDs[i] = Blocks.air;
                    blockMDs[i] = Integer.parseInt(data[1]);
                    blockStrings[i] = "";
                }
                else
                // planks-3
                {
                    blockIDs[i] = tryFindingBlockOfName(data[0]);
                    blockMDs[i] = Integer.parseInt(data[1]);
                    blockStrings[i] = items[i + 2];
                }
            }
            else if (!isNumber(data[0])) // MobSpawners and the like
            {
                blockIDs[i] = null;
                blockMDs[i] = 0;
                blockStrings[i] = items[i + 2];
            }
            else
            // does not have metadata specified, aka "50"
            {
                if (isNumber(items[i + 2]))
                {
                    System.err.println("Rule [" + rule + "] in template " + owner.getName()+" still uses numeric blockIDs! ERROR!");
                    blockIDs[i] = Blocks.air;
                }
                else
                {
                    tryFindingBlockOfName(items[i + 2]);
                }
                blockMDs[i] = 0;
                blockStrings[i] = "";
            }
        }
    }

    private Block tryFindingBlockOfName(String blockName)
    {
        return GameData.blockRegistry.getObject(blockName);
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

    public boolean canReplace(Block blockID, Block targetBlock)
    {
        if (owner.preserveBlock(targetBlock) && blockID == Blocks.air)
        {
            return false;
        }
        return true;
    }

    private void doNormalBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void doAboveBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        if ((condition <= 0 ? true : false) ^ owner.isAir(world.func_147439_a(x, y - 1, z)))
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
                (owner.isAir(world.func_147439_a(x + 1, y, z))) && (owner.isAir(world.func_147439_a(x, y, z + 1)))
                        && (owner.isAir(world.func_147439_a(x, y, z - 1))) && (owner.isAir(world.func_147439_a(x - 1, y, z)))))
        {
            return;
        }
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void doUnderBlock(World world, Random random, int x, int y, int z, int rotate)
    {
        if ((condition <= 0 ? true : false) ^ owner.isAir(world.func_147439_a(x, y + 1, z)))
        {
            return;
        }

        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum, rotate, blockStrings[blocknum]);
    }

    private void handleBlockSpawning(World world, Random random, int x, int y, int z, int blocknum, int rotate, String blockString)
    {
        Block blockID = blockIDs[blocknum];
        if (blockID == null)
        {
            doSpecialBlock(world, random, x, y, z, blockString);
        }
        else
        {
            placeBlock(world, blocknum, x, y, z, rotate);
        }
    }

    private void placeBlock(World world, int blocknum, int x, int y, int z, int rotate)
    {
        if (canReplace(blockIDs[blocknum], world.func_147439_a(x, y, z)))
        {
            if (rotate != RuinsMod.DIR_NORTH)
            {
                int metadata = rotateMetadata(blockIDs[blocknum], blockMDs[blocknum], rotate);
                world.func_147465_d(x, y, z, blockIDs[blocknum], metadata, 2);
            }
            else
            {
                world.func_147465_d(x, y, z, blockIDs[blocknum], blockMDs[blocknum], 2);
            }
        }
    }

    public void doSpecialBlock(World world, Random random, int x, int y, int z, String dataString)
    {
        if (dataString.equals("preserveBlock"))
        {
            // NOOP
        }
        else if (dataString.startsWith("MobSpawner:"))
        {
            addCustomSpawner(world, x, y, z, dataString.split(":")[1]);
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
            int meta = 0;
            if (dataString.contains("-"))
            {
                meta = Integer.valueOf(dataString.substring(dataString.indexOf("-") + 1));
            }
            addEasyChest(world, random, x, y, z, meta, random.nextInt(3) + 3);
        }
        else if (dataString.startsWith("MediumChest"))
        {
            int meta = 0;
            if (dataString.contains("-"))
            {
                meta = Integer.valueOf(dataString.substring(dataString.indexOf("-") + 1));
            }
            addMediumChest(world, random, x, y, z, meta, random.nextInt(4) + 3);
        }
        else if (dataString.startsWith("HardChest"))
        {
            int meta = 0;
            if (dataString.contains("-"))
            {
                meta = Integer.valueOf(dataString.substring(dataString.indexOf("-") + 1));
            }
            addHardChest(world, random, x, y, z, meta, random.nextInt(5) + 3);
        }
        else if (dataString.startsWith("ChestGenHook:"))
        {
            int meta = 0;
            if (dataString.contains("-"))
            {
                int index = dataString.indexOf("-");
                meta = Integer.valueOf(dataString.substring(index + 1));
                dataString = dataString.substring(0, index);
            }
            String[] s = dataString.split(":");
            addChestGenChest(world, random, x, y, z, s[1], Integer.valueOf(s[2]), meta);
        }
        else if (dataString.equals("EnderCrystal"))
        {
            spawnEnderCrystal(world, x, y, z);
        }
        else if (dataString.startsWith("CommandBlock:"))
        {
            String[] s = dataString.split(":");
            addCommandBlock(world, x, y, z, s[1], s[2]);
        }
        else
        {
            System.err.println("Ruins Mod could not determine what to spawn for [" + dataString + "] in Ruin template: " + owner.getName());
        }
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
        world.func_147465_d(x, y, z, Blocks.bedrock, 0, 2);
    }

    private void addCustomSpawner(World world, int x, int y, int z, String id)
    {
        world.func_147465_d(x, y, z, Blocks.mob_spawner, 0, 2);
        TileEntityMobSpawner mobspawner = (TileEntityMobSpawner) world.func_147438_o(x, y, z);
        if (mobspawner != null)
        {
            mobspawner.func_145881_a().setMobID(id);
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
        world.func_147465_d(x, y, z, Blocks.chest, meta, 2);
        TileEntityChest chest = (TileEntityChest) world.func_147438_o(x, y, z);
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
        world.func_147465_d(x, y, z, Blocks.chest, meta, 2);
        TileEntityChest chest = (TileEntityChest) world.func_147438_o(x, y, z);
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
        world.func_147465_d(x, y, z, Blocks.chest, meta, 2);
        TileEntityChest chest = (TileEntityChest) world.func_147438_o(x, y, z);
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
        world.func_147465_d(x, y, z, Blocks.chest, meta, 2);
        TileEntityChest chest = (TileEntityChest) world.func_147438_o(x, y, z);
        if (chest != null)
        {
            ChestGenHooks info = ChestGenHooks.getInfo(gen);
            WeightedRandomChestContent.generateChestContents(random, info.getItems(random), chest, items);
        }
    }

    private void addCommandBlock(World world, int x, int y, int z, String command, String sender)
    {
        world.func_147465_d(x, y, z, Blocks.command_block, 0, 2);
        TileEntityCommandBlock tecb = (TileEntityCommandBlock) world.func_147438_o(x, y, z);
        if (tecb != null)
        {
            tecb.func_145993_a().func_145752_a(command);
            tecb.func_145993_a().func_145754_b(sender);
        }
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
        // remember that, in this mod, NORTH is the default direction.
        // this method is unused if the direction is NORTH
        int tempdata = 0;
        
        if (blockID == Blocks.rail || blockID == Blocks.golden_rail || blockID == Blocks.detector_rail)
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
        else if (blockID == Blocks.wooden_door || blockID == Blocks.iron_door || blockID == Blocks.trapdoor)
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
        else if (blockID == Blocks.torch || blockID == Blocks.stone_button || blockID == Blocks.lever || blockID == Blocks.unlit_redstone_torch || blockID == Blocks.redstone_torch)
        {
            tempdata = 0;
            if (blockID == Blocks.lever || blockID == Blocks.stone_button)
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
        else if (blockID == Blocks.ladder || blockID == Blocks.dispenser || blockID == Blocks.furnace || blockID == Blocks.lit_furnace || blockID == Blocks.wall_sign)
        {
            switch (dir)
            {
            case RuinsMod.DIR_EAST:
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
            case RuinsMod.DIR_SOUTH:
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
            case RuinsMod.DIR_WEST:
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
        else if (blockID == Blocks.pumpkin || blockID == Blocks.lit_pumpkin || blockID == Blocks.unpowered_repeater || blockID == Blocks.powered_repeater)
        {
            if (blockID == Blocks.unpowered_repeater || blockID == Blocks.powered_repeater)
            {
                // check for the delay tick for repeaters
                if (metadata - 4 >= 0)
                {
                    if (metadata - 8 >= 0)
                    {
                        if (metadata - 12 >= 0)
                        {
                            // four tick delay
                            tempdata += 12;
                            metadata -= 12;
                        }
                        else
                        {
                            // three tick delay
                            tempdata += 8;
                            metadata -= 8;
                        }
                    }
                    else
                    {
                        // two tick delay
                        tempdata += 4;
                        metadata -= 4;
                    }
                }
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
        return CustomRotationMapping.getMapping(blockID, metadata, dir);
    }

}