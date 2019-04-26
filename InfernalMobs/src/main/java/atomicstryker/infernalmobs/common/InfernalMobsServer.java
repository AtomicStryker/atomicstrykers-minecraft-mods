package atomicstryker.infernalmobs.common;

import net.minecraft.entity.EntityLivingBase;

import java.util.concurrent.ConcurrentHashMap;

public class InfernalMobsServer implements ISidedProxy {

    private ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsServer;

    public InfernalMobsServer() {
        rareMobsServer = new ConcurrentHashMap<>();
    }

    @Override
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs() {
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
