package atomicstryker.multimine.common;

import java.util.Map;

public class MultiMineConfig {

    private boolean blockRegenEnabled;
    private long initialBlockRegenDelay;
    private long blockRegenInterval;
    private boolean debugMode;
    private Map<String, Boolean> bannedBlocks;
    private Map<String, Boolean> bannedItems;

    public boolean isBlockRegenEnabled() {
        return blockRegenEnabled;
    }

    public void setBlockRegenEnabled(boolean blockRegenEnabled) {
        this.blockRegenEnabled = blockRegenEnabled;
    }

    public long getInitialBlockRegenDelay() {
        return initialBlockRegenDelay;
    }

    public void setInitialBlockRegenDelay(long initialBlockRegenDelay) {
        this.initialBlockRegenDelay = initialBlockRegenDelay;
    }

    public long getBlockRegenInterval() {
        return blockRegenInterval;
    }

    public void setBlockRegenInterval(long blockRegenInterval) {
        this.blockRegenInterval = blockRegenInterval;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Map<String, Boolean> getBannedBlocks() {
        return bannedBlocks;
    }

    public void setBannedBlocks(Map<String, Boolean> bannedBlocks) {
        this.bannedBlocks = bannedBlocks;
    }

    public Map<String, Boolean> getBannedItems() {
        return bannedItems;
    }

    public void setBannedItems(Map<String, Boolean> bannedItems) {
        this.bannedItems = bannedItems;
    }
}
