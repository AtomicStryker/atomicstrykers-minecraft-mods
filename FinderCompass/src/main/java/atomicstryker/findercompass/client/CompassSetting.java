package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class CompassSetting {
    private final String displayedName;
    private final String featureNeedle;

    /**
     * Maps an int pair blockID/damage to a config array
     */
    private final HashMap<CompassTargetData, int[]> customNeedles;

    /**
     * Maps an int pair blockID/damage to the last detected Block BlockPos
     */
    private final ConcurrentHashMap<CompassTargetData, BlockPos> customNeedleTargets;

    /**
     * Maps an int pair blockID/damage to a newly found ChunkCoordinate, to overwrite the last known on next tick
     */
    private final HashMap<CompassTargetData, BlockPos> newFoundTargets;

    /**
     * Maps an int pair blockID/damage to it's AS_CompassWorker thread
     */
    private final HashMap<CompassTargetData, ThreadCompassWorker> compassWorkers;

    public CompassSetting(String name, String feature) {
        displayedName = name;
        featureNeedle = feature;
        customNeedles = new HashMap<>();
        customNeedleTargets = new ConcurrentHashMap<>();
        newFoundTargets = new HashMap<>();
        compassWorkers = new HashMap<>();
    }

    public String getFeatureNeedle() {
        return featureNeedle;
    }

    public HashMap<CompassTargetData, int[]> getCustomNeedles() {
        return customNeedles;
    }

    public ConcurrentHashMap<CompassTargetData, BlockPos> getCustomNeedleTargets() {
        return customNeedleTargets;
    }

    public HashMap<CompassTargetData, BlockPos> getNewFoundTargets() {
        return newFoundTargets;
    }

    public HashMap<CompassTargetData, ThreadCompassWorker> getCompassWorkers() {
        return compassWorkers;
    }

    public void onDisableThisConfig() {
        compassWorkers.values().stream().filter(worker -> worker != null && worker.isAlive()).forEach(ThreadCompassWorker::interrupt);
    }

    public String getName() {
        return displayedName;
    }
}
