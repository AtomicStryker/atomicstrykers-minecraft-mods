package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLiving;

public interface ISidedProxy
{
    public void load();
    
    public ConcurrentHashMap<EntityLiving, MobModifier> getRareMobs();
}
