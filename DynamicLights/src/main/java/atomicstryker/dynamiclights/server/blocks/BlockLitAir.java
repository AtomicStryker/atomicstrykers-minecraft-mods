package atomicstryker.dynamiclights.server.blocks;

import atomicstryker.dynamiclights.server.DynamicLights;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class BlockLitAir extends AirBlock {

    public BlockLitAir(Properties properties) {
        super(properties);
    }

    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    public void randomTick(BlockState blockState, ServerLevel serverWorld, BlockPos blockPos, Random random) {
        if (!DynamicLights.isKnownLitPosition(serverWorld, blockPos)) {
            serverWorld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

}
