package atomicstryker.infernalmobs.common;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public abstract class MobModifier
{    
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
     * Display-sized (up to 5) series of Modifier Strings, buffered
     */
    private String[] bufferedNames;
    
    /**
     * buffered maximum health
     */
    private int actualMaxHealth;
    
    private EntityLiving attackTarget;
    
    public MobModifier()
    {
        nextMod = null;
        healthHacked = false;
        actualHealth = 100;
        actualMaxHealth = -1;
    }
    
    /**
     * @return the complete List of linked Modifiers as their Names
     */
    public String getLinkedModName()
    {
        return (modName + " " + ((nextMod != null) ? nextMod.getLinkedModName() : ""));
    }
    
    /**
     * @return Display-sized (up to 5) series of Modifier Strings
     */
    public String[] getDisplayNames()
    {
        if (bufferedNames == null)
        {
            String[] allMods = getLinkedModName().split(" ");
            int index = 0;
            int j = 0;
            bufferedNames = new String[3];
            bufferedNames[index] = "";
            for (String m : allMods)
            {
                bufferedNames[index] = bufferedNames[index] + " " + m;
                j++;
                if (j % 5 == 0 && index+1 < bufferedNames.length)
                {
                    index++;
                    bufferedNames[index] = "";
                }
            }
        }
        return bufferedNames;
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
     * @param entity 
     */
    public void onSpawningComplete(EntityLiving entity)
    {
        entity.getEntityData().setString(InfernalMobsCore.getNBTTag(), getLinkedModName());
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
            InfernalMobsCore.instance().dropLootForEnt(moddedMob, this);
        }
    }

    public void onSetAttackTarget(EntityLiving target)
    {
        if (nextMod != null)
        {
            nextMod.onSetAttackTarget(target);
        }
        else if (target != null)
        {
            attackTarget = target;
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
     * @param mob 
     * @param source Damagesource doing the hurting
     * @param damage unmitigated damage value
     * @return damage to be applied after we processed the value
     */
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
    {
        if (nextMod != null)
        {
            damage = nextMod.onHurt(mob, source, damage);
        }
        else if (source.getEntity() != null)
        {
            if (source.getEntity().worldObj.isRemote
            && source.getEntity() instanceof EntityPlayer)
            {
                InfernalMobsCore.instance().sendHealthRequestPacket(mob);
            }
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
    
    public boolean onUpdate(EntityLiving mob)
    {
        if (nextMod != null)
        {
            return nextMod.onUpdate(mob);
        }
        else
        {
            if (attackTarget == null)
            {
                attackTarget = mob.worldObj.getClosestVulnerablePlayerToEntity(mob, 12f);
            }
            
            if (attackTarget != null)
            {
                if (attackTarget.isDead || attackTarget.getDistanceToEntity(mob) > 15f)
                {
                    attackTarget = null;
                }
            }
        }

        return false;
    }
    
    /**
     * clientside helper method. Due to the health not being networked, we keep track of it
     * internally, here. Also, this is a good spot for the more-than-allowed health hack.
     * @param mob 
     */
    public int getActualHealth(EntityLiving mob)
    {
        if (!healthHacked && !mob.worldObj.isRemote)
        {
            actualHealth = getActualMaxHealth(mob);
            InfernalMobsCore.setEntityHealthPastMax(mob, actualHealth);
            healthHacked = true;
        }
        
        return actualHealth;
    }
    
    /**
     * @param mob 
     * @return buffered modified max health
     */
    public int getActualMaxHealth(EntityLiving mob)
    {
        if (actualMaxHealth < 0)
        {
            actualMaxHealth = mob.getMaxHealth()*getModSize();
        }
        return actualMaxHealth;
    }
    
    /**
     * clientside receiving end of health packets sent from the InfernalMobs server instance
     */
    public void setActualHealth(int input)
    {
        actualHealth = input;
    }
    
    protected EntityLiving getMobTarget()
    {        
        return attackTarget;
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
    
    /**
     * @return size of linked Mod list
     */
    public int getModSize()
    {
        int r = 1;
        MobModifier nextmod = this.nextMod;
        while (nextmod != null)
        {
            r++;
            nextmod = nextmod.nextMod;
        }
        return r;
    }
}
