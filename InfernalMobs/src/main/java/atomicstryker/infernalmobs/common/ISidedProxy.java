package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;

public interface ISidedProxy
{
    void preInit();
    
    void load();
    
    ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs();
    
    void onHealthPacketForClient(String stringData, int entID, float health, float maxhealth);
    
    void onKnockBackPacket(float xv, float zv);
    
    void onMobModsPacketToClient(String stringData, int entID);
    
    void onVelocityPacket(float xv, float yv, float zv);

    void onAirPacket(int air);
}
