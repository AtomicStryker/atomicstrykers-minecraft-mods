package atomicstryker.multimine.common;

import java.util.HashMap;

public class MultiMineConfig {

    private boolean blockRegenerationEnabled = true;
    private int initialBlockRegenDelayMillis = 5000;
    private int blockRegenIntervalMillis = 1000;
    private boolean debugMode = false;

    private HashMap<String, Boolean> bannedBlocks = new HashMap<>();
    private HashMap<String, Boolean> bannedItems = new HashMap<>();
    private boolean disableForAllTileEntities = false;
    private boolean disableAutoRegisterNames = false;

    public boolean isBlockRegenerationEnabled() {
        return blockRegenerationEnabled;
    }

    public void setBlockRegenerationEnabled(boolean blockRegenerationEnabled) {
        this.blockRegenerationEnabled = blockRegenerationEnabled;
    }

    public int getInitialBlockRegenDelayMillis() {
        return initialBlockRegenDelayMillis;
    }

    public void setInitialBlockRegenDelayMillis(int initialBlockRegenDelayMillis) {
        this.initialBlockRegenDelayMillis = initialBlockRegenDelayMillis;
    }

    public int getBlockRegenIntervalMillis() {
        return blockRegenIntervalMillis;
    }

    public void setBlockRegenIntervalMillis(int blockRegenIntervalMillis) {
        this.blockRegenIntervalMillis = blockRegenIntervalMillis;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public HashMap<String, Boolean> getBannedBlocks() {
        return bannedBlocks;
    }

    public HashMap<String, Boolean> getBannedItems() {
        return bannedItems;
    }

    public boolean isDisableForAllTileEntities() {
        return disableForAllTileEntities;
    }

    public void setDisableForAllTileEntities(boolean disableForAllTileEntities) {
        this.disableForAllTileEntities = disableForAllTileEntities;
    }

    public boolean isDisableAutoRegisterNames() {
        return disableAutoRegisterNames;
    }

    public void setDisableAutoRegisterNames(boolean disableAutoRegisterNames) {
        this.disableAutoRegisterNames = disableAutoRegisterNames;
    }
}
