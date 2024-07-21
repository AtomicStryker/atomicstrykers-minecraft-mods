package atomicstryker.infernalmobs.common;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map.Entry;

public class EntityEventHandler {

    private final HashMap<Tuple<Integer, Integer>, Float> damageMap = new HashMap<>();
    private long nextMapEvaluation;

    /**
     * Links the Forge Event Handler to the registered Entity MobModifier Events
     * (if present) Also keeps track of the anti mobfarm mechanic if enabled
     */
    public EntityEventHandler() {
        nextMapEvaluation = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinLevelEvent event) {
        // make sure we are not catching items or player entities in this
        if (event.getEntity() instanceof LivingEntity && event.getEntity() instanceof Enemy) {
            String savedMods = event.getEntity().getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
            if (!savedMods.isEmpty() && !savedMods.equals(InfernalMobsCore.instance().getNBTMarkerForNonInfernalEntities())) {
                InfernalMobsCore.instance().addEntityModifiersByString((LivingEntity) event.getEntity(), savedMods);
            } else {
                InfernalMobsCore.instance().processEntitySpawn((LivingEntity) event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                if (mod.onDeath()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingSetAttackTarget(LivingChangeTargetEvent event) {
        if (!event.getEntity().level().isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onSetAttackTarget(event.getNewAboutToBeSetTarget());
            }
        }
    }

    /**
     * Hook into EntityLivingHurt. Is always serverside, assured by mc itself
     */
    @SubscribeEvent
    public void onEntityLivingHurt(LivingDamageEvent.Pre event) {
        // dont allow masochism
        if (event.getSource().getDirectEntity() != event.getEntity()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                // note we dont use original damage so mods can chain modify
                event.setNewDamage(mod.onHurt(event.getEntity(), event.getSource(), event.getNewDamage()));
            }

            /*
             * We use the Hook two-sided, both with the Mob as possible target
             * and attacker
             */
            Entity attacker = event.getSource().getDirectEntity();
            if (attacker == null) {
                // if damage does not have a defined source, ignore it
                return;
            }
            if (attacker instanceof LivingEntity) {
                mod = InfernalMobsCore.getMobModifiers((LivingEntity) attacker);
                if (mod != null) {
                    event.setNewDamage(mod.onAttack(event.getEntity(), event.getSource(), event.getNewDamage()));
                }
            }

            if (InfernalMobsCore.instance().config.isAntiMobFarm()) {
                /*
                 * check for an environmental/automated damage type, aka mob farms. FakePlayer is gone so we check for
                 * Player i guess
                 */
                if (event.getSource() == attacker.damageSources().cactus() || event.getSource() == attacker.damageSources().drown() || event.getSource() == attacker.damageSources().fall() || event.getSource() == attacker.damageSources().inWall()
                        || event.getSource() == attacker.damageSources().lava() || event.getSource().getDirectEntity() instanceof Player) {
                    Tuple<Integer, Integer> cpair = new Tuple<>((int) event.getEntity().getX(), (int) event.getEntity().getZ());
                    Float value = damageMap.get(cpair);
                    if (value == null) {
                        for (Entry<Tuple<Integer, Integer>, Float> e : damageMap.entrySet()) {
                            if (Math.abs(e.getKey().getA() - cpair.getA()) < 3) {
                                if (Math.abs(e.getKey().getB() - cpair.getB()) < 3) {
                                    e.setValue(e.getValue() + event.getNewDamage());
                                    break;
                                }
                            }
                        }
                    } else {
                        damageMap.put(cpair, value + event.getNewDamage());
                        GsonConfig.saveConfig(InfernalMobsCore.instance().config, InfernalMobsCore.instance().configFile);
                    }
                }
            }
        }
    }

    /**
     * Hook into LivingKnockBackEvent. Is always serverside, assured by mc itself
     */
    @SubscribeEvent
    public void onEntityLivingKnockback(LivingKnockBackEvent event) {
        MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
        if (mod != null) {
            mod.onKnockBack(event);
        }
    }

    @SubscribeEvent
    public void onEntityLivingFall(LivingFallEvent event) {
        if (!event.getEntity().level().isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                event.setCanceled(mod.onFall(event.getDistance()));
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!event.getEntity().level().isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onJump(event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) event.getEntity();

            // workaround to get save-loaded infernal entities working, init them on their first living tick
            if (event.getEntity().tickCount == 1) {
                String savedMods = event.getEntity().getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
                if (!savedMods.isEmpty() && !savedMods.equals(InfernalMobsCore.instance().getNBTMarkerForNonInfernalEntities())) {
                    InfernalMobsCore.instance().addEntityModifiersByString(livingEntity, savedMods);
                }
            }

            MobModifier mod = InfernalMobsCore.getMobModifiers(livingEntity);
            if (mod != null) {
                mod.onUpdate(livingEntity);
            }

            if (InfernalMobsCore.instance().config.isAntiMobFarm() && System.currentTimeMillis() > nextMapEvaluation) {
                if (!damageMap.isEmpty()) {
                    float maxDamage = 0f;
                    float val;
                    Tuple<Integer, Integer> maxC = null;
                    for (Entry<Tuple<Integer, Integer>, Float> e : damageMap.entrySet()) {
                        val = e.getValue();
                        if (val > maxDamage) {
                            maxC = e.getKey();
                            maxDamage = val;
                        }
                    }

                    if (maxC != null) {
                        System.out.println("Infernal Mobs AntiMobFarm damage check, max detected chunk damage value " + maxDamage + " near coords " + maxC.getA() + ", " + maxC.getB());
                        if (maxDamage > InfernalMobsCore.instance().config.getMobFarmDamageTrigger()) {
                            NeoForge.EVENT_BUS
                                    .post(new MobFarmDetectedEvent(event.getEntity().level().getChunk(maxC.getA(), maxC.getB()), InfernalMobsCore.instance().config.getMobFarmCheckIntervals(), maxDamage));
                        }
                    }
                    damageMap.clear();
                }
                nextMapEvaluation = System.currentTimeMillis() + InfernalMobsCore.instance().config.getMobFarmCheckIntervals();
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingDrops(LivingDropsEvent event) {
        if (!event.getEntity().level().isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onDropItems(event.getEntity(), event.getSource(), event.getDrops(), event.isRecentlyHit());
                InfernalMobsCore.removeEntFromElites(event.getEntity());
            }
        }
    }

    public static class MobFarmDetectedEvent extends ChunkEvent {
        public final long triggeringInterval;
        public final float triggeringDamage;

        public MobFarmDetectedEvent(LevelChunk chunk, long ti, float td) {
            super(chunk);
            triggeringInterval = ti;
            triggeringDamage = td;
        }
    }
}
