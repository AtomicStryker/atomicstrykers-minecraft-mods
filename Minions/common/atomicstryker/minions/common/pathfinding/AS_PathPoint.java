package atomicstryker.minions.common.pathfinding;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.src.PathPoint;

public class AS_PathPoint extends PathPoint
{
    private final AS_PathPoint instance;
    private final Class ppClass;
    
    public AS_PathPoint(int par1, int par2, int par3)
    {
        super(par1, par2, par3);
        instance = this;
        ppClass = this.getClass().getSuperclass();
    }

    public void setIndex(int i)
    {
        ReflectionHelper.setPrivateValue(ppClass, instance, i, 4);
    }

    public void setTotalPathDistance(float f)
    {
        ReflectionHelper.setPrivateValue(ppClass, instance, f, 5);
    }

    public void setDistanceToNext(float f)
    {
        ReflectionHelper.setPrivateValue(ppClass, instance, f, 6);
    }

    public void setDistanceToTarget(float f)
    {
        ReflectionHelper.setPrivateValue(ppClass, instance, f, 7);
    }

    public void setPrevious(PathPoint pp)
    {
        ReflectionHelper.setPrivateValue(ppClass, instance, pp, 8);
    }
}
