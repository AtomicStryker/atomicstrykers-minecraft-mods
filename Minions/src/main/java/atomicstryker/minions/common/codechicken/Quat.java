package atomicstryker.minions.common.codechicken;

/**
 * @author ChickenBones
 * Quat class, part of ChickenCore
 * Available at: http://www.minecraftforum.net/topic/909223-125-smp-chickenbones-mods/
 */

import java.util.Formatter;
import java.util.Locale;

public class Quat
{

    public double x;
    public double y;
    public double z;
    public double s;

    public Quat(double d, double d1, double d2, double d3)
    {
        x = d1;
        y = d2;
        z = d3;
        s = d;
    }

    public static Quat aroundAxis(double ax, double ay, double az, double angle)
    {
        angle *= 0.5D;
        double d4 = Math.sin(angle);
        return new Quat(Math.cos(angle), ax * d4, ay * d4, az * d4);
    }

    public void rotate(Vector3 vec)
    {
        double d = -x * vec.x - y * vec.y - z * vec.z;
        double d1 = (s * vec.x + y * vec.z) - z * vec.y;
        double d2 = (s * vec.y - x * vec.z) + z * vec.x;
        double d3 = (s * vec.z + x * vec.y) - y * vec.x;
        vec.x = (d1 * s - d * x - d2 * z) + d3 * y;
        vec.y = ((d2 * s - d * y) + d1 * z) - d3 * x;
        vec.z = (d3 * s - d * z - d1 * y) + d2 * x;
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringbuilder, Locale.US);
        formatter.format("Quaternion:\n");
        formatter.format("  < %f %f %f %f >\n", s, x, y, z);
        formatter.close();
        return stringbuilder.toString();
    }

	public static Quat aroundAxis(Vector3 axis, double angle)
	{
		return aroundAxis(axis.x, axis.y, axis.z, angle);
	}

}
