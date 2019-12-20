package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;


public class MM_Storm extends MobModifier {

    private final static long coolDown = 15000L;
    private final static float MIN_DISTANCE = 3F;
    private static String[] suffix = {"ofLightning", "theRaiden"};
    private static String[] prefix = {"striking", "thundering", "electrified"};
    private long nextAbilityUse = 0L;

    public MM_Storm() {
        super();
    }

    public MM_Storm(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Storm";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()
                && getMobTarget() instanceof PlayerEntity) {
            tryAbility(mob, getMobTarget());
        }

        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || target.getRidingEntity() != null || !mob.canEntityBeSeen(target)) {
            return;
        }

        long time = System.currentTimeMillis();
        if (time > nextAbilityUse
                && mob.getDistance(target) > MIN_DISTANCE
                && target.world.canBlockSeeSky(new BlockPos(MathHelper.floor(target.func_226277_ct_()), MathHelper.floor(target.func_226278_cu_()), MathHelper.floor(target.func_226281_cx_())))) {
            nextAbilityUse = time + coolDown;
            ((ServerWorld) mob.world).addLightningBolt(new LightningBoltEntity(mob.world, target.func_226277_ct_(), target.func_226278_cu_() - 1, target.func_226281_cx_(), false));
        }
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

}
