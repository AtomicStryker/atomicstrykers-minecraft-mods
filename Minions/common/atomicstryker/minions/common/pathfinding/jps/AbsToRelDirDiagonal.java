package atomicstryker.minions.common.pathfinding.jps;

/**
 * This thing can translate between my logical EnumDirection and MC Coordinates
 * For diagonal movement
 * @author AtomicStryker
 *
 */
public class AbsToRelDirDiagonal
{
    private final int[][] rotated;
    
    public AbsToRelDirDiagonal(int xS, int zS)
    {
        rotated = new int[EnumDirection.values().length][2];
        
        rotated[EnumDirection.STRAIGHT.ordinal()][0] = xS;
        rotated[EnumDirection.STRAIGHT.ordinal()][1] = zS;
        if (xS == 1)
        {
            rotated[EnumDirection.LEFT.ordinal()][0] = -xS;
            rotated[EnumDirection.LEFT.ordinal()][1] = zS;
        }
        else
        {
            if (zS == -1)
            {
                rotated[EnumDirection.LEFT.ordinal()][0] = xS;
                rotated[EnumDirection.LEFT.ordinal()][1] = -zS;
            }
            else
            {
                rotated[EnumDirection.LEFT.ordinal()][0] = -xS;
                rotated[EnumDirection.LEFT.ordinal()][1] = zS;
            }
        }
        
        rotated[EnumDirection.BACKWARDS.ordinal()][0] = -rotated[EnumDirection.STRAIGHT.ordinal()][0];
        rotated[EnumDirection.BACKWARDS.ordinal()][1] = -rotated[EnumDirection.STRAIGHT.ordinal()][1];
        rotated[EnumDirection.RIGHT.ordinal()][0] = -rotated[EnumDirection.LEFT.ordinal()][0];
        rotated[EnumDirection.RIGHT.ordinal()][1] = -rotated[EnumDirection.LEFT.ordinal()][1];
    }

    public int[] getRotatedOffsets(EnumDirection d)
    {
        return rotated[d.ordinal()];
    }
}