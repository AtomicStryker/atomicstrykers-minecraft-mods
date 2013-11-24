package atomicstryker.ruins.common;


public class RuinData implements Comparable<RuinData>
{
    private final int xMin, xMax, yMin, yMax, zMin, zMax;
    private final String name;

    public RuinData(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax, String n)
    {
        xMin = xmin;
        xMax = xmax;
        yMin = ymin;
        yMax = ymax;
        zMin = zmin;
        zMax = zmax;
        name = n;
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
    }

    public boolean collides(RuinData check)
    {
        if (((check.xMin >= xMin) && (check.xMin <= xMax))
        || ((check.xMax >= xMin) && (check.xMax <= xMax))
        || ((check.zMin >= zMin) && (check.zMin <= zMax))
        || ((check.zMax >= zMin) && (check.zMax <= zMax)))
        {
            return check.yMin >= yMin && check.yMax <= yMax;
        }
        return false;
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
        return (int)(xMin ^ (xMin >> 32) ^ yMin ^ (yMin >> 32) ^ zMin ^ (zMin >> 32));
    }

    @Override
    public int compareTo(RuinData r)
    {
        if (r.xMin == xMin && r.zMin == zMin && r.yMin == yMin)
        {
            return 0;
        }
        if (r.xMin < xMin || r.zMin < zMin || r.yMin < yMin)
        {
            return 1;
        }
        return -1;
    }
}