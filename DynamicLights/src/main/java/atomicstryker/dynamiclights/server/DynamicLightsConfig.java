package atomicstryker.dynamiclights.server;

import com.google.common.collect.Lists;

import java.util.List;

public class DynamicLightsConfig {
    private List<String> bannedDimensions = Lists.newArrayList("the_nether", "the_end");

    public List<String> getBannedDimensions() {
        return bannedDimensions;
    }

    public void setBannedDimensions(List<String> bannedDimensions) {
        this.bannedDimensions = bannedDimensions;
    }
}
