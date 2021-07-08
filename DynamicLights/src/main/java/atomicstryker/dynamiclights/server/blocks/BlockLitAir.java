package atomicstryker.dynamiclights.server.blocks;

import atomicstryker.dynamiclights.server.DynamicLights;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class BlockLitAir extends AirBlock {

    public BlockLitAir(Properties properties) {
        super(properties);
    }

    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    public void randomTick(BlockState blockState, ServerWorld serverWorld, BlockPos blockPos, Random random) {
        if (!DynamicLights.isKnownLitPosition(serverWorld, blockPos)) {
            serverWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

}
