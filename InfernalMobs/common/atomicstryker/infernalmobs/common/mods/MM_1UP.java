package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_1UP extends MobModifier
{
    private boolean healed;
    
    public MM_1UP(EntityLiving mob)
    {
        this.modName = "1UP";
        healed = false;
    }
    
    public MM_1UP(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "1UP";
        this.nextMod = prevMod;
        healed = false;
    }
    
    @Override
    public boolean onUpdate(EntityLiving mob)
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
    public Class[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class[] disallowed = { EntityCreeper.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { " of Recurrence", " the Undying", " of twin Lives" };
}
