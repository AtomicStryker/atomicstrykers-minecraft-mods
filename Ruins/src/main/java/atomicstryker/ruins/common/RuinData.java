package atomicstryker.ruins.common;

public class RuinData implements Comparable<RuinData>
{
    private final int xMin, xMax, yMin, yMax, zMin, zMax, xMid, yMid, zMid;
    public final String name;

    public RuinData(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax, String n)
    {
        xMin = xmin;
        xMax = xmax;
        yMin = ymin;
        yMax = ymax;
        zMin = zmin;
        zMax = zmax;
        name = n;
        xMid = (xMin+xMax)/2;
        yMid = (yMin+yMax)/2;
        zMid = (zMin+zMax)/2;
    }

    public RuinData(String string)
    {
        String[] split = string.split(" ");
        xMin = Integer.valueOf(split[0]);
        yMin = Integer.valueOf(split[1]);
        zMin = Integer.valueOf(split[2]);
        xMax = Integer.valueOf(split[3]);
        yMax = Integer.valueOf(split[4]);
        zMax = Integer.valueOf(split[5]);
        name = split[6];
        xMid = (xMin+xMax)/2;
        yMid = (yMin+yMax)/2;
        zMid = (zMin+zMax)/2;
    }
    
    public boolean intersectsWith(RuinData check)
    {
        if ((check.xMin >= xMin && check.xMin <= xMax) || (check.xMax >= xMin && check.xMax <= xMax) || (check.zMin >= zMin && check.zMin <= zMax)
                || (check.zMax >= zMin && check.zMax <= zMax))
        {
            return check.yMin >= yMin && check.yMax <= yMax;
        }
        return false;
    }
    
    public boolean collisionLowerBoundsPossible(RuinData check)
    {
        return check.xMin >= xMin || check.zMin >= zMin || check.yMin >= yMin;
    }

    public boolean collisionHigherBoundsPossible(RuinData check)
    {
        return check.xMax <= xMax || check.zMax <= zMax || check.zMax <= yMax;
    }
    
    public float getDistanceSqTo(RuinData r)
    {
        return (r.xMid-xMid)*(r.xMid-xMid)+(r.yMid-yMid)*(r.yMid-yMid)+(r.zMid-zMid)*(r.zMid-zMid);
    }

    @Override
    public String toString()
    {
        return xMin + " " + yMin + " " + zMin + " " + xMax + " " + yMax + " " + zMax + " " + name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof RuinData)
        {
            RuinData r = (RuinData) o;
            return r.xMin == xMin && r.yMin == yMin && r.zMin == zMin && r.name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return xMid + zMid << 8 + yMid << 16;
    }

    @Override
    public int compareTo(RuinData r)
    {
        if (r.xMin == xMin && r.zMin == zMin && r.yMin == yMin)
        {
            return 0;
        }
        if (r.xMin < xMin)
        {
            return 1;
        }
        return -1;
    }
}