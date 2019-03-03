package atomicstryker.findercompass.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompassConfig {

    private List<NeedleSet> needles = new ArrayList<>();

    public List<NeedleSet> getNeedles() {
        return needles;
    }

    public void setNeedles(List<NeedleSet> needles) {
        this.needles = needles;
    }

    public static class NeedleSet {
        private String name = "dummy";
        private Map<String, int[]> needles = new HashMap<>();

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
    }
}
