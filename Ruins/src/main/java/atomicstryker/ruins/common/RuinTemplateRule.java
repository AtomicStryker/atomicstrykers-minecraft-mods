package atomicstryker.ruins.common;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RuinTemplateRule {

    private final BlockState[] blockStates;
    private final CompoundNBT[] tileEntityData;
    private final double[] blockWeights;
    private final int[] blockBonemeals;
    private final RuinTemplate owner;
    private final boolean excessiveDebugging;
    private double blockWeightsTotal;

    private static final String PARAMETERS_TAG = "Ruins";
    private static final String NAME_TAG = "Name";
    private static final String NULL_BLOCK_NAME = "ruins:null";

    public RuinTemplateRule(RuinTemplate r, String rule, boolean debug) {
        owner = r;
        excessiveDebugging = debug;

        List<CompoundNBT> stateCompounds = RuleStringNbtHelper.splitRuleByBrackets(rule);
        if (stateCompounds == null || stateCompounds.isEmpty()) {
            RuinsMod.LOGGER.error("could not find any blockstates in rule {}", rule);
            blockStates = new BlockState[0];
            blockWeights = new double[0];
            blockBonemeals = new int[0];
            tileEntityData = new CompoundNBT[0];
            return;
        }
        int numblocks = stateCompounds.size();
        blockStates = new BlockState[numblocks];
        blockWeights = new double[numblocks];
        blockBonemeals = new int[numblocks];
        tileEntityData = new CompoundNBT[numblocks];
        blockWeightsTotal = 0;
        for (int i = 0; i < numblocks; i++) {
            // stateCompounds[i] = TAG_Compound
            CompoundNBT stateCompound = stateCompounds.get(i);

            // extract and strip Ruins-specific parameters
            double blockWeight = 1;
            int blockBonemeal = 0;
            CompoundNBT blockEntity = null;
            if (stateCompound.contains(PARAMETERS_TAG, 10)) {
                CompoundNBT parameters = stateCompound.getCompound(PARAMETERS_TAG);
                blockWeight = extractWeight(blockWeight, parameters);
                blockBonemeal = extractBonemeal(blockBonemeal, parameters);
                blockEntity = extractEntity(blockEntity, parameters);
                if (!parameters.isEmpty()) {
                    RuinsMod.LOGGER.warn("ignoring invalid Ruins parameters {} in rule {}", () -> parameters.keySet().toString(), () -> rule);
                }
                stateCompound.remove(PARAMETERS_TAG);
            }
            blockWeightsTotal += blockWeights[i] = blockWeight;
            blockBonemeals[i] = blockBonemeal;

            if (stateCompound.getString(NAME_TAG).equals(NULL_BLOCK_NAME)) {
                // pseudo-block "ruins:null" leaves existing block at this position intact
                blockStates[i] = null;
                tileEntityData[i] = null;

                if (excessiveDebugging) {
                    RuinsMod.LOGGER.info("rule alternative: {}, {}", i + 1, NULL_BLOCK_NAME);
                }
            } else {
                blockStates[i] = RuleStringNbtHelper.blockStateFromCompound(stateCompound);
                tileEntityData[i] = RuleStringNbtHelper.tileEntityNBTFromCompound(blockEntity, stateCompound);

                if (excessiveDebugging) {
                    RuinsMod.LOGGER.info("rule alternative: {}, {}", i + 1, blockStates[i].toString());
                }
            }
        }
    }

    RuinTemplateRule(RuinTemplate r, final String rule) {
        this(r, rule, false);
    }

    private static final String PARAMETER_WEIGHT_TAG = "weight";

    // get Ruins weight parameter (numeric, cast to double; must be non-negative)
    private static double extractWeight(double defaultValue, CompoundNBT parameters) {
        double weight = defaultValue;
        if (parameters.contains(PARAMETER_WEIGHT_TAG, 99)) {
            double value = parameters.getDouble(PARAMETER_WEIGHT_TAG);
            if (value >= 0) {
                weight = value;
                parameters.remove(PARAMETER_WEIGHT_TAG);
            }
        }
        return weight;
    }

    private static final String PARAMETER_BONEMEAL_TAG = "bonemeal";

    // get Ruins bonemeal parameter (int; must be non-negative)
    private static int extractBonemeal(int defaultValue, CompoundNBT parameters) {
        int bonemeal = defaultValue;
        if (parameters.contains(PARAMETER_BONEMEAL_TAG, 3)) {
            int value = parameters.getInt(PARAMETER_BONEMEAL_TAG);
            if (value >= 0) {
                bonemeal = value;
                parameters.remove(PARAMETER_BONEMEAL_TAG);
            }
        }
        return bonemeal;
    }

    private static final String PARAMETER_ENTITY_TAG = "entity";

    // get Ruins block_entity parameter (compound)
    private static CompoundNBT extractEntity(CompoundNBT defaultValue, CompoundNBT parameters) {
        CompoundNBT entity = defaultValue;
        if (parameters.contains(PARAMETER_ENTITY_TAG, 10)) {
            entity = parameters.getCompound(PARAMETER_ENTITY_TAG).copy();
            entity.remove("id");
            entity.remove("x");
            entity.remove("y");
            entity.remove("z");
            parameters.remove(PARAMETER_ENTITY_TAG);
        }
        return entity;
    }

    // get rotation (minecraft enum) corresponding to given direction (ruins int)
    private static Rotation getDirectionalRotation(int direction) {
        Rotation rotation = Rotation.NONE;
        switch (direction) {
            case RuinsMod.DIR_EAST:
                rotation = Rotation.CLOCKWISE_90;
                break;
            case RuinsMod.DIR_SOUTH:
                rotation = Rotation.CLOCKWISE_180;
                break;
            case RuinsMod.DIR_WEST:
                rotation = Rotation.COUNTERCLOCKWISE_90;
                break;
        }
        return rotation;
    }

    public void doBlock(World world, Random random, BlockPos pos, int rotate) {
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, pos, blocknum, rotate);
    }

    private void handleBlockSpawning(World world, Random random, BlockPos pos, int blocknum, int rotate) {
        BlockState blockState = blockStates[blocknum];
        if (blockState != null) {
            // use vanilla rotation - lets see how this goes
            BlockState rotatedState = blockState.rotate(world, pos, getDirectionalRotation(rotate));
            if (excessiveDebugging) {
                RuinsMod.LOGGER.info("About to place blockstate {} at pos {}", rotatedState.toString(), pos.toString());
            }
            realizeBlock(world, pos, rotatedState, tileEntityData[blocknum]);
            int bonemeal = blockBonemeals[blocknum];
            if (bonemeal > 0) {
                owner.markBlockForBonemeal(pos, bonemeal);
            }
        }
    }

    private int getBlockNum(Random random) {
        // random selection using weights assigned in config file
        int blockIndex = 0;
        for (double selector = random.nextDouble() * blockWeightsTotal; (selector -= blockWeights[blockIndex]) >= 0; ++blockIndex)
            ;
        return blockIndex;
    }

    // make specified block manifest in world, with given metadata and direction
    // returns associated tile entity, if there is one
    private void realizeBlock(World world, BlockPos position, BlockState blockState, CompoundNBT tileEntityData) {
        if (world != null && blockState != null) {

            // clobber existing tile entity block, if any
            TileEntity existing_entity = world.getTileEntity(position);
            if (existing_entity != null) {
                if (existing_entity instanceof IInventory) {
                    ((IInventory) existing_entity).clear();
                }
                world.setBlockState(position, Blocks.AIR.getDefaultState(), 4);
            }

            if (world.setBlockState(position, blockState, 2)) {
                TileEntity entity = world.getTileEntity(position);
                if (entity != null && tileEntityData != null) {
                    entity = TileEntity.func_235657_b_(blockState, entity.write(new CompoundNBT()).merge(tileEntityData));
                    world.setTileEntity(position, entity);

                    if (entity instanceof LockableLootTileEntity) {
                        CompoundNBT nbtTagCompound = entity.getTileData();
                        // unwrap forgedata if needed?
                        if (nbtTagCompound.contains("ForgeData")) {
                            nbtTagCompound = nbtTagCompound.getCompound("ForgeData");
                        }
                        if (nbtTagCompound.contains("LootTable")) {
                            String lootTable = nbtTagCompound.getString("LootTable");
                            long lootSeed = nbtTagCompound.getLong("LootTableSeed");

                            LockableLootTileEntity tileEntityLockableLoot = (LockableLootTileEntity) entity;
                            tileEntityLockableLoot.setLootTable(new ResourceLocation(lootTable), lootSeed);
                            tileEntityLockableLoot.fillWithLoot(null);
                        }
                    }
                }
            }
        }
    }
}
