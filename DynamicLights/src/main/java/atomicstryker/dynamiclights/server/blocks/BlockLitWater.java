package atomicstryker.dynamiclights.server.blocks;

import atomicstryker.dynamiclights.server.DynamicLights;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.Random;

public class BlockLitWater extends LiquidBlock {

    public BlockLitWater(FlowingFluid flowingFluid, Properties properties) {
        super(flowingFluid, properties);
    }

    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    public void randomTick(BlockState blockState, ServerLevel serverWorld, BlockPos blockPos, Random random) {
        if (!DynamicLights.isKnownLitPosition(serverWorld, blockPos)) {
            serverWorld.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
        }
    }

}
