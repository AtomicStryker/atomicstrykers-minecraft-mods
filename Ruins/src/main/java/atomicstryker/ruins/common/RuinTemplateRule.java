package atomicstryker.ruins.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class RuinTemplateRule {

    private final BlockState[] blockStates;
    private final CompoundTag[] tileEntityData;
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

        List<CompoundTag> stateCompounds = RuleStringNbtHelper.splitRuleByBrackets(rule);
        if (stateCompounds == null || stateCompounds.isEmpty()) {
            RuinsMod.LOGGER.error("could not find any blockstates in rule {}", rule);
            blockStates = new BlockState[0];
            blockWeights = new double[0];
            blockBonemeals = new int[0];
            tileEntityData = new CompoundTag[0];
            return;
        }
        int numblocks = stateCompounds.size();
        blockStates = new BlockState[numblocks];
        blockWeights = new double[numblocks];
        blockBonemeals = new int[numblocks];
        tileEntityData = new CompoundTag[numblocks];
        blockWeightsTotal = 0;
        for (int i = 0; i < numblocks; i++) {
            // stateCompounds[i] = TAG_Compound
            CompoundTag stateCompound = stateCompounds.get(i);

            // extract and strip Ruins-specific parameters
            double blockWeight = 1;
            int blockBonemeal = 0;
            CompoundTag blockEntity = null;
            if (stateCompound.contains(PARAMETERS_TAG)) {
                CompoundTag parameters = stateCompound.getCompoundOrEmpty(PARAMETERS_TAG);
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
    private static double extractWeight(double defaultValue, CompoundTag parameters) {
        double weight = defaultValue;
        if (parameters.contains(PARAMETER_WEIGHT_TAG)) {
            double value = parameters.getDouble(PARAMETER_WEIGHT_TAG).orElse(0D);
            if (value >= 0) {
                weight = value;
                parameters.remove(PARAMETER_WEIGHT_TAG);
            }
        }
        return weight;
    }

    private static final String PARAMETER_BONEMEAL_TAG = "bonemeal";

    // get Ruins bonemeal parameter (int; must be non-negative)
    private static int extractBonemeal(int defaultValue, CompoundTag parameters) {
        int bonemeal = defaultValue;
        if (parameters.contains(PARAMETER_BONEMEAL_TAG)) {
            int value = parameters.getInt(PARAMETER_BONEMEAL_TAG).orElse(0);
            if (value >= 0) {
                bonemeal = value;
                parameters.remove(PARAMETER_BONEMEAL_TAG);
            }
        }
        return bonemeal;
    }

    private static final String PARAMETER_ENTITY_TAG = "entity";

    // get Ruins block_entity parameter (compound)
    private static CompoundTag extractEntity(CompoundTag defaultValue, CompoundTag parameters) {
        CompoundTag entity = defaultValue;
        if (parameters.contains(PARAMETER_ENTITY_TAG)) {
            entity = parameters.getCompound(PARAMETER_ENTITY_TAG).get();
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
        return switch (direction) {
            case RuinsMod.DIR_EAST -> Rotation.CLOCKWISE_90;
            case RuinsMod.DIR_SOUTH -> Rotation.CLOCKWISE_180;
            case RuinsMod.DIR_WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public void doBlock(Level world, RandomSource random, BlockPos pos, int rotate) {
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, pos, blocknum, rotate);
    }

    private void handleBlockSpawning(Level world, RandomSource random, BlockPos pos, int blocknum, int rotate) {
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

    private int getBlockNum(RandomSource random) {
        // random selection using weights assigned in config file
        int blockIndex = 0;
        for (double selector = random.nextDouble() * blockWeightsTotal; (selector -= blockWeights[blockIndex]) >= 0; ++blockIndex)
            ;
        return blockIndex;
    }

    // make specified block manifest in world, with given metadata and direction
    // returns associated tile entity, if there is one
    private void realizeBlock(Level world, BlockPos position, BlockState blockState, CompoundTag nbtTagCompound) {
        if (world != null && blockState != null) {

            // clobber existing tile entity block, if any
            BlockEntity existing_entity = world.getBlockEntity(position);
            if (existing_entity != null) {
                world.setBlock(position, Blocks.AIR.defaultBlockState(), 3);
            }
            boolean success = world.setBlock(position, blockState, 3);
            if (!success) {
                // there is a notable edge case where setting a block fails if the block is already equal, catch that
                if (!world.getBlockState(position).is(blockState.getBlock())) {
                    RuinsMod.LOGGER.error("world setBlock({}, {}) returned false, current blockstate: {}", position, blockState, world.getBlockState(position));
                    return;
                }
                // if there was equality, just continue
            }
            BlockEntity entity = world.getBlockEntity(position);
            if (nbtTagCompound != null) {
                if (entity == null) {
                    RuinsMod.LOGGER.error("no BlockEntity created from {}", blockState);
                    return;
                }
                // load Ruins stored NBT data into the entity
                entity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), nbtTagCompound));
                if (entity instanceof RandomizableContainerBlockEntity) {
                    // unwrap forgedata if needed?
                    if (nbtTagCompound.contains("ForgeData")) {
                        nbtTagCompound = nbtTagCompound.getCompound("ForgeData").get();
                    }
                    if (nbtTagCompound.contains("LootTable")) {
                        String lootTable = nbtTagCompound.getString("LootTable").get();
                        long lootSeed = nbtTagCompound.getLong("LootTableSeed").get();

                        Identifier lootIdentifier = Identifier.parse(lootTable);
                        RandomizableContainerBlockEntity tileEntityLockableLoot = (RandomizableContainerBlockEntity) entity;
                        for (ResourceKey<LootTable> lootTableResourceKey : BuiltInLootTables.all()) {
                            if (lootTableResourceKey.identifier().equals(lootIdentifier)) {
                                tileEntityLockableLoot.setLootTable(lootTableResourceKey, lootSeed);
                                tileEntityLockableLoot.unpackLootTable(null);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
