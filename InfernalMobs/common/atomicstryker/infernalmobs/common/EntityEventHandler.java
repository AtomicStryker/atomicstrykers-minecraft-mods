package atomicstryker.infernalmobs.common;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraftforge.event.ForgeSubscribe;
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

    @ForgeSubscribe
    public void onEntityJoinedWorld (EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityLiving)
        {
            String savedMods = event.entity.getEntityData().getString(InfernalMobsCore.getNBTTag());
            if (!savedMods.equals(""))
            {
                InfernalMobsCore.addEntityModifiersByString((EntityLiving) event.entity, savedMods);
            }
            else
            {
                InfernalMobsCore.processEntitySpawn((EntityLiving) event.entity);
            }
        }
    }

    @ForgeSubscribe
    public void onEntityLivingDeath(LivingDeathEvent event)
    {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
        if (mod != null)
        {
            if (!event.entityLiving.worldObj.isRemote)
            {
                mod.updateEntityReference(event.entityLiving);
            }
            
            if(mod.onDeath())
            {
                event.setCanceled(true);
            }
        }
    }

    @ForgeSubscribe
    public void onEntityLivingSetAttackTarget(LivingSetAttackTargetEvent event)
    {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
        if (mod != null)
        {
            mod.updateEntityReference(event.entityLiving);
            mod.onSetAttackTarget(event.target);
        }
    }

    @ForgeSubscribe
    public void onEntityLivingAttacked(LivingAttackEvent event)
    {
        /* fires both client and server before hurt, but we dont need this */
    }
    
    @ForgeSubscribe
    public void onEntityLivingHurt(LivingHurtEvent event)
    {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
        if (mod != null)
        {
            mod.updateEntityReference(event.entityLiving);
            event.ammount = mod.onHurt(event.source, event.ammount);
        }
        
        /*
         * We use the Hook two-sided, both with the Mob as possible target and attacker
         */
        Entity attacker = event.source.getEntity();
        if (attacker != null
        && attacker instanceof EntityLiving)
        {
            mod = InfernalMobsCore.getMobModifiers((EntityLiving) attacker);
            if (mod != null)
            {
                mod.updateEntityReference((EntityLiving) attacker);
            	event.ammount = mod.onAttack(event.entityLiving, event.source, event.ammount);
            }
        }
    }

    @ForgeSubscribe
    public void onEntityLivingFall(LivingFallEvent event)
    {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
        if (mod != null)
        {
            mod.updateEntityReference(event.entityLiving);
            event.setCanceled(mod.onFall(event.distance));
        }
    }
    
    @ForgeSubscribe
    public void onEntityLivingJump(LivingEvent.LivingJumpEvent event)
    {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
        if (mod != null)
        {
            mod.updateEntityReference(event.entityLiving);
            mod.onJump(event.entityLiving);
        }
    }

    @ForgeSubscribe
    public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event)
    {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
        if (mod != null)
        {
            mod.updateEntityReference(event.entityLiving);
            mod.onUpdate();
        }
    }

    @ForgeSubscribe
    public void onEntityLivingDrops(LivingDropsEvent event)
    {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving);
        if (mod != null)
        {
            mod.updateEntityReference(event.entityLiving);
            mod.onDropItems(event.entityLiving, event.source, event.drops, event.lootingLevel, event.recentlyHit, event.specialDropValue);
            InfernalMobsCore.removeEntFromElites(event.entityLiving);
        }
    }
}
