package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLiving;

public class InfernalMobsServer implements ISidedProxy
{
    
    private ConcurrentHashMap<EntityLiving, MobModifier> rareMobsServer;
    
    public InfernalMobsServer()
    {
        rareMobsServer = new ConcurrentHashMap();
    }

    @Override
    public void load()
    {
        // NOOP
    }

    @Override
    public ConcurrentHashMap<EntityLiving, MobModifier> getRareMobs()
    {
        return rareMobsServer;
    }

}
