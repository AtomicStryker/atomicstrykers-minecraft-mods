package atomicstryker.dynamiclights.client.modules;

import java.util.ArrayList;
import java.util.List;

public class LightConfig {

    private List<String> itemsList = new ArrayList<>();
    private List<String> notWaterProofList = new ArrayList<>();
    private int updateInterval = 1000;

    public List<String> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<String> itemsList) {
        this.itemsList = itemsList;
    }

    public List<String> getNotWaterProofList() {
        return notWaterProofList;
    }

    public void setNotWaterProofList(List<String> notWaterProofList) {
        this.notWaterProofList = notWaterProofList;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }
}
