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
        xMid = xMin + ((xMax-xMin)/2);
        yMid = yMin + ((yMax-yMin)/2);
        zMid = zMin + ((zMax-zMin)/2);
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
        xMid = xMin + ((xMax-xMin)/2);
        yMid = yMin + ((yMax-yMin)/2);
        zMid = zMin + ((zMax-zMin)/2);
    }
    
    public boolean intersectsWith(RuinData check)
    {
        if ((check.xMin >= xMin && check.xMin <= xMax) // check xMin inside this x
        || (check.xMax >= xMin && check.xMax <= xMax) // check xMax inside this x
        || (check.zMin >= zMin && check.zMin <= zMax) // check zMin inside this z
        || (check.zMax >= zMin && check.zMax <= zMax)) // check zMax inside this z
        {
            return (check.yMin >= yMin && check.yMin <= yMax) // check yMin inside this y
                || (check.yMax >= yMin && check.yMax <= yMax); // check yMax inside this y
        }
        return false;
    }
    
    /**
     * finds the minimum distance between the 2 bounding boxes
     * BRUTE FORCE, minus anything further than 512 blocks center to center
     * @param r other box
     * @return lowest distance found
     */
    public double getClosestDistanceBetweenBounds(RuinData r)
    {
        if (intersectsWith(r))
        {
        	return 0f;
        }
    	
        int midToMid = ((xMid-r.xMid)*(xMid-r.xMid)) + ((yMid-r.yMid)*(yMid-r.yMid)) + ((zMid-r.zMid)*(zMid-r.zMid));
        if (midToMid > 262144)
        {
            return Math.sqrt(midToMid);
        }
        
        double[][] vertices = {
                { xMin, yMin, zMin },
                { xMin, yMin, zMax },
                { xMax, yMin, zMax },
                { xMax, yMin, zMin },
                { xMin, yMax, zMin },
                { xMin, yMax, zMax },
                { xMax, yMax, zMax },
                { xMax, yMax, zMin },
                { xMin, yMin, zMid },
                { xMid, yMin, zMax },
                { xMax, yMin, zMid },
                { xMid, yMin, zMin },
                { xMin, yMax, zMid },
                { xMid, yMax, zMax },
                { xMax, yMax, zMid },
                { xMid, yMax, zMin },
                };
        double[][] verticesOther = {
                { r.xMin, r.yMin, r.zMin },
                { r.xMin, r.yMin, r.zMax },
                { r.xMax, r.yMin, r.zMax },
                { r.xMax, r.yMin, r.zMin },
                { r.xMin, r.yMax, r.zMin },
                { r.xMin, r.yMax, r.zMax },
                { r.xMax, r.yMax, r.zMax },
                { r.xMax, r.yMax, r.zMin },
                { r.xMin, r.yMin, r.zMid },
                { r.xMid, r.yMin, r.zMax },
                { r.xMax, r.yMin, r.zMid },
                { r.xMid, r.yMin, r.zMin },
                { r.xMin, r.yMax, r.zMid },
                { r.xMid, r.yMax, r.zMax },
                { r.xMax, r.yMax, r.zMid },
                { r.xMid, r.yMax, r.zMin },
                };
        
        double lowest = 99999d;
        for (double[] v : vertices)
        {
            for (double[] vO : verticesOther)
            {
                double vx = (v[0]-vO[0]) * (v[0]-vO[0]);
                double vy = (v[1]-vO[1]) * (v[1]-vO[1]);
                double vz = (v[2]-vO[2]) * (v[2]-vO[2]);
                lowest = Math.min(lowest, Math.sqrt(vx+vy+vz));
            }
        }
        
        return lowest;
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