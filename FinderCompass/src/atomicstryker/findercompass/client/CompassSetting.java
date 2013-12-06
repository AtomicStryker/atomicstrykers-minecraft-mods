package atomicstryker.findercompass.client;

import java.util.HashMap;

import net.minecraft.util.ChunkCoordinates;
import atomicstryker.findercompass.common.CompassIntPair;

public class CompassSetting
{
    private final String displayedName;
    private boolean noEnderEyeNeedle;
    
    /**
     * Maps an int pair blockID/damage to a config array
     */
    private final HashMap<CompassIntPair, int[]> customNeedles;
    
    /**
     * Maps an int pair blockID/damage to the last detected Block ChunkCoordinates
     */
    private final HashMap<CompassIntPair, ChunkCoordinates> customNeedleTargets;
    
    /**
     * Maps an int pair blockID/damage to a newly found ChunkCoordinate, to overwrite the last known on next tick
     */
    private final HashMap<CompassIntPair, ChunkCoordinates> newFoundTargets;
    
    /**
     * Maps an int pair blockID/damage to it's AS_CompassWorker thread
     */
    private final HashMap<CompassIntPair, ThreadCompassWorker> compassWorkers;
    
    public CompassSetting(String name)
    {
        displayedName = name;
        noEnderEyeNeedle = false;
        customNeedles = new HashMap<CompassIntPair, int[]>();
        customNeedleTargets = new HashMap<CompassIntPair, ChunkCoordinates>();
        newFoundTargets = new HashMap<CompassIntPair, ChunkCoordinates>();
        compassWorkers = new HashMap<CompassIntPair, ThreadCompassWorker>();
    }
    
    public void setHasStrongholdNeedle(boolean input)
    {
        noEnderEyeNeedle = !input;
    }
    
    public boolean isStrongholdNeedleEnabled()
    {
        return !noEnderEyeNeedle;
    }
    
    public HashMap<CompassIntPair, int[]> getCustomNeedles()
    {
        return customNeedles;
    }
    
    public HashMap<CompassIntPair, ChunkCoordinates> getCustomNeedleTargets()
    {
        return customNeedleTargets;
    }
    
    public HashMap<CompassIntPair, ChunkCoordinates> getNewFoundTargets()
    {
        return newFoundTargets;
    }
    
    public HashMap<CompassIntPair, ThreadCompassWorker> getCompassWorkers()
    {
        return compassWorkers;
    }
    
    public void onDisableThisConfig()
    {
        for (ThreadCompassWorker worker : compassWorkers.values())
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
