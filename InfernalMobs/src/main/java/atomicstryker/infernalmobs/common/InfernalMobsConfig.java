package atomicstryker.infernalmobs.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfernalMobsConfig {

    private boolean antiMobFarm;
    private long mobFarmCheckIntervals;
    private float mobFarmDamageTrigger;

    private int eliteRarity;
    private int ultraRarity;
    private int infernoRarity;

    private boolean useSimpleEntityClassNames;
    private boolean disableHealthBar;
    private double modHealthFactor;

    private double maxDamage;

    private List<String> droppedItemIDsElite = new ArrayList<>();
    private List<String> droppedItemIDsUltra = new ArrayList<>();
    private List<String> droppedItemIDsInfernal = new ArrayList<>();

    private List<String> dimensionIDBlackList = new ArrayList<>();

    private Map<String, Boolean> modsEnabled = new HashMap<>();

    private Map<String, Boolean> permittedentities = new HashMap<>();
    private Map<String, Boolean> entitiesalwaysinfernal = new HashMap<>();
    private Map<String, Double> entitybasehealth = new HashMap<>();

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

    public int getEliteRarity() {
        return eliteRarity;
    }

    public void setEliteRarity(int eliteRarity) {
        this.eliteRarity = eliteRarity;
    }

    public int getUltraRarity() {
        return ultraRarity;
    }

    public void setUltraRarity(int ultraRarity) {
        this.ultraRarity = ultraRarity;
    }

    public int getInfernoRarity() {
        return infernoRarity;
    }

    public void setInfernoRarity(int infernoRarity) {
        this.infernoRarity = infernoRarity;
    }

    public boolean isUseSimpleEntityClassNames() {
        return useSimpleEntityClassNames;
    }

    public void setUseSimpleEntityClassNames(boolean useSimpleEntityClassNames) {
        this.useSimpleEntityClassNames = useSimpleEntityClassNames;
    }

    public boolean isDisableHealthBar() {
        return disableHealthBar;
    }

    public void setDisableHealthBar(boolean disableHealthBar) {
        this.disableHealthBar = disableHealthBar;
    }

    public double getModHealthFactor() {
        return modHealthFactor;
    }

    public void setModHealthFactor(double modHealthFactor) {
        this.modHealthFactor = modHealthFactor;
    }

    public double getMaxDamage() {
        return maxDamage;
    }

    public void setMaxDamage(double maxDamage) {
        this.maxDamage = maxDamage;
    }

    public List<String> getDroppedItemIDsElite() {
        return droppedItemIDsElite;
    }

    public void setDroppedItemIDsElite(List<String> droppedItemIDsElite) {
        this.droppedItemIDsElite = droppedItemIDsElite;
    }

    public List<String> getDroppedItemIDsUltra() {
        return droppedItemIDsUltra;
    }

    public void setDroppedItemIDsUltra(List<String> droppedItemIDsUltra) {
        this.droppedItemIDsUltra = droppedItemIDsUltra;
    }

    public List<String> getDroppedItemIDsInfernal() {
        return droppedItemIDsInfernal;
    }

    public void setDroppedItemIDsInfernal(List<String> droppedItemIDsInfernal) {
        this.droppedItemIDsInfernal = droppedItemIDsInfernal;
    }

    public List<String> getDimensionIDBlackList() {
        return dimensionIDBlackList;
    }

    public void setDimensionIDBlackList(List<String> dimensionIDBlackList) {
        this.dimensionIDBlackList = dimensionIDBlackList;
    }

    public Map<String, Boolean> getModsEnabled() {
        return modsEnabled;
    }

    public void setModsEnabled(Map<String, Boolean> modsEnabled) {
        this.modsEnabled = modsEnabled;
    }

    public Map<String, Boolean> getPermittedentities() {
        return permittedentities;
    }

    public void setPermittedentities(Map<String, Boolean> permittedentities) {
        this.permittedentities = permittedentities;
    }

    public Map<String, Boolean> getEntitiesalwaysinfernal() {
        return entitiesalwaysinfernal;
    }

    public void setEntitiesalwaysinfernal(Map<String, Boolean> entitiesalwaysinfernal) {
        this.entitiesalwaysinfernal = entitiesalwaysinfernal;
    }

    public Map<String, Double> getEntitybasehealth() {
        return entitybasehealth;
    }

    public void setEntitybasehealth(Map<String, Double> entitybasehealth) {
        this.entitybasehealth = entitybasehealth;
    }
}
