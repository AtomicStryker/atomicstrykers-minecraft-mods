package atomicstryker.infernalmobs.common;

import java.util.List;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

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
    private float actualHealth;
    
    /**
     * Display-sized (up to 5) series of Modifier Strings, buffered
     */
    private String[] bufferedNames;
    
    /**
     * buffered maximum health
     */
    private float actualMaxHealth;
    
    /**
     * internal mob attack target
     */
    private EntityLivingBase attackTarget;
    
    /**
     * buffered modifier size
     */
    private int bufferedSize;
    
    /**
     * buffered modifier string result
     */
    private String bufferedEntityName;
    
    public MobModifier()
    {
        nextMod = null;
        healthHacked = false;
        actualHealth = 100;
        actualMaxHealth = -1;
        bufferedSize = 0;
    }
    
    /**
     * @return the complete List of linked Modifiers as their Names
     */
    public String getLinkedModName()
    {
        return (StatCollector.translateToLocal("translation.infernalmobs:mod."+modName) + " " + ((nextMod != null) ? nextMod.getLinkedModName() : ""));
    }
    
    /**
     * @return same as above, but without using the translation system
     */
    public String getLinkedModNameUntranslated()
    {
        return modName + " " + ((nextMod != null) ? nextMod.getLinkedModNameUntranslated() : "");
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
    public boolean containsModifierClass(Class<?> checkfor)
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
    public void onSpawningComplete(EntityLivingBase entity)
    {
        String oldTag = entity.getEntityData().getString(InfernalMobsCore.instance().getNBTTag());
        if (oldTag.equals(""))
        {
            entity.getEntityData().setString(InfernalMobsCore.instance().getNBTTag(), getLinkedModNameUntranslated());
        }
        else if (!oldTag.equals(getLinkedModNameUntranslated()))
        {
            System.out.printf("Infernal Mobs tag mismatch!! Was [%s], now trying to set [%s] \n", oldTag, getLinkedModNameUntranslated());
        }
    }

    /**
     * Passes the death event to the modifier list
     * @return true if death should be aborted
     */
    public boolean onDeath()
    {
        attackTarget = null;
        if (nextMod != null)
        {
            return nextMod.onDeath();
        }

        return false;
    }

    /**
     * Passes the loot drop event to the modifier list
     */
    public void onDropItems(EntityLivingBase moddedMob, DamageSource killSource, List<EntityItem> drops, int lootingLevel, boolean recentlyHit, int specialDropValue)
    {
        if (recentlyHit)
        {
            InfernalMobsCore.instance().dropLootForEnt(moddedMob, this);
        }
    }

    /**
     * passes the setAttackTarget event to the modifier list
     * @param target being passed from the event
     */
    public void onSetAttackTarget(EntityLivingBase target)
    {
        attackTarget = target;
        if (nextMod != null)
        {
            nextMod.onSetAttackTarget(target);
        }
    }

    /**
     * Modified Mob attacks something
     * @param entity Entity being attacked
     * @param source DamageSource instance doing the attacking
     * @param amount unmitigated damage value
     * @return damage to be applied after we processed the value
     */
    public float onAttack(EntityLivingBase entity, DamageSource source, float amount)
    {
        if (nextMod != null)
        {
            return nextMod.onAttack(entity, source, amount);
        }
        
        return amount;
    }

    /**
     * Modified Mob is being hurt
     * @param mob 
     * @param source Damagesource doing the hurting
     * @param amount unmitigated damage value
     * @return damage to be applied after we processed the value
     */
    public float onHurt(EntityLivingBase mob, DamageSource source, float amount)
    {
        if (nextMod != null)
        {
            amount = nextMod.onHurt(mob, source, amount);
        }
        else if (source.getEntity() != null)
        {
            if (source.getEntity().worldObj.isRemote
            && source.getEntity() instanceof EntityPlayer)
            {
                InfernalMobsCore.instance().sendHealthRequestPacket(mob);
            }
        }

        return amount;
    }
    
    /**
     * passes the fall event to the modifier list
     */
    public boolean onFall(float distance)
    {
        if (nextMod != null)
        {
            return nextMod.onFall(distance);
        }

        return false;
    }
    
    /**
     * passes the jump event to the modifier list
     */
    public void onJump(EntityLivingBase entityLiving)
    {
        if (nextMod != null)
        {
            nextMod.onJump(entityLiving);
        }
    }
    
    /**
     * passes the update event to the modifier list
     * the return value is currently unused
     */
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (nextMod != null)
        {
            return nextMod.onUpdate(mob);
        }
        else
        {
            if (attackTarget == null)
            {
                attackTarget = mob.worldObj.getClosestPlayerToEntity(mob, 7.5f);
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
    public float getActualHealth(EntityLivingBase mob)
    {
        if (!mob.worldObj.isRemote)
        {
            increaseHealthForMob(mob, getActualMaxHealth(mob));
        }
        
        return actualHealth;
    }
    
    /**
     * Prevents exponential health increase from re-loading the same infernal mob again and again
     */
    public void setHealthAlreadyHacked(EntityLivingBase mob)
    {
        if (!mob.worldObj.isRemote)
        {
            actualMaxHealth = getActualMaxHealth(mob);
            healthHacked = true;
        }
    }
    
    private void increaseHealthForMob(EntityLivingBase mob, float baseHealth)
    {
        if (!healthHacked)
        {
            actualMaxHealth = getActualMaxHealth(mob);
            actualHealth = actualMaxHealth;
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, actualHealth);
            healthHacked = true;
        }
    }
    
    /**
     * @param mob 
     * @return buffered modified max health
     */
    public float getActualMaxHealth(EntityLivingBase mob)
    {
        if (actualMaxHealth < 0)
        {
            actualMaxHealth = (float) (InfernalMobsCore.instance().getMobClassMaxHealth(mob) * getModSize() * InfernalMobsCore.instance().getMobModHealthFactor());
        }
        return actualMaxHealth;
    }
    
    /**
     * clientside receiving end of health packets sent from the InfernalMobs server instance
     * @param packetReadout 
     */
    public void setActualHealth(float health, float maxHealth)
    {
        actualHealth = health;
        actualMaxHealth = maxHealth;
    }
    
    protected EntityLivingBase getMobTarget()
    {        
        return attackTarget;
    }

    /**
     * @return Array of classes an EntityLiving cannot equal, implement or extend in order for this MobModifier to be applied to it
     */
    public Class<?>[] getBlackListMobClasses()
    {
        return null;
    }

    /**
     * @return Array of MobModifiers a considered MobModifier should not be mixed with. Both sides need to exclude each other for this to work.
     */
    public Class<?>[] getModsNotToMixWith()
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
        if (bufferedSize == 0)
        {
            bufferedSize = 1;
            MobModifier nextmod = this.nextMod;
            while (nextmod != null)
            {
                bufferedSize++;
                nextmod = nextmod.nextMod;
            }
        }
        
        return bufferedSize;
    }
    
    /**
     * Should be overridden by modifiers to provide possible name prefixes
     */
    protected String[] getModNamePrefix()
    {
        return null;
    }
    
    /**
     * Should be overridden by modifiers to provide possible name suffixes
     */
    protected String[] getModNameSuffix()
    {
        return null;
    }
    
    /**
     * Creates the Entity name the Infernal Mobs GUI displays, and buffers it
     * @param target Entity to create the Name from
     * @return Entity display name such as 'Rare Zombie'
     */
    public String getEntityDisplayName(EntityLivingBase target)
    {
        if (bufferedEntityName == null)
        {
            String buffer = EntityList.getEntityString(target);
            String[] subStrings = buffer.split("\\."); // in case of Package.Class.EntityName derps
            if (subStrings.length > 1)
            {
                buffer = subStrings[subStrings.length-1]; // reduce that to EntityName before proceeding
            }
            buffer = buffer.replaceFirst("Entity", "");
            
            String entLoc = "translation.infernalmobs:entity."+buffer;
            String entTrans = StatCollector.translateToLocal(entLoc);
            if (!entLoc.equals(entTrans))
            {
                buffer = entTrans;
            }
            
            int size = getModSize();
            
            int randomMod = target.getRNG().nextInt(getModSize());
            MobModifier mod = this;
            while (randomMod > 0)
            {
                mod = mod.nextMod;
                randomMod--;
            }
            
            String modprefix = "";
            if (mod.getModNamePrefix() != null)
            {
                modprefix = mod.getModNamePrefix()[target.getRNG().nextInt(mod.getModNamePrefix().length)];
                modprefix = StatCollector.translateToLocal("translation.infernalmobs:prefix."+modprefix);
            }
            
            String prefix = size <= 5 ? EnumChatFormatting.AQUA+StatCollector.translateToLocal("translation.infernalmobs:rareClass") 
                    : size <= 10 ? EnumChatFormatting.YELLOW+StatCollector.translateToLocal("translation.infernalmobs:ultraClass") 
                            : EnumChatFormatting.GOLD+StatCollector.translateToLocal("translation.infernalmobs:infernalClass");

            buffer = prefix+modprefix+buffer;
            
            if (size > 1)
            {
                mod = mod.nextMod != null ? mod.nextMod : this;
                if (mod.getModNameSuffix() != null)
                {
                    String pickedSuffix = mod.getModNameSuffix()[target.getRNG().nextInt(mod.getModNameSuffix().length)];
                    pickedSuffix = StatCollector.translateToLocal("translation.infernalmobs:suffix."+pickedSuffix);
                    buffer = buffer+pickedSuffix;
                }
            }
            
            bufferedEntityName = buffer;
        }
        
        return bufferedEntityName;
    }
    
}
