package atomicstryker.infernalmobs.common;


import net.minecraft.entity.LivingEntity;

import java.util.concurrent.ConcurrentHashMap;

public class InfernalMobsServer implements ISidedProxy {

    private ConcurrentHashMap<LivingEntity, MobModifier> rareMobsServer;

    public InfernalMobsServer() {
        rareMobsServer = new ConcurrentHashMap<>();
    }

    @Override
    public ConcurrentHashMap<LivingEntity, MobModifier> getRareMobs() {
        return rareMobsServer;
    }

    @Override
    public void preInit() {

    }

    @Override
    public void load() {

    }

    @Override
    public void onHealthPacketForClient(int entID, float health, float maxhealth) {

    }

    @Override
    public void onKnockBackPacket(float xv, float zv) {

    }

    @Override
    public void onMobModsPacketToClient(String stringData, int entID) {

    }

    @Override
    public void onVelocityPacket(float xv, float yv, float zv) {

    }

    @Override
    public void onAirPacket(int air) {

    }
}
