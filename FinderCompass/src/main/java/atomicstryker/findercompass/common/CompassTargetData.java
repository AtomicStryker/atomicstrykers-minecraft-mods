package atomicstryker.findercompass.common;

import net.minecraft.block.Block;

public class CompassTargetData
{
    private final Block blockID;
    private final int damage;
    
    public CompassTargetData(Block a, int b)
    {
        blockID = a;
        damage = b;
    }
    
    public Block getBlockID()
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
        if (o instanceof CompassTargetData)
        {
            CompassTargetData comp = (CompassTargetData)o;
            return comp.getBlockID() == blockID && comp.getDamage() == damage;
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return blockID.func_149739_a().hashCode();
    }
}
