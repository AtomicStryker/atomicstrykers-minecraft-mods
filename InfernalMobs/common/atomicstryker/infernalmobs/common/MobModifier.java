package atomicstryker.infernalmobs.common;

import java.util.ArrayList;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public abstract class MobModifier
{
    /**
     * modified entity instance
     */
    protected EntityLiving mob;
    
    /**
     * next MobModifier in a linked chain, on the last one this field is null
     */
    protected MobModifier nextMod;
    
    /**
     * name of this particular MobModifier instance
     */
    protected String modName;
    
    /**
     * keeps track of our past-max-bounds health patch
     */
    private boolean healthHacked;
    
    /**
     * clientside health value to be displayed, because health is not networked
     */
    private int actualHealth;
    
    /**
     * tracks the entity ai type the mod is attached to. 0 = undetermined, 1 = old ai getAttackTarget, 2 = new ai getAITarget
     */
    private int aiStatus;
    
    public MobModifier()
    {
        nextMod = null;
        healthHacked = false;
        actualHealth = 100;
        aiStatus = 0;
    }
    
    /**
     * @return the complete List of linked Modifiers as their Names
     */
    public String getLinkedModName()
    {
        return (modName + " " + ((nextMod != null) ? nextMod.getLinkedModName() : ""));
    }
    
    /**
     * Helper to avoid adding the same mod twice
     */
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
        mob.getEntityData().setString(InfernalMobsCore.getNBTTag(), getLinkedModName());
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
     * @return damage to be applied after we processed the value
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
     * @return damage to be applied after we processed the value
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

        return false;
    }
    
    /**
     * clientside helper method. Due to the health not being networked, we keep track of it
     * internally, here. Also, this is a good spot for the more-than-allowed health hack.
     */
    public int getActualHealth()
    {
        if (!healthHacked && !mob.worldObj.isRemote)
        {
            actualHealth = mob.getMaxHealth()*InfernalMobsCore.RARE_MOB_HEALTH_MODIFIER;
            InfernalMobsCore.setEntityHealthPastMax(mob, actualHealth);
            healthHacked = true;
        }
        
        return actualHealth;
    }
    
    /**
     * clientside receiving end of health packets sent from the InfernalMobs server instance
     */
    public void setActualHealth(int input)
    {
        actualHealth = input;
    }
    
    public void updateEntityReference(EntityLiving ent)
    {
        mob = ent;
    }
    
    /**
     * wrapper helper for the old / new ai systems now both present in mc and mods
     * buffers the first result so there wont have to be countless re-checks
     */
    protected EntityLiving getMobTarget()
    {
        if (aiStatus == 0)
        {
            if (mob.getAITarget() != null)
            {
                aiStatus = 2;
            }
            else if (mob.getAttackTarget() != null)
            {
                aiStatus = 1;
            }
        }
        
        switch (aiStatus)
        {
            case 1:
            {
                return mob.getAttackTarget();
            }
            case 2:
            {
                return mob.getAITarget();
            }
        }
        
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
