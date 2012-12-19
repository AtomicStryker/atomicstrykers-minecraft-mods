package atomicstryker.ropesplus.common;

import java.io.File;
import java.util.Iterator;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorArrowDispense;
import atomicstryker.ropesplus.common.arrows.EntityArrow303;

public class CommonProxy implements IProxy
{
    
    @Override
    public void loadConfig(File configFile)
    {
        // NOOP
    }
    
    @Override
    public void load()
    {
        for(Iterator<EntityArrow303> iterator = RopesPlusCore.arrows.iterator(); iterator.hasNext();)
        {
            EntityArrow303 arrow = iterator.next();
            BlockDispenser.dispenseBehaviorRegistry.putObject(arrow.item, new BehaviorArrowDispense(FMLCommonHandler.instance().getMinecraftServerInstance()));
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
    public boolean getShouldHookShotPull()
    {
        return false;
    }
    
    @Override
    public void setShouldHookShotPull(boolean b)
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
