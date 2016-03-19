package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class MM_1UP extends MobModifier
{
    private boolean healed;
    
    public MM_1UP()
    {
        super();
    }
    
    public MM_1UP(MobModifier next)
    {
        super(next);
    }
    
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
            mob.worldObj.playSound(null, new BlockPos(mob), SoundEvents.entity_player_levelup, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
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
