package atomicstryker.dynamiclights.server.blocks;

import atomicstryker.dynamiclights.server.DynamicLights;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class BlockLitWater extends FlowingFluidBlock {

    public BlockLitWater(FlowingFluid flowingFluid, Properties properties) {
        super(flowingFluid, properties);
    }

    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    public void randomTick(BlockState blockState, ServerWorld serverWorld, BlockPos blockPos, Random random) {
        if (!DynamicLights.isKnownLitPosition(serverWorld, blockPos)) {
            serverWorld.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
        }
    }

}
