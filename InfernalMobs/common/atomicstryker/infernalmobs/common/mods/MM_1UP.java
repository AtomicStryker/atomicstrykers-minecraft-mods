package atomicstryker.infernalmobs.common.mods;

import java.lang.reflect.Field;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_1UP extends MobModifier
{
    private boolean healed;
    
    public MM_1UP(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "1UP";
        healed = false;
    }
    
    public MM_1UP(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "1UP";
        this.nextMod = prevMod;
        healed = false;
    }
    
    @Override
    public boolean onUpdate()
    {
        if (!healed && mob.getHealth() < (mob.getMaxHealth()*InfernalMobsCore.RARE_MOB_HEALTH_MODIFIER)/4)
        {
            InfernalMobsCore.setEntityHealthPastMax(mob, mob.getMaxHealth()*InfernalMobsCore.RARE_MOB_HEALTH_MODIFIER);
            mob.worldObj.playSoundAtEntity(mob, "random.levelup", 1.0F, 1.0F);
            healed = true;
        }
        return super.onUpdate();
    }
    
    @Override
    public Class[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class[] disallowed = { EntityCreeper.class };
}
