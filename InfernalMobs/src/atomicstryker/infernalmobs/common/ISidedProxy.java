package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;

public interface ISidedProxy
{
    public void preInit();
    
    public void load();
    
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs();
}
