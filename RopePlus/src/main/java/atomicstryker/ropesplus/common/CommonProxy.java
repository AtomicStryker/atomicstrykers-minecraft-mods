package atomicstryker.ropesplus.common;

import net.minecraftforge.common.config.Configuration;

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
    public float getShouldRopeChangeState()
    {
        return 0f;
    }
    
    @Override
    public void setShouldRopeChangeState(float f)
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
