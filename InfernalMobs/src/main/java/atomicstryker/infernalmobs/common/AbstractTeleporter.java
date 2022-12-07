package atomicstryker.infernalmobs.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;

public abstract class AbstractTeleporter extends MobModifier {

    public AbstractTeleporter() {
        super();
    }

    public AbstractTeleporter(MobModifier next) {
        super(next);
    }

    protected boolean tryTeleportWithTarget(LivingEntity mob, Entity targetEnt) {
        double startX = mob.getX();
        double startY = mob.getY();
        double startZ = mob.getZ();
        // if distant: teleport close
        if (mob.distanceTo(targetEnt) > 8) {
            for (int attempts = 0; attempts < 5; attempts++) {
                if (tryTeleportTowardsEntity(mob, targetEnt)) {
                    playStartEffects(mob, startX, startY, startZ);
                    return true;
                }
            }
        }
        // fallback: if teleporting towards target doesnt work, try doing it randomly
        for (int attempts = 0; attempts < 5; attempts++) {
            if (teleportRandomly(mob)) {
                playStartEffects(mob, startX, startY, startZ);
                return true;
            }
        }
        return false;
    }

    protected boolean teleportRandomly(LivingEntity mob) {
        if (!mob.level.isClientSide() && mob.isAlive()) {
            double d0 = mob.getX() + (mob.getRandom().nextDouble() - 0.5D) * 64.0D;
            double d1 = mob.getY() + (double) (mob.getRandom().nextInt(64) - 32);
            double d2 = mob.getZ() + (mob.getRandom().nextDouble() - 0.5D) * 64.0D;
            return this.tryTeleportTo(mob, d0, d1, d2);
        } else {
            return false;
        }
    }

    protected boolean tryTeleportTowardsEntity(LivingEntity mob, Entity targetEnt) {
        Vec3 vec3 = new Vec3(mob.getX() - targetEnt.getX(), mob.getY(0.5D) - targetEnt.getEyeY(), mob.getZ() - targetEnt.getZ());
        vec3 = vec3.normalize();
        double d1 = mob.getX() + (mob.getRandom().nextDouble() - 0.5D) * 8.0D - vec3.x * 16.0D;
        double d2 = mob.getY() + (double) (mob.getRandom().nextInt(16) - 8) - vec3.y * 16.0D;
        double d3 = mob.getZ() + (mob.getRandom().nextDouble() - 0.5D) * 8.0D - vec3.z * 16.0D;
        return this.tryTeleportTo(mob, d1, d2, d3);
    }

    protected boolean tryTeleportTo(LivingEntity mob, double x, double y, double z) {
        BlockPos.MutableBlockPos destination = new BlockPos.MutableBlockPos(x, y, z);

        while (destination.getY() > mob.level.getMinBuildHeight() && !mob.level.getBlockState(destination).getMaterial().blocksMotion()) {
            destination.move(Direction.DOWN);
        }

        BlockState destinationFloorState = mob.level.getBlockState(destination);
        boolean blocksMotion = destinationFloorState.getMaterial().blocksMotion();
        boolean isWater = destinationFloorState.getFluidState().is(FluidTags.WATER);
        if (blocksMotion && !isWater) {
            EntityTeleportEvent forgeEvent = getForgeEvent(mob, x, y, z);
            if (forgeEvent.isCanceled()) {
                return false;
            }
            Vec3 vec3 = mob.position();
            boolean teleportResult = mob.randomTeleport(forgeEvent.getTargetX(), forgeEvent.getTargetY(), forgeEvent.getTargetZ(), true);
            if (teleportResult) {
                mob.level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(mob));
                if (!mob.isSilent()) {
                    playDestinationEffects(mob);
                }
            }
            return teleportResult;
        } else {
            return false;
        }
    }

    protected void playStartEffects(LivingEntity mob, double x, double y, double z) {
        // default teleport mimicks Enderman which has no source effect
    }

    protected void playDestinationEffects(LivingEntity mob) {
        mob.level.playSound(null, mob.xo, mob.yo, mob.zo, SoundEvents.ENDERMAN_TELEPORT, mob.getSoundSource(), 1.0F, 1.0F);
        mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    protected EntityTeleportEvent getForgeEvent(LivingEntity mob, double x, double y, double z) {
        return new EntityTeleportEvent(mob, x, y, z);
    }

}
