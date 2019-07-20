package atomicstryker.battletowers.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AS_WorldGenTower
{

    public String failState;

    private static int candidates[][] = { { 4, -5 }, { 4, 0 }, { 4, 5, }, { 0, -5 }, { 0, 0 }, { 0, 5, }, { -4, -5 }, { -4, 0, }, { -4, 5 } };

    private static int candidatecount = candidates.length;
    private final static int maxHoleDepthInBase = 22;

    /**
     * @param world
     *            instance
     * @param random
     *            gen
     * @param ix
     *            coordinate
     * @param jy
     *            coordinate
     * @param kz
     *            coordinate
     * @return -1 when no tower should be able to spawn, else Towerchosen enum
     *         ordinal
     */
    public int getChosenTowerOrdinal(World world, Random random, int ix, int jy, int kz)
    {
        TowerTypes towerChosen;
        int countWater = 0;
        int countSand = 0;
        int countSnow = 0;
        int countFoliage = 0;
        int countElse = 0;

        for (int ccounter = 0; ccounter < candidatecount; ccounter++)
        {
            int pair[] = candidates[ccounter];
            int checkBlockY = getSurfaceBlockHeight(world, ix + pair[0], kz + pair[1]);

            Block ID = world.getBlockState(new BlockPos(ix + pair[0], checkBlockY, kz + pair[1])).getBlock();

            if (world.getBlockState(new BlockPos(ix + pair[0], checkBlockY + 1, kz + pair[1])).getBlock() == Blocks.SNOW || ID == Blocks.ICE)
            {
                countSnow++;
            }
            else if (ID == Blocks.SAND || ID == Blocks.SANDSTONE)
            {
                countSand++;
            }
            else if (ID == Blocks.WATER)
            {
                countWater++;
            }
            else if (ID == Blocks.LEAVES || ID == Blocks.WATERLILY || ID == Blocks.LOG || ID == Blocks.LOG2)
            {
                countFoliage++;
            }
            else
                countElse++;

            if (Math.abs(checkBlockY - jy) > maxHoleDepthInBase)
            {
                failState = "Uneven Surface, diff value: " + Math.abs(checkBlockY - jy);
                return -1;
            }

            for (int ycounter2 = 1; ycounter2 <= 3; ycounter2++)
            {
                ID = world.getBlockState(new BlockPos(ix + pair[0], (checkBlockY + ycounter2), kz + pair[1])).getBlock();
                if (isBannedBlockID(ID))
                {
                    failState = "Surface banned Block of ID: " + ID + " at height: " + ycounter2;
                    return -1;
                }
            }

            for (int ycounter = 1; ycounter <= 5; ycounter++)
            {
                ID = world.getBlockState(new BlockPos(ix + pair[0], checkBlockY - ycounter, kz + pair[1])).getBlock();

                if (ID == Blocks.AIR || isBannedBlockID(ID))
                {
                    failState = "Depth check - Banned Block or hole, Depth: " + ycounter + " ID: " + ID;
                    return -1;
                }
            }
        }

        // System.err.println("Snow: "+countSnow+" Sand: "+countSand+" Water:
        // "+countWater+" else: "+countElse);

        int[] nums = { countWater, countSnow, countSand, countFoliage, countElse };
        Arrays.sort(nums);
        int result = nums[nums.length - 1];

        // System.err.println("Picked max value of "+result);

        if (countSand == result)
        {
            towerChosen = TowerTypes.SandStone;
        }
        else if (countSnow == result)
        {
            towerChosen = TowerTypes.Ice;
        }
        else if (countWater == result)
        {
            towerChosen = TowerTypes.CobbleStoneMossy;
        }
        else if (countFoliage == result)
        {
            towerChosen = TowerTypes.CobbleStoneMossy;
        }
        else // standard is cobblestone, really rare should be nether
        {
            if (random.nextInt(10) == 0)
            {
                towerChosen = TowerTypes.Netherrack;
            }
            else
            {
                towerChosen = (random.nextInt(5) == 0) ? TowerTypes.SmoothStone : TowerTypes.CobbleStone;
            }
        }

        return towerChosen.ordinal();
    }

    @SuppressWarnings("deprecation") // is needed because getDefaultState on
                                     // stairs does not work
    public void generate(World world, Random random, int ix, int jy, int kz, int towerchoice, boolean underground)
    {
        TowerTypes towerChosen = TowerTypes.values()[towerchoice];

        Block towerWallBlockID = towerChosen.getWallBlockID();
        Block towerLightBlockID = towerChosen.getLightBlockID();
        Block towerFloorBlockID = towerChosen.getFloorBlockID();
        int towerFloorMeta = towerChosen.getFloorBlockMetaData();

        int startingHeight = underground ? Math.max(jy - 70, 15) : jy - 6;
        int maximumHeight = underground ? jy + 7 : 120;

        int floor = 1;
        boolean topFloor = false;
        int builderHeight = startingHeight;
        for (; builderHeight < maximumHeight; builderHeight += 7) // builderHeight
                                                                  // jumps
                                                                  // floors
        {
            if (builderHeight + 7 >= maximumHeight)
            {
                topFloor = true;
            }

            for (int floorIterator = 0; floorIterator < 7; floorIterator++) // build
                                                                            // each
                                                                            // floor
                                                                            // height
                                                                            // block
                                                                            // till
                                                                            // next
                                                                            // floor
            {
                if (floor == 1 && floorIterator < 4) // initial floor
                {
                    floorIterator = 4;
                }
                for (int xIterator = -7; xIterator < 7; xIterator++) // do each
                                                                     // X
                {
                    for (int zIterator = -7; zIterator < 7; zIterator++) // do
                                                                         // each
                                                                         // Z
                    {
                        int iCurrent = xIterator + ix;
                        int jCurrent = floorIterator + builderHeight;
                        int zCurrent = zIterator + kz;

                        if (zIterator == -7) // last row, 14
                        {
                            if (xIterator > -5 && xIterator < 4) // rear outer
                                                                 // wall
                            {
                                buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                            }
                            continue;
                        }
                        if (zIterator == -6 || zIterator == -5) // rows 12 and
                                                                // 13
                        {
                            if (xIterator == -5 || xIterator == 4) // outer wall
                                                                   // parts
                            {
                                buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                                continue;
                            }
                            if (zIterator == -6) // row 13 extra
                            {
                                if (xIterator == (floorIterator + 1) % 7 - 3) // stairwell!!
                                {
                                    if (!(underground && floor == 1))
                                    {
                                        world.setBlockState(new BlockPos(iCurrent, jCurrent, zCurrent), towerChosen.getStairBlockID().getStateFromMeta(0));
                                    }
                                    if (floorIterator == 5)
                                    {
                                        world.setBlockState(new BlockPos(iCurrent - 7, jCurrent, zCurrent), towerFloorBlockID.getDefaultState());
                                    }
                                    if (floorIterator == 6 && topFloor) // top
                                                                        // ledge
                                                                        // part
                                    {
                                        buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                                    }
                                    continue;
                                }
                                if (xIterator < 4 && xIterator > -5) // tower
                                                                     // insides
                                {
                                    world.setBlockState(new BlockPos(iCurrent, jCurrent, zCurrent), Blocks.AIR.getDefaultState());
                                }
                                continue;
                            }
                            if (zIterator != -5 || xIterator <= -5 || xIterator >= 5) // outside
                                                                                      // tower
                            {
                                continue;
                            }
                            if (floorIterator != 0 && floorIterator != 6 || xIterator != -4 && xIterator != 3)
                            {
                                if (floorIterator == 5 && (xIterator == 3 || xIterator == -4))
                                {
                                    buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                                }
                                else
                                {
                                    buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator); // under
                                                                                                                                 // stairwell
                                }
                            }
                            else
                            {
                                world.setBlockState(new BlockPos(iCurrent, jCurrent, zCurrent), Blocks.AIR.getDefaultState()); // stairwell
                                                                                                                               // space
                            }
                            continue;
                        }
                        if (zIterator == -4 || zIterator == -3 || zIterator == 2 || zIterator == 3) // rows
                                                                                                    // 11,
                                                                                                    // 10,
                                                                                                    // 5,
                                                                                                    // 4
                        {
                            if (xIterator == -6 || xIterator == 5) // outer wall
                                                                   // parts
                            {
                                buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                                continue;
                            }
                            if (xIterator <= -6 || xIterator >= 5) // outside
                                                                   // tower
                            {
                                continue;
                            }
                            if (floorIterator == 5)
                            {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                                continue;
                            }
                            if (world.getBlockState(new BlockPos(iCurrent, jCurrent, zCurrent)).getBlock() != Blocks.CHEST) // tower
                                                                                                                            // inside
                                                                                                                            // space
                            {
                                world.setBlockState(new BlockPos(iCurrent, jCurrent, zCurrent), Blocks.AIR.getDefaultState());
                            }
                            continue;
                        }
                        if (zIterator > -3 && zIterator < 2) // rows 10 to 5
                        {
                            if (xIterator == -7 || xIterator == 6)
                            {
                                if (floorIterator < 0 || floorIterator > 3 || ((xIterator != -7 && xIterator != 6) || underground) || zIterator != -1 && zIterator != 0) // wall,
                                                                                                                                                                         // short
                                                                                                                                                                         // of
                                                                                                                                                                         // window
                                {
                                    buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                                }
                                else
                                {
                                    world.setBlockState(new BlockPos(iCurrent, jCurrent, zCurrent), Blocks.AIR.getDefaultState());
                                }
                                continue;
                            }
                            if (xIterator <= -7 || xIterator >= 6)
                            {
                                continue;
                            }
                            if (floorIterator == 5)
                            {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                            }
                            else
                            {
                                world.setBlockState(new BlockPos(iCurrent, jCurrent, zCurrent), Blocks.AIR.getDefaultState());
                            }
                            continue;
                        }
                        if (zIterator == 4) // row 3
                        {
                            if (xIterator == -5 || xIterator == 4)
                            {
                                buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                                continue;
                            }
                            if (xIterator <= -5 || xIterator >= 4)
                            {
                                continue;
                            }
                            if (floorIterator == 5)
                            {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                            }
                            else
                            {
                                world.setBlockState(new BlockPos(iCurrent, jCurrent, zCurrent), Blocks.AIR.getDefaultState());
                            }
                            continue;
                        }
                        if (zIterator == 5) // row 2
                        {
                            if (xIterator == -4 || xIterator == -3 || xIterator == 2 || xIterator == 3)
                            {
                                buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                                continue;
                            }
                            if (xIterator <= -3 || xIterator >= 2)
                            {
                                continue;
                            }
                            if (floorIterator == 5)
                            {
                                buildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID, towerFloorMeta);
                            }
                            else
                            {
                                buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                            }
                            continue;
                        }
                        if (zIterator != 6 || xIterator <= -3 || xIterator >= 2)
                        {
                            continue;
                        }
                        if (floorIterator < 0 || floorIterator > 3 || xIterator != -1 && xIterator != 0)
                        {
                            buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                        }
                        else
                        {
                            buildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID, floor, floorIterator);
                        }
                    }

                }
            }

            if (floor == 2)
            {
                world.setBlockState(new BlockPos(ix + 3, builderHeight, kz - 5), towerWallBlockID.getDefaultState());
                world.setBlockState(new BlockPos(ix + 3, builderHeight - 1, kz - 5), towerWallBlockID.getDefaultState());
            }
            if ((!underground && topFloor) || (underground && floor == 1))
            {
                if (towerChosen != TowerTypes.Null)
                {
                    AS_EntityGolem entitygolem = new AS_EntityGolem(world, towerChosen.ordinal());
                    entitygolem.setLocationAndAngles(ix + 0.5D, builderHeight + 6, kz + 0.5D, world.rand.nextFloat() * 360F, 0.0F);
                    world.spawnEntity(entitygolem);
                }
            }
            else
            {
                if (towerChosen != TowerTypes.Null)
                {
                    world.setBlockState(new BlockPos(ix + 2, builderHeight + 6, kz + 2), Blocks.MOB_SPAWNER.getDefaultState());
                    TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) world.getTileEntity(new BlockPos(ix + 2, builderHeight + 6, kz + 2));
                    if (tileentitymobspawner != null)
                    {
                        if (!DungeonTweaksCompat.isLoaded)
                        {
                            tileentitymobspawner.getSpawnerBaseLogic().setEntityId(getMobType(world.rand));
                        }
                        else
                        {
                            DungeonTweaksCompat.fireDungeonSpawn(tileentitymobspawner, world, random, towerChosen);
                        }
                    }

                    world.setBlockState(new BlockPos(ix - 3, builderHeight + 6, kz + 2), Blocks.MOB_SPAWNER.getDefaultState());
                    tileentitymobspawner = (TileEntityMobSpawner) world.getTileEntity(new BlockPos(ix - 3, builderHeight + 6, kz + 2));
                    if (tileentitymobspawner != null)
                    {
                        if (!DungeonTweaksCompat.isLoaded)
                        {
                            tileentitymobspawner.getSpawnerBaseLogic().setEntityId(getMobType(world.rand));
                        }
                        else
                        {
                            DungeonTweaksCompat.fireDungeonSpawn(tileentitymobspawner, world, random, towerChosen);
                        }
                    }
                }
                else
                {
                    world.setBlockState(new BlockPos(ix + 2, builderHeight + 6, kz + 2), Blocks.AIR.getDefaultState());
                    world.setBlockState(new BlockPos(ix - 3, builderHeight + 6, kz + 2), Blocks.AIR.getDefaultState());
                }
            }
            // chest pedestal
            world.setBlockState(new BlockPos(ix, builderHeight + 6, kz + 3), towerFloorBlockID.getDefaultState());
            world.setBlockState(new BlockPos(ix - 1, builderHeight + 6, kz + 3), towerFloorBlockID.getDefaultState());

            if (builderHeight + 56 >= 120 && floor == 1)
            {
                floor = 2;
            }

            if (towerChosen != TowerTypes.Null)
            {
                // chest
                TowerStageItemManager floorChestManager;
                if (!underground)
                {
                    floorChestManager = topFloor ? WorldGenHandler.getTowerStageManagerForFloor(10) : WorldGenHandler.getTowerStageManagerForFloor(floor);
                }
                else
                {
                    floorChestManager = floor == 1 ? WorldGenHandler.getTowerStageManagerForFloor(10) : WorldGenHandler.getTowerStageManagerForFloor(Math.abs(11 - floor));
                }

                for (int chestlength = 0; chestlength < 2; chestlength++)
                {
                    world.setBlockState(new BlockPos(ix - chestlength, builderHeight + 7, kz + 3), Blocks.CHEST.getStateFromMeta(2));
                    TileEntityChest tileentitychest = (TileEntityChest) world.getTileEntity(new BlockPos(ix - chestlength, builderHeight + 7, kz + 3));
                    if (tileentitychest != null)
                    {
                        int count = underground ? AS_BattleTowersCore.instance.itemGenerateAttemptsPerFloor * 2 : AS_BattleTowersCore.instance.itemGenerateAttemptsPerFloor;
                        List<ItemStack> generatedStacks = floorChestManager.getStageItemStacks(world, world.rand, tileentitychest, count);
                        List<Integer> freeSlots = new ArrayList<>(tileentitychest.getSizeInventory());
                        for (int i = 0; i < tileentitychest.getSizeInventory(); i++)
                        {
                            freeSlots.add(i);
                        }
                        Iterator<ItemStack> iterator = generatedStacks.iterator();
                        while (iterator.hasNext() && !freeSlots.isEmpty())
                        {
                            Integer slot = freeSlots.get(world.rand.nextInt(freeSlots.size()));
                            freeSlots.remove(slot);
                            tileentitychest.setInventorySlotContents(slot, iterator.next());
                        }
                    }
                }
            }
            else
            {
                for (int chestlength = 0; chestlength < 2; chestlength++)
                {
                    world.setBlockState(new BlockPos(ix - chestlength, builderHeight + 7, kz + 3), Blocks.CHEST.getStateFromMeta(2));
                }
            }

            // move lights builder a bit higher, to support non-opaque lights
            // such as lamps
            if (towerLightBlockID == Blocks.TORCH)
            {
                world.setBlockState(new BlockPos(ix + 3, builderHeight + 2, kz - 6), towerLightBlockID.getStateFromMeta(0).withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 2);
                world.setBlockState(new BlockPos(ix - 4, builderHeight + 2, kz - 6), towerLightBlockID.getStateFromMeta(0).withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 2);
                world.setBlockState(new BlockPos(ix + 1, builderHeight + 2, kz - 4), towerLightBlockID.getStateFromMeta(0).withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 2);
                world.setBlockState(new BlockPos(ix - 2, builderHeight + 2, kz - 4), towerLightBlockID.getStateFromMeta(0).withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 2);
            }
            else
            {
                world.setBlockState(new BlockPos(ix + 3, builderHeight + 2, kz - 6), towerLightBlockID.getStateFromMeta(0));
                world.setBlockState(new BlockPos(ix - 4, builderHeight + 2, kz - 6), towerLightBlockID.getStateFromMeta(0));
                world.setBlockState(new BlockPos(ix + 1, builderHeight + 2, kz - 4), towerLightBlockID.getStateFromMeta(0));
                world.setBlockState(new BlockPos(ix - 2, builderHeight + 2, kz - 4), towerLightBlockID.getStateFromMeta(0));
            }

            if (towerChosen != TowerTypes.Null)
            {
                for (int l3 = 0; l3 < (floor * 4 + towerChosen.ordinal()) - 8 && !topFloor; l3++) // random
                                                                                                  // hole
                                                                                                  // poker
                {
                    int k4 = 5 - world.rand.nextInt(12);
                    int k5 = builderHeight + 5;
                    int j6 = 5 - world.rand.nextInt(10);
                    if (j6 < -2 && k4 < 4 && k4 > -5 && k4 != 1 && k4 != -2)
                    {
                        continue;
                    }
                    k4 += ix;
                    j6 += kz;
                    if (world.getBlockState(new BlockPos(k4, k5, j6)).getBlock() == towerFloorBlockID && world.getBlockState(new BlockPos(k4, k5 + 1, j6)).getBlock() != Blocks.MOB_SPAWNER)
                    {
                        world.setBlockState(new BlockPos(k4, k5, j6), Blocks.AIR.getDefaultState());
                    }
                }
            }

            floor++;
        }

        System.out.println("Dimension " + world.provider.getDimension() + " Battle Tower type " + towerChosen + " spawned at [ " + ix + " | " + kz + " ], underground: " + underground);
    }

    @SuppressWarnings("deprecation")
    private void buildFloorPiece(World world, int i, int j, int k, Block towerFloorBlockID, int towerFloorMeta)
    {
        world.setBlockState(new BlockPos(i, j, k), towerFloorBlockID.getStateFromMeta(towerFloorMeta));
    }

    private void buildWallPiece(World world, int i, int j, int k, Block towerWallBlockID, int floor, int floorIterator)
    {
        world.setBlockState(new BlockPos(i, j, k), towerWallBlockID.getDefaultState());
        if (floor == 1 && floorIterator == 4)
        {
            fillTowerBaseToGround(world, i, j, k, towerWallBlockID);
        }
    }

    private void fillTowerBaseToGround(World world, int i, int j, int k, Block blocktype)
    {
        int y = j - 1;
        while (y > 0 && !isBuildableBlockID(world.getBlockState(new BlockPos(i, y, k)).getBlock()))
        {
            world.setBlockState(new BlockPos(i, y, k), blocktype.getDefaultState());
            y--;
        }
    }

    private int getSurfaceBlockHeight(World world, int x, int z)
    {
        int h = 50;

        do
        {
            h++;
        }
        while (world.getBlockState(new BlockPos(x, h, z)).getBlock() != Blocks.AIR && !isFoliageBlockID(world.getBlockState(new BlockPos(x, h, z)).getBlock()));

        return h - 1;
    }

    private boolean isFoliageBlockID(Block ID)
    {
        return (ID == Blocks.SNOW || ID == Blocks.TALLGRASS || ID == Blocks.DEADBUSH || ID == Blocks.LOG || ID == Blocks.LOG2 || ID == Blocks.LEAVES);
    }

    private boolean isBuildableBlockID(Block ID)
    {
        return (ID == Blocks.STONE || ID == Blocks.GRASS || ID == Blocks.SAND || ID == Blocks.SANDSTONE || ID == Blocks.GRAVEL || ID == Blocks.DIRT);
    }

    private boolean isBannedBlockID(Block iD)
    {
        return (iD == Blocks.YELLOW_FLOWER || iD == Blocks.RED_FLOWER || iD == Blocks.BROWN_MUSHROOM_BLOCK || iD == Blocks.RED_MUSHROOM_BLOCK || iD == Blocks.CACTUS || iD == Blocks.PUMPKIN
                || iD == Blocks.LAVA);
    }

    private ResourceLocation getMobType(Random random)
    {
        switch (random.nextInt(10))
        {
        case 0:
        case 1:
        case 2:
        {
            return EntityList.getKey(EntitySkeleton.class);
        }
        case 3:
        case 4:
        case 5:
        case 6:
        {
            return EntityList.getKey(EntityZombie.class);
        }
        case 7:
        case 8:
        {
            EntityList.getKey(EntitySpider.class);
        }
        case 9:
        {
            return EntityList.getKey(EntityCaveSpider.class);
        }
        default:
            return EntityList.getKey(EntityZombie.class);
        }
    }

    public enum TowerTypes
    {
        Null("null", Blocks.AIR, Blocks.AIR, Blocks.AIR, 0, Blocks.AIR),
        CobbleStone("cobblestone", Blocks.COBBLESTONE, Blocks.TORCH, Blocks.DOUBLE_STONE_SLAB, 0, Blocks.STONE_STAIRS),
        CobbleStoneMossy("cobblestonemossy", Blocks.MOSSY_COBBLESTONE, Blocks.TORCH, Blocks.DOUBLE_STONE_SLAB, 0, Blocks.STONE_STAIRS),
        SandStone("sandstone", Blocks.SANDSTONE, Blocks.TORCH, Blocks.DOUBLE_STONE_SLAB, 1, Blocks.SANDSTONE_STAIRS),
        Ice("ice", Blocks.ICE, Blocks.AIR /* Blocks.GLOWSTONE */, Blocks.CLAY, 2, Blocks.OAK_STAIRS), // since
                                                                                                      // when
                                                                                                      // does
                                                                                                      // glowstone
                                                                                                      // melt
                                                                                                      // ice
        SmoothStone("smoothstone", Blocks.STONE, Blocks.TORCH, Blocks.DOUBLE_STONE_SLAB, 3, Blocks.STONE_STAIRS),
        Netherrack("netherrack", Blocks.NETHERRACK, Blocks.GLOWSTONE, Blocks.SOUL_SAND, 0, Blocks.NETHER_BRICK_STAIRS),
        Jungle("jungle", Blocks.MOSSY_COBBLESTONE, Blocks.WEB, Blocks.DIRT, 0, Blocks.JUNGLE_STAIRS);

        private Block wallBlockID;
        private Block lightBlockID;
        private Block floorBlockID;
        private int floorBlockMetaData;
        private Block stairBlockID;
        private String typeName;

        TowerTypes(String t, Block a, Block b, Block c, int d, Block e)
        {
            this.wallBlockID = a;
            this.lightBlockID = b;
            this.floorBlockID = c;
            this.floorBlockMetaData = d;
            this.stairBlockID = e;
            this.typeName = t;
        }

        Block getWallBlockID()
        {
            return wallBlockID;
        }

        Block getLightBlockID()
        {
            return lightBlockID;
        }

        Block getFloorBlockID()
        {
            return floorBlockID;
        }

        int getFloorBlockMetaData()
        {
            return floorBlockMetaData;
        }

        Block getStairBlockID()
        {
            return stairBlockID;
        }

        public String getName()
        {
            return this.typeName;
        }

        public ResourceLocation getId()
        {
            return new ResourceLocation("battletowers:" + this.typeName);
        }
    }

}
