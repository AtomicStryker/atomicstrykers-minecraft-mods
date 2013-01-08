package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Berserk extends MobModifier
{
    public MM_Berserk(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Berserk";
    }
    
    public MM_Berserk(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Berserk";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null)
        {
            mob.attackEntityFrom(DamageSource.generic, damage);
            damage *= 2;
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    public Class[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class[] disallowed = { EntityCreeper.class };
}
