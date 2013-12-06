package atomicstryker.findercompass.common;

public class CompassIntPair
{
    private final int blockID;
    private final int damage;
    
    public CompassIntPair(int a, int b)
    {
        blockID = a;
        damage = b;
    }
    
    public int getBlockID()
    {
        return blockID;
    }
    
    public int getDamage()
    {
        return damage;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof CompassIntPair)
        {
            CompassIntPair comp = (CompassIntPair)o;
            return comp.getBlockID() == blockID && comp.getDamage() == damage;
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return blockID + damage << 8;
    }
}
