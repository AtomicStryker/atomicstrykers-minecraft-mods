package atomicstryker.infernalmobs.common;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

public abstract class MobModifier {
    private final static int TARGETING_TICKS_BEFORE_ATTACK = 30;

    /**
     * next MobModifier in a linked chain, on the last one this field is null
     */
    protected MobModifier nextMod;

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
    private LivingEntity attackTarget;

    /**
     * previous attack target, to compare across ticks and prevent nonagressive
     * mobs popping mod effects
     */
    private LivingEntity previousAttackTarget;

    /**
     * how many ticks the mob is targeting something without interruption
     */
    private int targetingTicksSteadyTarget = 0;

    /**
     * buffered modifier size
     */
    private int bufferedSize;

    /**
     * buffered modifier string result
     */
    private String bufferedEntityName;

    /**
     * Base constructor
     */
    public MobModifier() {
        actualHealth = 100;
        actualMaxHealth = -1;
        bufferedSize = 0;
    }

    /**
     * Constructor for the chained-together case of modifiers
     *
     * @param nxtMod chained MobModifier instance
     */
    protected MobModifier(MobModifier nxtMod) {
        this();
        this.nextMod = nxtMod;
    }

    /**
     * @return internal Modifier id string
     */
    public abstract String getModName();

    /**
     * @return the complete List of linked Modifiers as their Names
     */
    public String getLinkedModName() {
        return (I18n.get("translation.infernalmobs:mod." + getModName()) + " " + ((nextMod != null) ? nextMod.getLinkedModName() : ""));
    }

    /**
     * @return same as above, but without using the translation system
     */
    public String getLinkedModNameUntranslated() {
        return getModName() + " " + ((nextMod != null) ? nextMod.getLinkedModNameUntranslated() : "");
    }

    /**
     * @return Display-sized (up to 5) series of Modifier Strings
     */
    public String[] getDisplayNames() {
        if (bufferedNames == null) {
            String[] allMods = getLinkedModName().split(" ");
            int index = 0;
            int j = 0;
            bufferedNames = new String[3];
            bufferedNames[index] = "";
            for (String m : allMods) {
                bufferedNames[index] = bufferedNames[index] + " " + m;
                j++;
                if (j % 5 == 0 && index + 1 < bufferedNames.length) {
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
    public boolean containsModifierClass(Class<?> checkfor) {
        return checkfor.equals(this.getClass()) || nextMod != null && nextMod.containsModifierClass(checkfor);
    }

    /**
     * Called when local Spawn Processing is completed or when a client
     * remote-attached Modifiers to a local Entity
     *
     * @param entity target mob to attach modifiers to
     */
    public void onSpawningCompleteStoreModsAndBuffHealth(LivingEntity entity) {
        String oldTag = entity.getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
        if (!oldTag.isEmpty() && !oldTag.equals(getLinkedModNameUntranslated())) {
            InfernalMobsCore.LOGGER.info("Infernal Mobs tag mismatch!! Was [{}}], now trying to set [{}}] \n", oldTag, getLinkedModNameUntranslated());
        }
        entity.getPersistentData().putString(InfernalMobsCore.instance().getNBTTag(), getLinkedModNameUntranslated());

        increaseMaxHealthForMobIfNeeded(entity);
    }

    /**
     * Passes the death event to the modifier list
     *
     * @return true if death should be aborted
     */
    public boolean onDeath() {
        attackTarget = null;
        return nextMod != null && nextMod.onDeath();
    }

    /**
     * Passes the loot drop event to the modifier list
     */
    public void onDropItems(LivingEntity moddedMob, DamageSource killSource, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit, int specialDropValue) {
        if (recentlyHit) {
            InfernalMobsCore.instance().dropLootForEnt(moddedMob, this);
        }
    }

    /**
     * passes the setAttackTarget event to the modifier list
     *
     * @param target being passed from the event
     */
    public void onSetAttackTarget(LivingEntity target) {
        previousAttackTarget = attackTarget;
        attackTarget = target;
        if (previousAttackTarget != target) {
            targetingTicksSteadyTarget = 0;
        }
        if (nextMod != null) {
            nextMod.onSetAttackTarget(target);
        }
    }

    /**
     * Modified Mob attacks something
     *
     * @param entity Entity being attacked
     * @param source DamageSource instance doing the attacking
     * @param amount unmitigated damage value
     * @return damage to be applied after we processed the value
     */
    public float onAttack(LivingEntity entity, DamageSource source, float amount) {
        if (nextMod != null) {
            return nextMod.onAttack(entity, source, amount);
        }

        return amount;
    }

    /**
     * Modified Mob is being hurt
     *
     * @param mob    entity instance
     * @param source Damagesource doing the hurting
     * @param amount unmitigated damage value
     * @return damage to be applied after we processed the value
     */
    public float onHurt(LivingEntity mob, DamageSource source, float amount) {
        if (nextMod != null) {
            amount = nextMod.onHurt(mob, source, amount);
        } else if (source.getEntity() != null) {
            if (source.getEntity().level.isClientSide && source.getEntity() instanceof Player) {
                InfernalMobsCore.instance().sendHealthRequestPacket(source.getEntity().getName().getContents(), mob);
            }
        }

        return amount;
    }

    /**
     * passes the fall event to the modifier list
     */
    public boolean onFall(float distance) {
        return nextMod != null && nextMod.onFall(distance);
    }

    /**
     * passes the jump event to the modifier list
     */
    public void onJump(LivingEntity entityLiving) {
        if (nextMod != null) {
            nextMod.onJump(entityLiving);
        }
    }

    /**
     * passes the update event to the modifier list the return value is
     * currently unused
     */
    public boolean onUpdate(LivingEntity mob) {
        if (nextMod != null) {
            return nextMod.onUpdate(mob);
        } else {
            if (attackTarget == null) {
                attackTarget = mob.level.getNearestPlayer(mob, 7.5f);
            }

            if (attackTarget != null) {
                if (!attackTarget.isAlive() || attackTarget.distanceTo(mob) > 15f) {
                    attackTarget = null;
                }
            }
        }

        return false;
    }

    /**
     * used by mods with offensive onUpdate functions - increments the steady
     * target ticker which is wiped when a target is reset and checks the amount
     */
    public boolean hasSteadyTarget() {
        if (attackTarget != null) {
            if (isCreativePlayer(attackTarget)) {
                targetingTicksSteadyTarget = 0;
            } else {
                targetingTicksSteadyTarget++;
            }
            return targetingTicksSteadyTarget > TARGETING_TICKS_BEFORE_ATTACK;
        }
        return false;
    }

    /**
     * players in creative mode are not considered valid targets
     */
    protected boolean isCreativePlayer(Entity entity) {
        if (entity instanceof Player player) {
            if (player.isCreative()) {
                return true;
            }
        }
        return false;
    }

    /**
     * clientside helper method. Due to the health not being networked, we keep
     * track of it internally, here. Also, this is a good spot for the
     * more-than-allowed health hack.
     *
     * @param mob entity instance
     */
    public float getActualHealth(LivingEntity mob) {
        return actualHealth;
    }

    private void increaseMaxHealthForMobIfNeeded(LivingEntity mob) {
        if (mob.level.isClientSide) {
            return;
        }
        float storedMaxHealth = mob.getPersistentData().getFloat("infernalMaxHealth");
        if (storedMaxHealth < 1F) {
            actualMaxHealth = (float) (InfernalMobsCore.instance().getMobClassMaxHealth(mob) * getModSize() * InfernalMobsCore.instance().getMobModHealthFactor());
            actualHealth = actualMaxHealth;
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, actualHealth);
            mob.getPersistentData().putFloat("infernalMaxHealth", actualMaxHealth);
        } else {
            actualMaxHealth = storedMaxHealth;
        }
    }

    /**
     * @param mob entity instance
     * @return buffered modified max health
     */
    public float getActualMaxHealth(LivingEntity mob) {
        if (!mob.level.isClientSide && actualMaxHealth < 1F) {
            actualMaxHealth = mob.getPersistentData().getFloat("infernalMaxHealth");
        }
        return actualMaxHealth;
    }

    /**
     * clientside receiving end of health packets sent from the InfernalMobs
     * server instance
     */
    public void setActualHealth(float health, float maxHealth) {
        actualHealth = health;
        actualMaxHealth = maxHealth;
    }

    protected LivingEntity getMobTarget() {
        return attackTarget;
    }

    /**
     * @return Array of classes an EntityLiving cannot equal, implement or
     * extend in order for this MobModifier to be applied to it
     */
    public Class<?>[] getBlackListMobClasses() {
        return null;
    }

    /**
     * @return Array of MobModifiers a considered MobModifier should not be
     * mixed with. Both sides need to exclude each other for this to
     * work.
     */
    public Class<?>[] getModsNotToMixWith() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MobModifier && ((MobModifier) o).getModName().equals(getModName()));
    }

    /**
     * @return size of linked Mod list
     */
    public int getModSize() {
        if (bufferedSize == 0) {
            bufferedSize = 1;
            MobModifier nextmod = this.nextMod;
            while (nextmod != null) {
                bufferedSize++;
                nextmod = nextmod.nextMod;
            }
        }

        return bufferedSize;
    }

    /**
     * Should be overridden by modifiers to provide possible name prefixes
     */
    protected String[] getModNamePrefix() {
        return null;
    }

    /**
     * Should be overridden by modifiers to provide possible name suffixes
     */
    protected String[] getModNameSuffix() {
        return null;
    }

    /**
     * Creates the Entity name the Infernal Mobs GUI displays, and buffers it
     *
     * @param target Entity to create the Name from
     * @return Entity display name such as 'Rare Zombie'
     */
    public String getEntityDisplayName(LivingEntity target) {
        if (bufferedEntityName == null) {
            String buffer = ForgeRegistries.ENTITIES.getKey(target.getType()).getPath();
            String[] subStrings = buffer.split("\\."); // in case of
            // Package.Class.EntityName
            // derps
            if (subStrings.length > 1) {
                buffer = subStrings[subStrings.length - 1]; // reduce that to
                // EntityName before
                // proceeding
            }
            buffer = buffer.replaceFirst("Entity", "");

            String entLoc = "translation.infernalmobs:entity." + buffer;
            String entTrans = I18n.get(entLoc);
            if (!entLoc.equals(entTrans)) {
                buffer = entTrans;
            }

            // upper case entity if it isnt already
            buffer = buffer.substring(0, 1).toUpperCase() + buffer.substring(1);

            int size = getModSize();

            int randomMod = target.getRandom().nextInt(getModSize());
            MobModifier mod = this;
            while (randomMod > 0) {
                mod = mod.nextMod;
                randomMod--;
            }

            String modprefix = "";
            if (mod.getModNamePrefix() != null) {
                modprefix = mod.getModNamePrefix()[target.getRandom().nextInt(mod.getModNamePrefix().length)];
                modprefix = I18n.get("translation.infernalmobs:prefix." + modprefix);
            }

            String prefix = size <= 5 ? ChatFormatting.AQUA + I18n.get("translation.infernalmobs:rareClass")
                    : size <= 10 ? ChatFormatting.YELLOW + I18n.get("translation.infernalmobs:ultraClass") : ChatFormatting.GOLD + I18n.get("translation.infernalmobs:infernalClass");

            buffer = String.format("%s %s %s", prefix, modprefix, buffer);

            if (size > 1) {
                mod = mod.nextMod != null ? mod.nextMod : this;
                if (mod.getModNameSuffix() != null) {
                    String pickedSuffix = mod.getModNameSuffix()[target.getRandom().nextInt(mod.getModNameSuffix().length)];
                    pickedSuffix = I18n.get("translation.infernalmobs:suffix." + pickedSuffix);
                    buffer = String.format("%s %s", buffer, pickedSuffix);
                }
            }

            bufferedEntityName = buffer;
        }

        return bufferedEntityName;
    }

    /**
     * if a modifier does permanent changes to an entity, they need to register
     * the playername at InfernalMobsCore.instance().getModifiedPlayerTimes()
     * with the current timestamp a timer keeps track of the player not being
     * infernal'd for a while and triggers this modifiers should override this
     * and clean up their changes when necessary
     */
    public void resetModifiedVictim(Player victim) {
        // NOOP by default
    }

    /**
     * helper method to check for target visibility
     */
    protected boolean canMobSeeTarget(LivingEntity mob, LivingEntity target) {
        return target.getVisibilityPercent(mob) >= 0.25D;
    }

}
