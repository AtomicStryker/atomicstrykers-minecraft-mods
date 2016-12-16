package atomicstryker.multimine.common;

public class PartiallyMinedBlock
{
    private final BlockPos pos = new BlockPos();
    private final int dimension;
    private float progress;
    private long lastTimeMined;

    public PartiallyMinedBlock(int x, int y, int z, int dimension, float progress)
    {
        pos.x = x;
        pos.y = y;
        pos.z = z;
        this.dimension = dimension;
        this.progress = progress;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public int getDimension()
    {
        return dimension;
    }

    public float getProgress()
    {
        return progress;
    }

    public void setProgress(float i)
    {
        progress = i;
    }

    public boolean isFinished()
    {
        return progress >= 1.0f;
    }

    public long getLastTimeMined()
    {
        return lastTimeMined;
    }

    public void setLastTimeMined(long lastTimeMined)
    {
        this.lastTimeMined = lastTimeMined;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof PartiallyMinedBlock)
        {
            PartiallyMinedBlock p = (PartiallyMinedBlock) o;
            return p.getPos().equals(getPos());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return pos.hashCode();
    }
}
