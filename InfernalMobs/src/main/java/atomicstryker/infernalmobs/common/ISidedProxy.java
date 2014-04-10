package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;

public interface ISidedProxy
{
    public void preInit();
    
    public void load();
    
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs();
    
    public void onHealthPacket(String stringData, int entID, float health, float maxhealth);
    
    public void onKnockBackPacket(float xv, float zv);
    
    public void onMobModsPacket(String stringData, int entID);
    
    public void onVelocityPacket(float xv, float yv, float zv);

    public void onAirPacket(int air);
}
