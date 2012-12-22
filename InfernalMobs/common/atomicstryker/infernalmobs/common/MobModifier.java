package atomicstryker.infernalmobs.common;

import java.util.ArrayList;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public abstract class MobModifier
{
    protected EntityLiving mob;
    protected MobModifier nextMod = null;
    protected String modName;
    private boolean healthHacked = false;
    private int healthHackDelayTicks = 10;
    private long lastExistCheckTime = System.currentTimeMillis();
    private final long existCheckDelay = 5000L;
    private int actualHealth = 100;

    public String getModName()
    {
        return (modName + " " + ((nextMod != null) ? nextMod.getModName() : ""));
    }

    public boolean containsModifierClass(Class checkfor)
    {
        if (checkfor.equals(this.getClass()))
        {
            return true;
        }

        if (nextMod != null)
        {
            return nextMod.containsModifierClass(checkfor);
        }

        return false;
    }

    /**
     * Called when local Spawn Processing is completed or when a client remote-attached Modifiers to a local Entity
     */
    public void onSpawningComplete()
    {
        mob.getEntityData().setString(InfernalMobsCore.getNBTTag(), getModName());
    }

    public boolean onDeath()
    {
        if (nextMod != null)
        {
            return nextMod.onDeath();
        }

        return false;
    }

    public void onDropItems(EntityLiving moddedMob, DamageSource killSource, ArrayList<EntityItem> drops, int lootingLevel, boolean recentlyHit, int specialDropValue)
    {
        if (recentlyHit)
        {
            InfernalMobsCore.instance().dropLootForEnt(moddedMob);
        }
    }

    public void onSetAttackTarget(EntityLiving target)
    {
        if (nextMod != null)
        {
            nextMod.onSetAttackTarget(target);
        }
    }

    /**
     * Modified Mob attacks something
     * @param entity Entity being attacked
     * @param source DamageSource instance doing the attacking
     * @param damage unmitigated damage value
     * @return
     */
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (nextMod != null)
        {
            return nextMod.onAttack(entity, source, damage);
        }

        return damage;
    }

    /**
     * Modified Mob is being hurt
     * @param source Damagesource doing the hurting
     * @param damage unmitigated damage value
     * @return
     */
    public int onHurt(DamageSource source, int damage)
    {
        if (nextMod != null)
        {
            damage = nextMod.onHurt(source, damage);
        }
        
        if (source.getEntity() != null
        && source.getEntity().worldObj.isRemote
        && source.getEntity() instanceof EntityPlayer)
        {
            InfernalMobsCore.instance().sendHealthRequestPacket(mob);
        }

        return damage;
    }

    public boolean onFall(float distance)
    {
        if (nextMod != null)
        {
            return nextMod.onFall(distance);
        }

        return false;
    }

    public void onJump(EntityLiving entityLiving)
    {
        if (nextMod != null)
        {
            nextMod.onJump(entityLiving);
        }
    }

    public boolean onUpdate()
    {
        if (nextMod != null)
        {
            return nextMod.onUpdate();
        }

        if (!mob.worldObj.isRemote)
        {
            if (!healthHacked && --healthHackDelayTicks <= 0)
            {
                InfernalMobsCore.setEntityHealthPastMax(mob, mob.getMaxHealth()*InfernalMobsCore.RARE_MOB_HEALTH_MODIFIER);
                //System.out.println("new entity "+mob+" health hacked to: "+mob.getHealth()+" from max: "+mob.getMaxHealth());
                healthHacked = true;
            }
            InfernalMobsCore.instance().sendHealthPacket(mob, mob.getHealth());
        }

        return false;
    }
    
    public int getActualHealth()
    {
        return actualHealth;
    }
    
    public void setActualHealth(int input)
    {
        actualHealth = input;
    }
    
    public void updateEntityReference(EntityLiving ent)
    {
        mob = ent;
    }

    /**
     * @return Array of classes an EntityLiving must equal, implement or extend in order for this MobModifier to be applied to it
     */
    public Class[] getWhiteListMobClasses()
    {
        return null;
    }

    /**
     * @return Array of classes an EntityLiving cannot equal, implement or extend in order for this MobModifier to be applied to it
     */
    public Class[] getBlackListMobClasses()
    {
        return null;
    }

    /**
     * @return Array of MobModifiers a considered MobModifier should not be mixed with. Both sides need to exclude each other for this to work.
     */
    public Class[] getModsNotToMixWith()
    {
        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof MobModifier
                && ((MobModifier)o).modName.equals(modName));
    }
}
