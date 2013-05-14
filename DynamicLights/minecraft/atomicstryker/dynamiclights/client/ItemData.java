package atomicstryker.dynamiclights.client;

public class ItemData
{
    private final int id;
    private final int meta;
    
    public ItemData(int i, int m)
    {
        id = i;
        meta = m;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof ItemData)
        {
            ItemData d = (ItemData) o;
            return d.id == id && d.meta == meta; 
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return id+meta;
    }
}
