package atomicstryker.ropesplus.common;

import java.util.Iterator;

import net.minecraft.block.BlockDispenser;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import atomicstryker.ropesplus.common.arrows.EntityArrow303;

public class CommonProxy implements IProxy
{
    
    @Override
    public void loadConfig(Configuration configFile)
    {
        // NOOP
    }
    
    @Override
    public void load()
    {
        for(Iterator<EntityArrow303> iterator = RopesPlusCore.arrows.iterator(); iterator.hasNext();)
        {
            EntityArrow303 arrow = iterator.next();
            Object arrowBehaviour = BlockDispenser.dispenseBehaviorRegistry.getObject(Item.arrow);
            BlockDispenser.dispenseBehaviorRegistry.putObject(arrow.item, arrowBehaviour);
        }
    }
    
    @Override
    public boolean getShouldHookShotDisconnect()
    {
        return false;
    }
    
    @Override
    public void setShouldHookShotDisconnect(boolean b)
    {
        // NOOP
    }
    
    @Override
    public float getShouldHookShotPull()
    {
        return 0f;
    }
    
    @Override
    public void setShouldHookShotPull(float f)
    {
        // NOOP
    }
    
    @Override
    public int getGrapplingHookRenderId()
    {
        return 0;
    }

    @Override
    public boolean getHasClientRopeOut()
    {
        return false;
    }

    @Override
    public void setHasClientRopeOut(boolean b)
    {
        // NOOP
    }
    
}
