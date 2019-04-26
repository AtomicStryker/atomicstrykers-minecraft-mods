package atomicstryker.infernalmobs.common;

import net.minecraft.entity.EntityLivingBase;

import java.util.concurrent.ConcurrentHashMap;

public interface ISidedProxy {
    void preInit();

    void load();

    ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs();

    void onHealthPacketForClient(int entID, float health, float maxhealth);

    void onKnockBackPacket(float xv, float zv);

    void onMobModsPacketToClient(String stringData, int entID);

    void onVelocityPacket(float xv, float yv, float zv);

    void onAirPacket(int air);
}
