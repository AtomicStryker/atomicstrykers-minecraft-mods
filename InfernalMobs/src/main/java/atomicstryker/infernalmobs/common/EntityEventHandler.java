package atomicstryker.infernalmobs.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

public class EntityEventHandler
{
    /**
     * Links the Forge Event Handler to the registered Entity MobModifier Events (if present)
     */

    @SubscribeEvent
    public void onEntityJoinedWorld (EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityLivingBase)
        {
            String savedMods = event.entity.getEntityData().getString(InfernalMobsCore.instance().getNBTTag());
            if (!savedMods.equals(""))
            {
                InfernalMobsCore.instance().addEntityModifiersByString((EntityLivingBase) event.entity, savedMods);
            }
            else
            {
                InfernalMobsCore.instance().processEntitySpawn((EntityLivingBase) event.entity);
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingDeath(LivingDeathEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
            if (mod != null)
            {
                if(mod.onDeath())
                {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingSetAttackTarget(LivingSetAttackTargetEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
            if (mod != null)
            {
                mod.onSetAttackTarget(event.target);
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingAttacked(LivingAttackEvent event)
    {
        /* fires both client and server before hurt, but we dont need this */
    }
    
    @SubscribeEvent
    public void onEntityLivingHurt(LivingHurtEvent event)
    {
        // dont allow masochism
        if (event.source.getEntity() != event.entityLiving)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
            if (mod != null)
            {
                event.ammount = mod.onHurt(event.entityLiving, event.source, event.ammount);
            }

            /*
             * We use the Hook two-sided, both with the Mob as possible target and attacker
             */
            Entity attacker = event.source.getEntity();
            if (attacker != null
            && attacker instanceof EntityLivingBase)
            {
                mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) attacker);
                if (mod != null)
                {
                    event.ammount = mod.onAttack(event.entityLiving, event.source, event.ammount);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingFall(LivingFallEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
            if (mod != null)
            {
                event.setCanceled(mod.onFall(event.distance));
            }
        }
    }
    
    @SubscribeEvent
    public void onEntityLivingJump(LivingEvent.LivingJumpEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
            if (mod != null)
            {
                mod.onJump(event.entityLiving);
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event)
    {
        if (!event.entityLiving.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
            if (mod != null)
            {
                mod.onUpdate(event.entityLiving);
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingDrops(LivingDropsEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
            if (mod != null)
            {
                mod.onDropItems(event.entityLiving, event.source, event.drops, event.lootingLevel, event.recentlyHit, event.specialDropValue);
                InfernalMobsCore.removeEntFromElites(event.entityLiving);
            }
        }
    }
}
