package atomicstryker.findercompass.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompassConfig {

    private List<NeedleSet> needles = new ArrayList<>();

    private double onScreenPositionWidth;
    private double onScreenPositionHeight;
    private double needleWidthOfScreenWidth;
    private double needleHeightOfScreenHeight;
    private boolean mustHoldCompassInHandToBeActive;

    public List<NeedleSet> getNeedles() {
        return needles;
    }

    public void setNeedles(List<NeedleSet> needles) {
        this.needles = needles;
    }

    public double getOnScreenPositionWidth() {
        return onScreenPositionWidth;
    }

    public void setOnScreenPositionWidth(double onScreenPositionWidth) {
        this.onScreenPositionWidth = onScreenPositionWidth;
    }

    public double getOnScreenPositionHeight() {
        return onScreenPositionHeight;
    }

    public void setOnScreenPositionHeight(double onScreenPositionHeight) {
        this.onScreenPositionHeight = onScreenPositionHeight;
    }

    public double getNeedleWidthOfScreenWidth() {
        return needleWidthOfScreenWidth;
    }

    public void setNeedleWidthOfScreenWidth(double needleWidthOfScreenWidth) {
        this.needleWidthOfScreenWidth = needleWidthOfScreenWidth;
    }

    public double getNeedleHeightOfScreenHeight() {
        return needleHeightOfScreenHeight;
    }

    public void setNeedleHeightOfScreenHeight(double needleHeightOfScreenHeight) {
        this.needleHeightOfScreenHeight = needleHeightOfScreenHeight;
    }

    public boolean isMustHoldCompassInHandToBeActive() {
        return mustHoldCompassInHandToBeActive;
    }

    public void setMustHoldCompassInHandToBeActive(boolean mustHoldCompassInHandToBeActive) {
        this.mustHoldCompassInHandToBeActive = mustHoldCompassInHandToBeActive;
    }

    public static class NeedleSet {
        private String name = "dummy";
        private Map<String, int[]> needles = new HashMap<>();
        private String featureNeedle = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, int[]> getNeedles() {
            return needles;
        }

        public void setNeedles(Map<String, int[]> needles) {
            this.needles = needles;
        }

        public String getFeatureNeedle() {
            return featureNeedle;
        }

        public void setFeatureNeedle(String featureNeedle) {
            this.featureNeedle = featureNeedle;
        }
    }
}
