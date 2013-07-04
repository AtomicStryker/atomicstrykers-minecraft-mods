package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;

public class InfernalMobsServer implements ISidedProxy
{
    
    private ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsServer;
    
    public InfernalMobsServer()
    {
        rareMobsServer = new ConcurrentHashMap<EntityLivingBase, MobModifier>();
    }

    @Override
    public void load()
    {
        // NOOP
    }

    @Override
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs()
    {
        return rareMobsServer;
    }

}
