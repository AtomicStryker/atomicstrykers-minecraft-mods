package atomicstryker.minions.common.pathfinding.jps;

/**
 * JPS skipping directions in an order that seems most efficient
 * @author AtomicStryker
 *
 */
public enum EnumDirection
{
    STRAIGHT(1,0),
    DIAG_LEFT(1,1),
    DIAG_RIGTH(1,-1),
    LEFT(0,1),
    RIGHT(0,-1),
    DIAG_B_LEFT(-1,1),
    DIAG_R_RIGHT(-1,-1),
    BACKWARDS(-1,0);
    
    final int xO;
    final int zO;
    
    EnumDirection(int xOffset, int zOffset)
    {
        xO = xOffset;
        zO = zOffset;
    }
    
    boolean isStraightMove()
    {
        return !(xO != 0 && zO != 0);
    }
}