package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;

public class MM_Berserk extends MobModifier
{

    public MM_Berserk()
    {
        super();
    }

    public MM_Berserk(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Berserk";
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        if (entity != null)
        {
            source.getEntity().attackEntityFrom(DamageSource.GENERIC, damage);
            damage *= 2;
            damage = InfernalMobsCore.instance().getLimitedDamage(damage);
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }

    private static Class<?>[] disallowed = { EntityCreeper.class };

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }

    private static String[] suffix = { "ofRecklessness", "theRaging", "ofSmashing" };

    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }

    private static String[] prefix = { "reckless", "raging", "smashing" };

}
