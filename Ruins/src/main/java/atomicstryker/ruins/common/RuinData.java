package atomicstryker.ruins.common;

public class RuinData implements Comparable<RuinData>
{
    public final int xMin, xMax, yMin, yMax, zMin, zMax, xMid, yMid, zMid;
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
    	//
    	// Parsing input line from RuinsPositionsFile.txt
    	// Since users are allowed to make changes to the file
    	// we also need to make sure, that min and max values 
    	// are correct.
    	//
        String[] split = string.split(" ");
        int min, max, tmp;
        min = Integer.valueOf(split[0]);
        max = Integer.valueOf(split[3]);
        if (min > max) {tmp = min; min = max; max = tmp;}
        xMin = min;
        xMax = max;
        
        min = Integer.valueOf(split[1]);
        max = Integer.valueOf(split[4]);
        if (min > max) {tmp = min; min = max; max = tmp;}
        yMin = min;
        yMax = max;
        
        min = Integer.valueOf(split[2]);
        max = Integer.valueOf(split[5]);
        if (min > max) {tmp = min; min = max; max = tmp;}
        zMin = min;
        zMax = max;
        
        name = split[6];
        
        xMid = (xMin+xMax)/2;
        yMid = (yMin+yMax)/2;
        zMid = (zMin+zMax)/2;
    }
    
    public boolean intersectsWith(RuinData check)
    {
    	// precondition: min <= max for each axis
    	
    	// 1. Basically, an intersection exists only if there is an intersection on each axis.
    	// 2. An intersection on one axis exists, if at least one boundary 
    	//    of one object lies inside (or on one of) the boundaries of 
    	//    the other object.
        return     (check.xMin <= xMax && check.xMax >= xMin)  // intersection on X
        		&& (check.zMin <= zMax && check.zMax >= zMin)  // intersection on Z
        		&& (check.yMin <= yMax && check.yMax >= yMin); // intersection on Y
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