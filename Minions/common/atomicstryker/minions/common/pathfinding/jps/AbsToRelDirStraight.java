package atomicstryker.minions.common.pathfinding.jps;

/**
 * This thing can translate between my logical EnumDirection and MC Coordinates
 * For non-diagonal movement
 * @author AtomicStryker
 *
 */
public class AbsToRelDirStraight
{
    private final boolean swapXZ;
    private final boolean invertX;
    private final boolean invertZ;
    
    public AbsToRelDirStraight(int xS, int zS)
    {
        if (xS > 0)
        {
            swapXZ = false;
            invertX = false;
            invertZ = true;
        }
        else if (xS < 0)
        {
            swapXZ = false;
            invertX = true;
            invertZ = false;
        }
        else if (zS > 0)
        {
            swapXZ = true;
            invertX = false;
            invertZ = false;
        }
        else
        {
            swapXZ = true;
            invertX = true;
            invertZ = true;
        }
    }
    
    public int[] getRotatedOffsets(EnumDirection dir)
    {
        int[] r = new int[2];
        int x = invertX ? -dir.xO : dir.xO;
        int z = invertZ ? -dir.zO : dir.zO;
        r[0] = swapXZ ? z : x;
        r[1] = swapXZ ? x : z;
        return r;
    }
}