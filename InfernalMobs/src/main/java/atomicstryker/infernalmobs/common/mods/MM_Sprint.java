package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;

public class MM_Sprint extends MobModifier {

    private final static long coolDown = 5000L;
    private static String[] suffix = {"ofBolting", "theSwiftOne", "ofbeinginyourFace"};
    private static String[] prefix = {"sprinting", "swift", "charging"};
    private long nextAbilityUse = 0L;
    private boolean sprinting;
    private double modMotionX;
    private double modMotionZ;

    public MM_Sprint() {
        super();
    }

    public MM_Sprint(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Sprint";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse) {
                nextAbilityUse = time + coolDown;
                sprinting = !sprinting;
            }

            if (sprinting) {
                doSprint(mob);
            }
        }

        return super.onUpdate(mob);
    }

    private void doSprint(LivingEntity mob) {
        float rotationMovement = (float) ((Math.atan2(mob.getDeltaMovement().x, mob.getDeltaMovement().z) * 180D) / 3.1415D);
        float rotationLook = mob.getYRot();

        // god fucking dammit notch
        if (rotationLook > 360F) {
            rotationLook -= (rotationLook % 360F) * 360F;
        } else if (rotationLook < 0F) {
            rotationLook += ((rotationLook * -1) % 360F) * 360F;
        }

        // god fucking dammit, NOTCH
        if (Math.abs(rotationMovement + rotationLook) > 10F) {
            rotationLook -= 360F;
        }

        double entspeed = GetAbsSpeed(mob);

        // unfuck velocity lock
        if (Math.abs(rotationMovement + rotationLook) > 10F) {
            modMotionX = mob.getDeltaMovement().x;
            modMotionZ = mob.getDeltaMovement().z;
        }

        if (entspeed < 0.3D) {
            if (GetAbsModSpeed() > 0.6D || !(mob.onGround())) {
                modMotionX /= 1.55;
                modMotionZ /= 1.55;
            }

            modMotionX *= 1.5;
            modMotionZ *= 1.5;
            mob.setDeltaMovement(modMotionX, mob.getDeltaMovement().y, modMotionZ);
        }
    }

    private double GetAbsSpeed(LivingEntity ent) {
        return Math.sqrt(ent.getDeltaMovement().x * ent.getDeltaMovement().x + ent.getDeltaMovement().z * ent.getDeltaMovement().z);
    }

    private double GetAbsModSpeed() {
        return Math.sqrt(modMotionX * modMotionX + modMotionZ * modMotionZ);
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
