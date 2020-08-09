package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class MM_1UP extends MobModifier {
    private static Class<?>[] disallowed = {CreeperEntity.class};
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
        if (!healed && mob.getHealth() < (getActualMaxHealth(mob) * 0.25)) {
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, getActualMaxHealth(mob));
            mob.world.playSound(null, mob.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
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
