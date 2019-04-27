package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class MM_Exhaust extends MobModifier {

    public MM_Exhaust() {
        super();
    }

    public MM_Exhaust(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Exhaust";
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && (source.getTrueSource() instanceof EntityPlayer)) {
            ((EntityPlayer) source.getTrueSource()).addExhaustion(1F);
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null
                && entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).addExhaustion(1F);
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = {"ofFatigue", "theDrainer"};

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = {"exhausting", "draining"};

}
