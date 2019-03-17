package atomicstryker.ruins.common;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import java.io.PrintWriter;
import java.util.Random;

public class RuinTemplateRule {

    protected final IBlockState[] blockStates;
    protected final double[] blockWeights;
    protected double blockWeightsTotal;

    final RuinTemplate owner;
    private final PrintWriter debugPrinter;
    private final boolean excessiveDebugging;

    public RuinTemplateRule(PrintWriter dpw, RuinTemplate r, String rule, boolean debug) throws Exception {
        debugPrinter = dpw;
        owner = r;
        excessiveDebugging = debug;

        String[] stateStrings = RuleStringNbtHelper.splitRuleByBrackets(rule, debugPrinter);
        if (stateStrings == null || stateStrings.length == 0) {
            debugPrinter.println("could not find any blockstates in rule " + rule);
            int numblocks = 0;
            blockStates = new IBlockState[numblocks];
            blockWeights = new double[numblocks];
            return;
        }
        int numblocks = stateStrings.length;
        blockStates = new IBlockState[numblocks];
        blockWeights = new double[numblocks];
        blockWeightsTotal = 0;
        for (int i = 0; i < numblocks; i++) {
            // invalidate cached block state
            blockStates[i] = null;

            double blockWeight = 1;
            blockWeightsTotal += blockWeights[i] = blockWeight;

            // stateStrings[i] = "{nbt string}"
            blockStates[i] = RuleStringNbtHelper.blockStateFromString(stateStrings[i], debugPrinter);

            if (excessiveDebugging) {
                debugPrinter.printf("rule alternative: %d, %s\n", i + 1, blockStates[i].toString());
            }
        }
    }

    RuinTemplateRule(PrintWriter dpw, RuinTemplate r, final String rule) throws Exception {
        this(dpw, r, rule, false);
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

    public void doBlock(World world, Random random, int x, int y, int z) {
        doNormalBlock(world, random, x, y, z);
    }

    private void doNormalBlock(World world, Random random, int x, int y, int z) {
        int blocknum = getBlockNum(random);
        handleBlockSpawning(world, random, x, y, z, blocknum);
    }

    private void handleBlockSpawning(World world, Random random, int x, int y, int z, int blocknum) {
        IBlockState state = blockStates[blocknum];
        BlockPos pos = new BlockPos(x, y, z);
        if (excessiveDebugging) {
            debugPrinter.printf("About to place blockstate %s at pos %s\n", state.toString(), pos.toString());
        }
        placeBlock(world, blocknum, x, y, z);
    }

    private void placeBlock(World world, int blocknum, int x, int y, int z) {
        realizeBlock(world, x, y, z, blockStates[blocknum]);
    }

    private int getBlockNum(Random random) {
        // random selection using weights assigned in config file
        int blockIndex = 0;
        for (double selector = random.nextDouble() * blockWeightsTotal; (selector -= blockWeights[blockIndex]) >= 0; ++blockIndex)
            ;
        return blockIndex;
    }

    private void addChestGenChest(World world, Random random, int x, int y, int z, String gen) {
        TileEntityChest chest = (TileEntityChest) realizeBlock(world, x, y, z, Blocks.CHEST.getDefaultState());
        if (chest != null) {
            ResourceLocation lootTable;
            if (gen.contains(":")) {
                String[] pair = gen.split(":");
                lootTable = new ResourceLocation(pair[0], pair[1]);
            } else {
                lootTable = new ResourceLocation("minecraft", gen);
            }

            if (world.getServer() != null) {
                LootTable lootTableFromLocation = world.getServer().getLootTableManager().getLootTableFromLocation(lootTable);
                LootContext.Builder lootContextBuilder = new LootContext.Builder((WorldServer) world);
                lootTableFromLocation.fillInventory(chest, random, lootContextBuilder.build());
            }
        }
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

    // make specified block manifest in world, with given metadata and direction
    // returns associated tile entity, if there is one
    private TileEntity realizeBlock(World world, int x, int y, int z, IBlockState blockState) {
        TileEntity entity = null;
        if (world != null && blockState != null) {
            BlockPos position = new BlockPos(x, y, z);

            // clobber existing tile entity block, if any
            TileEntity existing_entity = world.getTileEntity(position);
            if (existing_entity != null) {
                if (existing_entity instanceof IInventory) {
                    ((IInventory) existing_entity).clear();
                }
                world.setBlockState(position, Blocks.AIR.getDefaultState(), 4);
            }

            if (world.setBlockState(position, blockState, 2)) {
                if ((entity = world.getTileEntity(position)) == null) {
                    // workaround for vanilla weirdness (bug?)
                    // double set required for some states (e.g., rails)
                    world.setBlockState(position, blockState, 2);
                }
            }
        }
        return entity;
    }
}
