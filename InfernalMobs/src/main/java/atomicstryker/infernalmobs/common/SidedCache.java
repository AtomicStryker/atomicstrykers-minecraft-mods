package atomicstryker.infernalmobs.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SidedCache {

    private static final ConcurrentHashMap<LivingEntity, MobModifier> rareMobsClient = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<LivingEntity, MobModifier> rareMobsServer = new ConcurrentHashMap<>();

    public static Map<LivingEntity, MobModifier> getInfernalMobs(Level world) {
        return world.isClientSide() ? rareMobsClient : rareMobsServer;
    }
}
