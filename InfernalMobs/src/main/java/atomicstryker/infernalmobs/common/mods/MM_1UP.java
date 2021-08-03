package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

public class MM_1UP extends MobModifier {
    private static Class<?>[] disallowed = {Creeper.class};
    private static String[] suffix = {"ofRecurrence", "theUndying", "oftwinLives"};
    private static String[] prefix = {"recurring", "undying", "twinlived"};
    private boolean healed;

    public MM_1UP() {
        super();
    }

    public MM_1UP(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "1UP";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (!healed && !mob.level.isClientSide && mob.getHealth() < (getActualMaxHealth(mob) * 0.25)) {
            mob.setHealth(getActualHealth(mob));
            mob.level.playSound(null, mob.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.HOSTILE, 1.0F + mob.getRandom().nextFloat(), mob.getRandom().nextFloat() * 0.7F + 0.3F);
            healed = true;
        }
        return super.onUpdate(mob);
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
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
