package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MM_Gravity extends MobModifier {

    private final static long coolDown = 5000L;
    private static Class<?>[] modBans = {MM_Webber.class};
    private static String[] suffix = {"ofRepulsion", "theFlipper"};
    private static String[] prefix = {"repulsing", "sproing"};
    private long nextAbilityUse = 0L;

    public MM_Gravity() {
        super();
    }

    public MM_Gravity(MobModifier next) {
        super(next);
    }

    public static void knockBack(EntityLivingBase target, double x, double z) {
        target.isAirBorne = true;
        float normalizedPower = MathHelper.sqrt(x * x + z * z);
        float knockPower = 0.8F;
        target.motionX /= 2.0D;
        target.motionY /= 2.0D;
        target.motionZ /= 2.0D;
        target.motionX -= x / (double) normalizedPower * (double) knockPower;
        target.motionY += (double) knockPower;
        target.motionZ -= z / (double) normalizedPower * (double) knockPower;

        if (target.motionY > 0.4000000059604645D) {
            target.motionY = 0.4000000059604645D;
        }
    }

    @Override
    public String getModName() {
        return "Gravity";
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (hasSteadyTarget() && getMobTarget() instanceof EntityPlayer) {
            tryAbility(mob, getMobTarget());
        }

        return super.onUpdate(mob);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target) {
        if (target == null || !mob.canEntityBeSeen(target)) {
            return;
        }

        long time = System.currentTimeMillis();
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;

            double diffX = target.posX - mob.posX;
            double diffZ;
            for (diffZ = target.posZ - mob.posZ; diffX * diffX + diffZ * diffZ < 1.0E-4D; diffZ = (Math.random() - Math.random()) * 0.01D) {
                diffX = (Math.random() - Math.random()) * 0.01D;
            }

            mob.world.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);

            if (mob.world.isRemote || !(target instanceof EntityPlayerMP)) {
                knockBack(target, diffX, diffZ);
            } else {
                InfernalMobsCore.instance().sendKnockBackPacket((EntityPlayerMP) target, (float) diffX, (float) diffZ);
            }
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
