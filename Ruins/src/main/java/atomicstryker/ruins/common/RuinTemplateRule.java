package atomicstryker.ruins.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

public class RuinTemplateRule {

    private final BlockState[] blockStates;
    private final CompoundTag[] tileEntityData;
    private final double[] blockWeights;
    private final int[] blockBonemeals;
    // where each block may go: PLACE_ANYWHERE, PLACE_BACKGROUND or PLACE_FOREGROUND (Ruins parameter "place")
    private final byte[] placePolicies;
    // true where the block is the pseudo-block "ruins:end_crystal"
    private final boolean[] endCrystals;
    private final RuinTemplate owner;
    private final boolean excessiveDebugging;
    private double blockWeightsTotal;
    // the "condition,chance," in front of a rule (e.g. "rule3=1,50,{...}")
    private int condition = 0;
    private double chance = 1;

    private static final Pattern PREFIX = Pattern.compile("^\\s*(-?\\d+)\\s*,\\s*(\\d+(?:\\.\\d+)?)\\s*,");

    private static final String PARAMETERS_TAG = "Ruins";
    // Place blocks exactly as the template stores them: send them to clients, but don't notify or reshape the
    // neighbours. Ruins are built block by block, so with neighbour/shape updates a torch, ladder, vine, button or crop
    // placed before its support would break, water and lava would start to flow and sand would fall. Templates store
    // full block states, connections included, so nothing needs to be recomputed.
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final String NAME_TAG = "Name";
    private static final String NULL_BLOCK_NAME = "ruins:null";
    private static final String END_CRYSTAL_BLOCK_NAME = "ruins:end_crystal";

    private static final byte PLACE_ANYWHERE = 0;
    private static final byte PLACE_BACKGROUND = 1;
    private static final byte PLACE_FOREGROUND = 2;
    private static final String PARAMETER_PLACE_TAG = "place";

    public RuinTemplateRule(RuinTemplate r, String rule, boolean debug) {
        owner = r;
        excessiveDebugging = debug;

        Matcher prefix = PREFIX.matcher(rule);
        if (prefix.find()) {
            condition = Integer.parseInt(prefix.group(1));
            chance = Math.min(Math.max(Double.parseDouble(prefix.group(2)) / 100, 0), 1);
        }

        List<CompoundTag> stateCompounds = RuleStringNbtHelper.splitRuleByBrackets(rule);
        if (stateCompounds == null || stateCompounds.isEmpty()) {
            RuinsMod.LOGGER.error("could not find any blockstates in rule {}", rule);
            blockStates = new BlockState[0];
            blockWeights = new double[0];
            blockBonemeals = new int[0];
            placePolicies = new byte[0];
            endCrystals = new boolean[0];
            tileEntityData = new CompoundTag[0];
            return;
        }
        int numblocks = stateCompounds.size();
        blockStates = new BlockState[numblocks];
        blockWeights = new double[numblocks];
        blockBonemeals = new int[numblocks];
        placePolicies = new byte[numblocks];
        endCrystals = new boolean[numblocks];
        tileEntityData = new CompoundTag[numblocks];
        blockWeightsTotal = 0;
        for (int i = 0; i < numblocks; i++) {
            // stateCompounds[i] = TAG_Compound
            CompoundTag stateCompound = stateCompounds.get(i);

            // extract and strip Ruins-specific parameters
            double blockWeight = 1;
            int blockBonemeal = 0;
            byte placePolicy = PLACE_ANYWHERE;
            CompoundTag blockEntity = null;
            if (stateCompound.contains(PARAMETERS_TAG, 10)) {
                CompoundTag parameters = stateCompound.getCompound(PARAMETERS_TAG);
                blockWeight = extractWeight(blockWeight, parameters);
                blockBonemeal = extractBonemeal(blockBonemeal, parameters);
                placePolicy = extractPlace(placePolicy, parameters);
                blockEntity = extractEntity(blockEntity, parameters);
                if (!parameters.isEmpty()) {
                    RuinsMod.LOGGER.warn("ignoring invalid Ruins parameters {} in rule {}", () -> parameters.getAllKeys().toString(), () -> rule);
                }
                stateCompound.remove(PARAMETERS_TAG);
            }
            blockWeightsTotal += blockWeights[i] = blockWeight;
            blockBonemeals[i] = blockBonemeal;
            placePolicies[i] = placePolicy;

            if (stateCompound.getString(NAME_TAG).equals(NULL_BLOCK_NAME)) {
                // pseudo-block "ruins:null" leaves existing block at this position intact
                blockStates[i] = null;
                tileEntityData[i] = null;

                if (excessiveDebugging) {
                    RuinsMod.LOGGER.info("rule alternative: {}, {}", i + 1, NULL_BLOCK_NAME);
                }
            } else if (stateCompound.getString(NAME_TAG).equals(END_CRYSTAL_BLOCK_NAME)) {
                // pseudo-block "ruins:end_crystal" places bedrock with an end crystal, like the old "EnderCrystal"
                blockStates[i] = null;
                tileEntityData[i] = null;
                endCrystals[i] = true;

                if (excessiveDebugging) {
                    RuinsMod.LOGGER.info("rule alternative: {}, {}", i + 1, END_CRYSTAL_BLOCK_NAME);
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
    private static int extractBonemeal(int defaultValue, CompoundTag parameters) {
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

    /**
     * Gets the Ruins place parameter. "background" (the old "?" prefix) only replaces blocks the ground search counts
     * as ground, "foreground" (the old "!" prefix) only goes where the ground search looks through (air, plants, trees,
     * water/lava kept by preserve_water/preserve_lava). Any other value is left in place and reported as invalid.
     *
     * @param defaultValue place policy to use when the parameter is missing or invalid
     * @param parameters   the block's Ruins parameters; a valid place parameter is removed from them
     * @return PLACE_BACKGROUND, PLACE_FOREGROUND or defaultValue
     */
    private byte extractPlace(byte defaultValue, CompoundTag parameters) {
        byte place = defaultValue;
        if (parameters.contains(PARAMETER_PLACE_TAG, Tag.TAG_STRING)) {
            String value = parameters.getString(PARAMETER_PLACE_TAG);
            if (value.equals("background")) {
                place = PLACE_BACKGROUND;
            } else if (value.equals("foreground")) {
                place = PLACE_FOREGROUND;
            } else {
                return place;
            }
            parameters.remove(PARAMETER_PLACE_TAG);
        }
        return place;
    }

    private static final String PARAMETER_ENTITY_TAG = "entity";

    // get Ruins block_entity parameter (compound)
    private static CompoundTag extractEntity(CompoundTag defaultValue, CompoundTag parameters) {
        CompoundTag entity = defaultValue;
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
        return switch (direction) {
            case RuinsMod.DIR_EAST -> Rotation.CLOCKWISE_90;
            case RuinsMod.DIR_SOUTH -> Rotation.CLOCKWISE_180;
            case RuinsMod.DIR_WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public void doBlock(Level world, RandomSource random, BlockPos pos, int rotate) {
        if (chance < 1 && random.nextDouble() >= chance) {
            return;
        }
        if (!conditionMet(world, pos)) {
            return;
        }
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, pos, blocknum, rotate);
    }

    /**
     * Rule conditions: 1 = only on top of a solid block, 2 = only next to one, 3 = only under one; negative = the
     * opposite (only where there is none). "Solid" means not looked through by the ground search (not air, plants...).
     */
    private boolean conditionMet(Level world, BlockPos pos) {
        boolean positive = condition > 0;
        switch (Math.abs(condition)) {
            case 1:
                return owner.isIgnoredBlock(world.getBlockState(pos.below())) != positive;
            case 2:
                boolean allIgnored = owner.isIgnoredBlock(world.getBlockState(pos.east())) && owner.isIgnoredBlock(world.getBlockState(pos.south()))
                        && owner.isIgnoredBlock(world.getBlockState(pos.north())) && owner.isIgnoredBlock(world.getBlockState(pos.west()));
                return allIgnored != positive;
            case 3:
                return owner.isIgnoredBlock(world.getBlockState(pos.above())) != positive;
            default:
                return true;
        }
    }

    /** Conditions 1 and 2 are checked after the rest of the ruin is built (a torch needs its wall first). */
    public boolean runLater() {
        int c = Math.abs(condition);
        return c == 1 || c == 2;
    }

    /** Conditions 3 and 7 run after the late ones. */
    public boolean runLast() {
        int c = Math.abs(condition);
        return c == 3 || c == 7;
    }

    private void handleBlockSpawning(Level world, RandomSource random, BlockPos pos, int blocknum, int rotate) {
        if (!placeAllowed(world, pos, placePolicies[blocknum])) {
            return;
        }
        if (endCrystals[blocknum]) {
            spawnEndCrystal(world, random, pos);
            return;
        }
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

    /**
     * Background blocks only replace ground, foreground blocks only go where the ground search looks through.
     */
    private boolean placeAllowed(Level world, BlockPos pos, byte placePolicy) {
        if (placePolicy == PLACE_ANYWHERE) {
            return true;
        }
        boolean lookedThrough = owner.isIgnoredBlock(world.getBlockState(pos));
        return placePolicy == PLACE_BACKGROUND ? !lookedThrough : lookedThrough;
    }

    /**
     * Places bedrock with an end crystal in the same spot, the same way the old "EnderCrystal" block placed it.
     */
    private void spawnEndCrystal(Level world, RandomSource random, BlockPos pos) {
        realizeBlock(world, pos, Blocks.BEDROCK.defaultBlockState(), null);
        EndCrystal crystal = new EndCrystal(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        crystal.setYRot(random.nextFloat() * 360.0F);
        world.addFreshEntity(crystal);
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
                world.setBlock(position, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
            }
            boolean success = world.setBlock(position, blockState, PLACE_FLAGS);
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
                entity.load(nbtTagCompound);

                if (entity instanceof RandomizableContainerBlockEntity) {
                    // unwrap forgedata if needed?
                    if (nbtTagCompound.contains("ForgeData")) {
                        nbtTagCompound = nbtTagCompound.getCompound("ForgeData");
                    }
                    if (nbtTagCompound.contains("LootTable")) {
                        String lootTable = nbtTagCompound.getString("LootTable");
                        long lootSeed = nbtTagCompound.getLong("LootTableSeed");

                        ResourceLocation lootResourceLocation = ResourceLocation.tryParse(lootTable);
                        RandomizableContainerBlockEntity tileEntityLockableLoot = (RandomizableContainerBlockEntity) entity;
                        for (ResourceLocation lootTableResourceKey : BuiltInLootTables.all()) {
                            if (lootTableResourceKey.equals(lootResourceLocation)) {
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
