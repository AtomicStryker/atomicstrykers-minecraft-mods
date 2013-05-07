package atomicstryker.astarpathing;

import java.lang.reflect.Field;

import net.minecraft.pathfinding.PathPoint;

/**
 * PathPoint extension for field access.
 * 
 * 
 * @author AtomicStryker
 *
 */
public class AS_PathPoint extends PathPoint
{
    private static boolean init = false;
    private static Field fieldIndex;
    private static Field fieldTotalDistance;
    private static Field fieldDistanceToNext;
    private static Field fieldDistanceToTarget;
    private static Field fieldPrevPathPoint;
    
    public AS_PathPoint(int par1, int par2, int par3)
    {
        super(par1, par2, par3);
        
        if (!init)
        {
            Class<?> ppClass = getClass().getSuperclass();
            fieldIndex = ppClass.getDeclaredFields()[4];
            fieldIndex.setAccessible(true);
            fieldTotalDistance = ppClass.getDeclaredFields()[5];
            fieldTotalDistance.setAccessible(true);
            fieldDistanceToNext = ppClass.getDeclaredFields()[6];
            fieldDistanceToNext.setAccessible(true);
            fieldDistanceToTarget = ppClass.getDeclaredFields()[7];
            fieldDistanceToTarget.setAccessible(true);
            fieldPrevPathPoint = ppClass.getDeclaredFields()[8];
            fieldPrevPathPoint.setAccessible(true);
            init = true;
        }
    }
    
    private void setFieldValue(Field f, Object v)
    {
        try
        {
            f.set(this, v);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setIndex(int i)
    {
        setFieldValue(fieldIndex, i);
    }

    public void setTotalPathDistance(float f)
    {
        setFieldValue(fieldTotalDistance, f);
    }

    public void setDistanceToNext(float f)
    {
        setFieldValue(fieldDistanceToNext, f);
    }

    public void setDistanceToTarget(float f)
    {
        setFieldValue(fieldDistanceToTarget, f);
    }

    public void setPrevious(PathPoint pp)
    {
        setFieldValue(fieldPrevPathPoint, pp);
    }
}
