package atomicstryker.infernalmobs.common;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SidedCache {

    private static final ConcurrentHashMap<LivingEntity, MobModifier> rareMobsClient = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<LivingEntity, MobModifier> rareMobsServer = new ConcurrentHashMap<>();

    public static Map<LivingEntity, MobModifier> getInfernalMobs(World world) {
        return world.isRemote() ? rareMobsClient : rareMobsServer;
    }
}
