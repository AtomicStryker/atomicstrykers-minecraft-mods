package atomicstryker.dynamiclights.server.blocks;

import atomicstryker.dynamiclights.server.DynamicLights;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Random;

public class BlockLitAir extends AirBlock {

    public BlockLitAir(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(BlockStateProperties.POWER, 15));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWER);
    }

    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverWorld, BlockPos blockPos, RandomSource random) {
        if (!DynamicLights.isKnownLitPosition(serverWorld, blockPos)) {
            // random ticks are a last resort cleanup, in case save/load left orphan dynamic light blocks
            serverWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverWorld, BlockPos blockPos, RandomSource rand) {
        if (!DynamicLights.isKnownLitPosition(serverWorld, blockPos)) {
            serverWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            // schedule a block tick 5 seconds into the future, as fallback for the block to clean itself up
            serverWorld.scheduleTick(blockPos, this, 150);
        }
    }

}