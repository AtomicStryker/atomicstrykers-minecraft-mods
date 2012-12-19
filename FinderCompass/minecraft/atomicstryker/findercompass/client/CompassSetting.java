package atomicstryker.findercompass.client;

import java.util.HashMap;

import net.minecraft.util.ChunkCoordinates;
import atomicstryker.findercompass.common.AS_FinderCompassIntPair;

public class CompassSetting
{
    private final String displayedName;
    private boolean noDefaultNeedle;
    private boolean noEnderEyeNeedle;
    
    /**
     * Maps an int pair blockID/damage to a config array
     */
    private final HashMap<AS_FinderCompassIntPair, int[]> customNeedles;
    
    /**
     * Maps an int pair blockID/damage to the last detected Block ChunkCoordinates
     */
    private final HashMap<AS_FinderCompassIntPair, ChunkCoordinates> customNeedleTargets;
    
    /**
     * Maps an int pair blockID/damage to a newly found ChunkCoordinate, to overwrite the last known on next tick
     */
    private final HashMap<AS_FinderCompassIntPair, ChunkCoordinates> newFoundTargets;
    
    /**
     * Maps an int pair blockID/damage to it's AS_CompassWorker thread
     */
    private final HashMap<AS_FinderCompassIntPair, AS_CompassWorker> compassWorkers;
    
    public CompassSetting(String name)
    {
        displayedName = name;
        noDefaultNeedle = false;
        noEnderEyeNeedle = false;
        customNeedles = new HashMap<AS_FinderCompassIntPair, int[]>();
        customNeedleTargets = new HashMap<AS_FinderCompassIntPair, ChunkCoordinates>();
        newFoundTargets = new HashMap<AS_FinderCompassIntPair, ChunkCoordinates>();
        compassWorkers = new HashMap<AS_FinderCompassIntPair, AS_CompassWorker>();
    }
    
    public void setHasDefaultNeedle(boolean input)
    {
        noDefaultNeedle = !input;
    }
    
    public void setHasStrongholdNeedle(boolean input)
    {
        noEnderEyeNeedle = !input;
    }
    
    public boolean getHasDefaultNeedle()
    {
        return !noDefaultNeedle;
    }
    
    public boolean getHasStrongholdNeedle()
    {
        return !noEnderEyeNeedle;
    }
    
    public HashMap<AS_FinderCompassIntPair, int[]> getCustomNeedles()
    {
        return customNeedles;
    }
    
    public HashMap<AS_FinderCompassIntPair, ChunkCoordinates> getCustomNeedleTargets()
    {
        return customNeedleTargets;
    }
    
    public HashMap<AS_FinderCompassIntPair, ChunkCoordinates> getNewFoundTargets()
    {
        return newFoundTargets;
    }
    
    public HashMap<AS_FinderCompassIntPair, AS_CompassWorker> getCompassWorkers()
    {
        return compassWorkers;
    }
    
    public void onDisableThisConfig()
    {
        for (AS_CompassWorker worker : compassWorkers.values())
        {
            if (worker != null && worker.isAlive())
            {
                worker.interrupt();
            }
        }
    }

    public String getName()
    {
        return displayedName;
    }
}
