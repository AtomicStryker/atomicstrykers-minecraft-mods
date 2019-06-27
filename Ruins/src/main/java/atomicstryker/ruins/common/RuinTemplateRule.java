package atomicstryker.ruins.common;

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

import java.util.Random;

public class RuinTemplateRule {

    protected final BlockState[] blockStates;
    protected final CompoundNBT[] tileEntityData;
    protected final double[] blockWeights;
    final RuinTemplate owner;
    private final boolean excessiveDebugging;
    protected double blockWeightsTotal;

    public RuinTemplateRule(RuinTemplate r, String rule, boolean debug) {
        owner = r;
        excessiveDebugging = debug;

        String[] stateStrings = RuleStringNbtHelper.splitRuleByBrackets(rule);
        if (stateStrings == null || stateStrings.length == 0) {
            RuinsMod.LOGGER.error("could not find any blockstates in rule {}", rule);
            blockStates = new BlockState[0];
            blockWeights = new double[0];
            tileEntityData = new CompoundNBT[0];
            return;
        }
        int numblocks = stateStrings.length;
        blockStates = new BlockState[numblocks];
        blockWeights = new double[numblocks];
        tileEntityData = new CompoundNBT[numblocks];
        blockWeightsTotal = 0;
        for (int i = 0; i < numblocks; i++) {
            // invalidate cached block state
            blockStates[i] = null;

            double blockWeight = 1;
            blockWeightsTotal += blockWeights[i] = blockWeight;

            // stateStrings[i] = "{nbt string}"
            blockStates[i] = RuleStringNbtHelper.blockStateFromString(stateStrings[i]);
            tileEntityData[i] = RuleStringNbtHelper.tileEntityNBTFromString(stateStrings[i], 0, 0, 0);

            if (excessiveDebugging) {
                RuinsMod.LOGGER.error("rule alternative: {}, {}", i + 1, blockStates[i].toString());
            }
        }
    }

    RuinTemplateRule(RuinTemplate r, final String rule) {
        this(r, rule, false);
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

    @SuppressWarnings("unused")
    private boolean isNumber(String s) {
        if (s == null || s.equals("")) {
            return false;
        }
        try {
            int n = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void doBlock(World world, Random random, BlockPos pos, int rotate) {
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, pos, blocknum, rotate);
    }

    private void handleBlockSpawning(World world, Random random, BlockPos pos, int blocknum, int rotate) {
        // use vanilla rotation - lets see how this goes
        BlockState rotatedState = blockStates[blocknum].rotate(world, pos, getDirectionalRotation(rotate));
        if (excessiveDebugging) {
            RuinsMod.LOGGER.info("About to place blockstate {} at pos {}", rotatedState.toString(), pos.toString());
        }
        realizeBlock(world, pos, rotatedState, tileEntityData[blocknum]);
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
    private TileEntity realizeBlock(World world, BlockPos position, BlockState blockState, CompoundNBT tileEntityData) {
        TileEntity entity = null;
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
                if (tileEntityData != null) {
                    tileEntityData.putInt("x", position.getX());
                    tileEntityData.putInt("y", position.getY());
                    tileEntityData.putInt("z", position.getZ());

                    entity = TileEntity.create(tileEntityData);
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
        return entity;
    }
}
