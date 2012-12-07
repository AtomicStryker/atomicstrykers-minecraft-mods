package atomicstryker.ropesplus.common;

import java.io.File;
import java.util.HashMap;

import net.minecraft.src.EntityPlayer;

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
        // NOOP
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
