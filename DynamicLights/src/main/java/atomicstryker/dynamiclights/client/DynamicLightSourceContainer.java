package atomicstryker.dynamiclights.client;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;

/**
 * 
 * @author AtomicStryker
 * 
 * Container class to keep track of IDynamicLightSource instances. Remembers
 * their last position and calls World updates if they move.
 *
 */
public class DynamicLightSourceContainer
{
    private final IDynamicLightSource lightSource;
    
    private int prevX;
    private int prevY;
    private int prevZ;
    
    private int x;
    private int y;
    private int z;
    
    public DynamicLightSourceContainer(IDynamicLightSource light)
    {
        lightSource = light;
        x = y = z = prevX = prevY = prevZ = 0;
    }
    
    /**
     * Update passed on from the World tick. Checks for the Light Source Entity to be alive,
     * and for it to have changed Coordinates. Marks it's current Block for Update if it has
     * moved. When this method returns true, the Light Source Entity has died and it should
     * be removed from the List!
     * 
     * @return true when the Light Source has died, false otherwise
     */
    public boolean onUpdate()
    {
        Entity ent = lightSource.getAttachmentEntity();
        if (!ent.isEntityAlive())
        {
            return true;
        }
        
        if (hasEntityMoved(ent))
        {
            /*
             * This is the critical point, by this we tell Minecraft to ask for the BlockLight value
             * at the coordinates, which in turn triggers they Dynamic Lights response pointing to
             * this Light's value, which in turn has Minecraft update all surrounding Blocks :3
             * 
             * We also have to call an update for the previous coordinates, otherwise they would
             * stay lit up.
             */
            ent.worldObj.checkLightFor(EnumSkyBlock.BLOCK, new BlockPos(x, y, z));
            ent.worldObj.checkLightFor(EnumSkyBlock.BLOCK, new BlockPos(prevX, prevY, prevZ));
        }
        
        return false;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public int getZ()
    {
        return z;
    }
    
    public IDynamicLightSource getLightSource()
    {
        return lightSource;
    }

    /**
     * Checks for the Entity coordinates to have changed.
     * Updates internal Coordinates to new position if so.
     * @return true when Entities x, y or z changed, false otherwise
     */
    private boolean hasEntityMoved(Entity ent)
    {
        int newX = MathHelper.floor_double(ent.posX);
        int newY = MathHelper.floor_double(ent.posY);
        int newZ = MathHelper.floor_double(ent.posZ);
        
        if (newX != x || newY != y || newZ != z)
        {
            prevX = x;
            prevY = y;
            prevZ = z;
            x = newX;
            y = newY;
            z = newZ;
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof DynamicLightSourceContainer)
        {
            DynamicLightSourceContainer other = (DynamicLightSourceContainer) o;
            if (other.lightSource == this.lightSource)
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return lightSource.getAttachmentEntity().getEntityId();
    }
}
