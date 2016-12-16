package atomicstryker.multimine.common;

public class BlockPos
{
    public int x;
    public int y;
    public int z;

    public BlockPos(int x2, int y2, int z2)
    {
        x = x2;
        y = y2;
        z = z2;
    }

    public BlockPos()
    {
        this(0, 0, 0);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof BlockPos)
        {
            BlockPos b = (BlockPos) o;
            return b.x == x && b.y == y && b.z == z;
        }
        return false;
    }
}
