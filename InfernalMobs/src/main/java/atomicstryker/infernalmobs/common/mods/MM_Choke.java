package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;

public class MM_Choke extends MobModifier {

    private static String[] suffix = {"ofBreathlessness", "theAnaerobic", "ofDeprivation"};
    private static String[] prefix = {"Sith Lord", "Dark Lord", "Darth"};
    private final int RESET_AIR_VALUE = -999;
    private LivingEntity lastTarget;
    private int lastAir;

    public MM_Choke() {
        super();
    }

    public MM_Choke(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Choke";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (!hasSteadyTarget()) {
            return super.onUpdate(mob);
        }

        if (getMobTarget() != lastTarget) {
            lastAir = RESET_AIR_VALUE;
            if (lastTarget != null) {
                updateAir();
            }
            lastTarget = getMobTarget();
        }

        if (lastTarget != null) {
            if (mob.canEntityBeSeen(lastTarget)) {
                if (lastAir == RESET_AIR_VALUE) {
                    lastAir = lastTarget.getAir();
                } else {
                    lastAir = Math.min(lastAir, lastTarget.getAir());
                }

                if (!(lastTarget instanceof PlayerEntity && ((PlayerEntity) lastTarget).abilities.disableDamage)) {
                    lastAir--;
                    if (lastAir < -19) {
                        lastAir = 0;
                        lastTarget.attackEntityFrom(DamageSource.DROWN, 2.0F);
                    }

                    updateAir();
                }
            }
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (lastTarget != null && source.getTrueSource() == lastTarget && lastAir != RESET_AIR_VALUE) {
            lastAir += 60;
            updateAir();
        }

        return damage;
    }

    @Override
    public boolean onDeath() {
        lastAir = RESET_AIR_VALUE;
        if (lastTarget != null) {
            updateAir();
            lastTarget = null;
        }
        return false;
    }

    private void updateAir() {
        lastTarget.setAir(lastAir);
        if (lastTarget instanceof ServerPlayerEntity) {
            InfernalMobsCore.instance().sendAirPacket((ServerPlayerEntity) lastTarget, lastAir);
            InfernalMobsCore.instance().getModifiedPlayerTimes().put(lastTarget.getName().getUnformattedComponentText(), System.currentTimeMillis());
        }
    }

    @Override
    public void resetModifiedVictim(PlayerEntity victim) {
        victim.setAir(RESET_AIR_VALUE);
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
