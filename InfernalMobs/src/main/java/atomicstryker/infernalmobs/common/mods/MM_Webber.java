package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MM_Webber extends MobModifier {

    private final static long coolDown = 15000L;
    private static Class<?>[] modBans = {MM_Gravity.class, MM_Blastoff.class};
    private static String[] suffix = {"ofTraps", "theMutated", "theSpider"};
    private static String[] prefix = {"ensnaring", "webbing"};
    private long lastAbilityUse = 0L;

    public MM_Webber() {
        super();
    }

    public MM_Webber(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Webber";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()
                && getMobTarget() instanceof PlayerEntity) {
            tryAbility(mob, getMobTarget());
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && source.getTrueSource() instanceof LivingEntity) {
            tryAbility(mob, (LivingEntity) source.getTrueSource());
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || !mob.canEntityBeSeen(target)) {
            return;
        }

        int x = MathHelper.floor(target.getPosX());
        int y = MathHelper.floor(target.getPosY());
        int z = MathHelper.floor(target.getPosZ());

        long time = System.currentTimeMillis();
        if (time > lastAbilityUse + coolDown) {
            int offset;
            if (target.world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.AIR) {
                offset = -1;
            } else if (target.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR) {
                offset = 0;
            } else {
                return;
            }

            lastAbilityUse = time;
            target.world.setBlockState(new BlockPos(x, y + offset, z), Blocks.COBWEB.getDefaultState());
            mob.world.playSound(null, mob.getPosition(), SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
        }
    }

    @Override
    public Class<?>[] getModsNotToMixWith() {
        return modBans;
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
