package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_1UP extends MobModifier
{
    private boolean healed;

    @Override
    public String getModName()
    {
        return "1UP";
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (!healed && mob.getHealth() < (getActualMaxHealth(mob)*0.25))
        {
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, getActualMaxHealth(mob));
            mob.worldObj.playSoundAtEntity(mob, "random.levelup", 1.0F, 1.0F);
            healed = true;
        }
        return super.onUpdate(mob);
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
    private static String[] suffix = { "ofRecurrence", "theUndying", "oftwinLives" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "recurring", "undying", "twinlived" };
}
