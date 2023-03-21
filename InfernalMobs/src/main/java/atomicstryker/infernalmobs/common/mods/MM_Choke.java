package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.server.level.ServerPlayer;
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
        if (lastTarget != null && source.getDirectEntity() == lastTarget && lastAir != RESET_AIR_VALUE) {
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
        lastTarget.setAirSupply(lastAir);
        if (lastTarget instanceof ServerPlayer) {
            InfernalMobsCore.instance().sendAirPacket((ServerPlayer) lastTarget, lastAir);
            InfernalMobsCore.instance().getModifiedPlayerTimes().put(lastTarget.getName().getString(), System.currentTimeMillis());
        }
    }

    @Override
    public void resetModifiedVictim(Player victim) {
        victim.setAirSupply(RESET_AIR_VALUE);
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
