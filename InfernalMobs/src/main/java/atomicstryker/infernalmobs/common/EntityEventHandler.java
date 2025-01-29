package atomicstryker.infernalmobs.common;

import atomicstryker.infernalmobs.common.compat.ChampionsCompat;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        var entity = event.getEntity();
        var level = event.getLevel();
        // check level is server level first
        if (!level.isClientSide()) {
            // champions mob compat start
            if (InfernalMobsCore.instance().isChampionLoaded() && ChampionsCompat.isChampionEntity(entity)) {
                InfernalMobsCore.getLogger().debug("Stop spawning {} infernal mob at {} uuid: {}, Because entity already handled by champions mod.", entity.getName().getString(), entity.blockPosition(), entity.getUUID());
                return;
            }
            // champions mob compat end
            if (entity instanceof LivingEntity livingEntity && entity instanceof Enemy) {
                String savedMods = entity.getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
                if (!savedMods.isEmpty() && !savedMods.equals(InfernalMobsCore.instance().getNBTMarkerForNonInfernalEntities())) {
                    InfernalMobsCore.instance().addEntityModifiersByString(livingEntity, savedMods);
                } else {
                    InfernalMobsCore.instance().processEntitySpawn((LivingEntity) event.getEntity());
                }
            }
        }
    }


    @SubscribeEvent
    public void onEntityLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(entity);
            if (mod != null) {
                if (mod.onDeath()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingSetAttackTarget(LivingChangeTargetEvent event) {
        var entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onSetAttackTarget(event.getNewTarget());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingAttacked(LivingAttackEvent event) {
        /* fires both client and server before hurt, but we dont need this */
    }

    /**
     * Hook into EntityLivingHurt. Is always serverside, assured by mc itself
     */
    @SubscribeEvent
    public void onEntityLivingHurt(LivingHurtEvent event) {
        // dont allow masochism
        var entity = event.getEntity();
        var damageSource = event.getSource();
        var attacker = event.getSource().getDirectEntity();

        /*
         * We use the Hook two-sided, both with the Mob as possible target
         * and attacker
         */
        if (attacker == null) {
            // if damage does not have a defined source, ignore it
            return;
        }

        if (attacker != entity) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(entity);
            if (mod != null) {
                event.setAmount(mod.onHurt(event.getEntity(), event.getSource(), event.getAmount()));
            }


            if (attacker instanceof LivingEntity livingEntity) {
                mod = InfernalMobsCore.getMobModifiers(livingEntity);
                if (mod != null) {
                    event.setAmount(mod.onAttack(event.getEntity(), event.getSource(), event.getAmount()));
                }
            }

            if (InfernalMobsCore.instance().config.isAntiMobFarm()) {
                /*
                 * check for an environmental/automated damage type, aka mob
                 * farms
                 */
                if (damageSource == attacker.damageSources().cactus() || damageSource == attacker.damageSources().drown() || damageSource == attacker.damageSources().fall() || damageSource == attacker.damageSources().inWall()
                        || damageSource == attacker.damageSources().lava() || damageSource.getDirectEntity() instanceof FakePlayer) {
                    Tuple<Integer, Integer> cpair = new Tuple<>((int) event.getEntity().getX(), (int) event.getEntity().getZ());
                    Float value = damageMap.get(cpair);
                    if (value == null) {
                        for (Entry<Tuple<Integer, Integer>, Float> e : damageMap.entrySet()) {
                            if (Math.abs(e.getKey().getA() - cpair.getA()) < 3) {
                                if (Math.abs(e.getKey().getB() - cpair.getB()) < 3) {
                                    e.setValue(e.getValue() + event.getAmount());
                                    break;
                                }
                            }
                        }
                    } else {
                        damageMap.put(cpair, value + event.getAmount());
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
        var entity = event.getEntity();
        var distance = event.getDistance();
        if (!entity.level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(entity);
            if (mod != null) {
                event.setCanceled(mod.onFall(distance));
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingJump(LivingEvent.LivingJumpEvent event) {
        var entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(entity);
            if (mod != null) {
                mod.onJump(event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        var level = entity.level();
        if (!level.isClientSide()) {

            // workaround to get save-loaded infernal entities working, init them on their first living tick
            if (entity.tickCount == 1) {
                String savedMods = entity.getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
                if (!savedMods.isEmpty() && !savedMods.equals(InfernalMobsCore.instance().getNBTMarkerForNonInfernalEntities())) {
                    InfernalMobsCore.instance().addEntityModifiersByString(event.getEntity(), savedMods);
                }
            }

            MobModifier mod = InfernalMobsCore.getMobModifiers(entity);
            if (mod != null) {
                mod.onUpdate(entity);
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
                        InfernalMobsCore.getLogger().info("Infernal Mobs AntiMobFarm damage check, max detected chunk damage value {} near coords {}, {}", maxDamage, maxC.getA(), maxC.getB());
                        if (maxDamage > InfernalMobsCore.instance().config.getMobFarmDamageTrigger()) {
                            MinecraftForge.EVENT_BUS
                                    .post(new MobFarmDetectedEvent(level.getChunk(maxC.getA(), maxC.getB()), InfernalMobsCore.instance().config.getMobFarmCheckIntervals(), maxDamage));
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
        var entity = event.getEntity();

        if (!entity.level().isClientSide()) {
            var damageSource = event.getSource();
            var drops = event.getDrops();
            var lootingLevel = event.getLootingLevel();
            var isRecentlyHit = event.isRecentlyHit();
            MobModifier mod = InfernalMobsCore.getMobModifiers(entity);
            if (mod != null) {
                mod.onDropItems(entity, damageSource, drops, lootingLevel, isRecentlyHit, lootingLevel);
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
