package atomicstryker.infernalmobs.common;

import net.minecraft.entity.EntityLivingBase;

import java.util.concurrent.ConcurrentHashMap;

public class InfernalMobsServer {

    private ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsServer;

    public InfernalMobsServer() {
        rareMobsServer = new ConcurrentHashMap<>();
    }
}
