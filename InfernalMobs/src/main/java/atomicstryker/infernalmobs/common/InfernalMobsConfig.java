package atomicstryker.infernalmobs.common;

import net.minecraft.util.Tuple;

import java.util.HashMap;

public class InfernalMobsConfig {

    private boolean antiMobFarm;
    private long mobFarmCheckIntervals;
    private float mobFarmDamageTrigger;

    public boolean isAntiMobFarm() {
        return antiMobFarm;
    }

    public void setAntiMobFarm(boolean antiMobFarm) {
        this.antiMobFarm = antiMobFarm;
    }

    public long getMobFarmCheckIntervals() {
        return mobFarmCheckIntervals;
    }

    public void setMobFarmCheckIntervals(long mobFarmCheckIntervals) {
        this.mobFarmCheckIntervals = mobFarmCheckIntervals;
    }

    public float getMobFarmDamageTrigger() {
        return mobFarmDamageTrigger;
    }

    public void setMobFarmDamageTrigger(float mobFarmDamageTrigger) {
        this.mobFarmDamageTrigger = mobFarmDamageTrigger;
    }
}
