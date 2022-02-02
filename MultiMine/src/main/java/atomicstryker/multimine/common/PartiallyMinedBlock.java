package atomicstryker.multimine.common;


import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class PartiallyMinedBlock {
    private final BlockPos pos;
    private final ResourceKey<Level> dimension;
    private float progress;
    private long lastTimeMined;

    public PartiallyMinedBlock(int x, int y, int z, ResourceKey<Level> dimension, float progress) {
        this.pos = new BlockPos(x, y, z);
        this.dimension = dimension;
        this.progress = progress;
    }

    public BlockPos getPos() {
        return pos;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float i) {
        progress = i;
    }

    public boolean isFinished() {
        return progress >= 1.0f;
    }

    public long getLastTimeMined() {
        return lastTimeMined;
    }

    public void setLastTimeMined(long lastTimeMined) {
        this.lastTimeMined = lastTimeMined;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PartiallyMinedBlock) {
            PartiallyMinedBlock p = (PartiallyMinedBlock) o;
            return p.getPos().equals(getPos());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
