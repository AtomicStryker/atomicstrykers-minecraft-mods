package atomicstryker.minions.common.codechicken;

/**
 * @author ChickenBones
 * Quat class, part of ChickenCore
 * Available at: http://www.minecraftforum.net/topic/909223-125-smp-chickenbones-mods/
 */

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Vector3
{
    public double x;
    public double y;
    public double z;

    public Vector3()
    {
    }

    public Vector3(double d, double d1, double d2)
    {
        x = d;
        y = d1;
        z = d2;
    }

    public Vector3(Vector3 vec)
    {
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    public Vector3 copy()
    {
    	return new Vector3(this);
    }
    
    public static Vector3 fromEntityCenter(Entity e)
	{
    	return new Vector3(e.posX, e.posY - e.yOffset + e.height/2, e.posZ);
	}

	public static Vector3 fromVec3(Vec3 vec)
	{
		return new Vector3(vec.xCoord, vec.yCoord, vec.zCoord);
	}

    public double dotProduct(Vector3 vec)
    {
        double d = vec.x * x + vec.y * y + vec.z * z;
        
        if(d > 1 && d < 1.00001)
        	d = 1;
        else if(d < -1 && d > -1.00001)
        	d = -1;
        return d;
    }

    public Vector3 crossProduct(Vector3 vec)
    {
        double d = y * vec.z - z * vec.y;
        double d1 = z * vec.x - x * vec.z;
        double d2 = x * vec.y - y * vec.x;
        x = d;
        y = d1;
        z = d2;
        return this;
    }

    public Vector3 add(double d, double d1, double d2)
    {
        x += d;
        y += d1;
        z += d2;
        return this;
    }

    public Vector3 add(Vector3 vec)
    {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public Vector3 subtract(Vector3 vec)
    {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public Vector3 multiply(double d)
    {
        x *= d;
        y *= d;
        z *= d;
        return this;
    }

    public double mag()
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3 normalize()
    {
        double d = mag();
        if(d != 0)
        {
            multiply(1 / d);
        }
        return this;
    }

    public String toString()
    {
    	MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
    	return "Vector: "+new BigDecimal(x, cont)+", "+new BigDecimal(y, cont)+", "+new BigDecimal(z, cont);
    }
    
	public Vector3 perpendicular()
	{
		if(z == 0)
		{
			return zCrossProduct();
		}
		else
		{
			return xCrossProduct();
		}
	}
	
	public Vector3 xCrossProduct()
	{
		double d = z;
		double d1 = -y;
		x = 0;
		y = d;
		z = d1;
		return this;
	}
	
	public Vector3 zCrossProduct()
	{
		double d = y;
		double d1 = -x;
		x = d;
		y = d1;
		z = 0;
		return this;
	}
	
	public Vector3 rotate(double angle, Vector3 axis)
	{
		Quat.aroundAxis(axis.copy().normalize(), angle).rotate(this);
		return this;
	}

	public Vec3 toVec3D()
	{
		return Vec3.createVectorHelper(x, y, z);
	}

	public double angle(Vector3 vec)
	{
		return Math.acos(copy().normalize().dotProduct(vec.copy().normalize()));
	}
	
	@Override
	public boolean equals(Object o)
	{
	    if (o instanceof Vector3)
	    {
	        Vector3 v = (Vector3) o;
	        return v.x == x && v.z == z && v.y == y;
	    }
	    return false;
	}
	
	@Override
	public int hashCode()
	{
	    return (int) (x+y+z);
	}
}
