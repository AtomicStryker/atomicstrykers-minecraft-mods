package atomicstryker.findercompass.common;


import net.minecraft.block.BlockState;

public class CompassTargetData {

    private final BlockState blockState;

    public CompassTargetData(BlockState state) {
        blockState = state;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CompassTargetData) {
            CompassTargetData comp = (CompassTargetData) o;
            return comp.getBlockState() == getBlockState();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        if (blockState.getBlock().getStateContainer().getProperties().isEmpty()) {
            result = blockState.getBlock().hashCode();
        } else {
            result = blockState.hashCode();
        }
        return result;
    }
}
