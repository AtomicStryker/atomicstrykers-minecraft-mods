package atomicstryker.ropesplus.common;

import java.io.File;

import net.minecraft.src.EntityPlayer;

public interface IProxy
{
    public void loadConfig(File configFile);
    
    public void load();

    public boolean getShouldHookShotDisconnect();

    public void setShouldHookShotDisconnect(boolean b);
    
    public boolean getShouldHookShotPull();

    public void setShouldHookShotPull(boolean b);

    public int getGrapplingHookRenderId();

    public boolean getHasClientRopeOut();

    public void setHasClientRopeOut(boolean b);
}
