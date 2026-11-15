package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
        removeStaleAttackTarget(mob);
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
            if (canMobSeeTarget(mob, lastTarget)) {
                if (lastAir == RESET_AIR_VALUE) {
                    lastAir = lastTarget.getAirSupply();
                } else {
                    lastAir = Math.min(lastAir, lastTarget.getAirSupply());
                }

                if (!(lastTarget instanceof Player && ((Player) lastTarget).getAbilities().invulnerable)) {
                    lastAir--;
                    if (lastAir < -19) {
                        lastAir = 0;
                        lastTarget.hurt(lastTarget.damageSources().drown(), 2.0F);
                    }

                    updateAir();
                }
            }
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (lastTarget != null && source.getEntity() == lastTarget && lastAir != RESET_AIR_VALUE) {
            lastAir += 60;
            if (lastAir > lastTarget.getMaxAirSupply()) {
                lastAir = lastTarget.getMaxAirSupply();
            }
            updateAir();
        }

        return super.onHurt(mob, source, damage);
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
        if (lastAir == RESET_AIR_VALUE) {
            // RESET_AIR_VALUE only marks "not choking" here, it must never become the victim's air:
            // Forge's LivingBreatheEvent drowning check hurts any entity whose air is -20 or lower, even out of water,
            // and air only refills a few points per tick now, so -999 caused delayed drowning damage after a choke ended
            restoreAir(lastTarget);
        } else {
            lastTarget.setAirSupply(lastAir);
        }
        if (lastTarget instanceof ServerPlayer) {
            InfernalMobsCore.instance().getModifiedPlayerTimes().put(lastTarget.getName().getString(), System.currentTimeMillis());
        }
    }

    @Override
    public void resetModifiedVictim(Player victim) {
        restoreAir(victim);
    }

    /**
     * Ends the choke without hurting the victim: out of water air comes back right away (like it did when vanilla
     * refilled it every tick), underwater it only stops being negative so normal drowning carries on from there.
     */
    private void restoreAir(LivingEntity victim) {
        if (victim.isEyeInFluid(FluidTags.WATER)) {
            victim.setAirSupply(Math.max(victim.getAirSupply(), 0));
        } else {
            victim.setAirSupply(victim.getMaxAirSupply());
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
