package atomicstryker.dynamiclights.client;

import com.google.common.collect.Lists;

import java.util.List;

public class DynamicLightsConfig {
    private List<Integer> bannedDimensions = Lists.newArrayList(77, 88);

    public List<Integer> getBannedDimensions() {
        return bannedDimensions;
    }

    public void setBannedDimensions(List<Integer> bannedDimensions) {
        this.bannedDimensions = bannedDimensions;
    }
}
