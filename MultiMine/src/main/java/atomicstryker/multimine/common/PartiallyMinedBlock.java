package atomicstryker.multimine.common;

public class PartiallyMinedBlock
{
    private final int x;
    private final int y;
    private final int z;
    private final int dimension;
    private int progress;
    private long lastTimeMined;
    
    public PartiallyMinedBlock(int x, int y, int z, int dimension, int progress)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.progress = progress;
    }

    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public int getZ()
    {
        return z;
    }
    
    public int getDimension()
    {
        return dimension;
    }
    
    public int getProgress()
    {
        return progress;
    }
    
    public void setProgress(int i)
    {
        progress = i;
    }
    
    public void advanceProgress()
    {
        progress++;
    }
    
    public boolean isFinished()
    {
        return progress > 9;
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
            if (p.getX() == x
            && p.getY() == y
            && p.getZ() == z)
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return x*8123 + y + z*2546;
    }
}
