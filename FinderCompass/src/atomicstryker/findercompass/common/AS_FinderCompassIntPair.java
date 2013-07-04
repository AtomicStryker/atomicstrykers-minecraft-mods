package atomicstryker.findercompass.common;

public class AS_FinderCompassIntPair
{
    private final int blockID;
    private final int damage;
    
    public AS_FinderCompassIntPair(int a, int b)
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
        if (o instanceof AS_FinderCompassIntPair)
        {
            AS_FinderCompassIntPair comp = (AS_FinderCompassIntPair)o;
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
