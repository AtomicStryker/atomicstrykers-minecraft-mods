package atomicstryker.infernalmobs.common;

import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
        if (event.getEntity() instanceof LivingEntity) {
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
        if (!event.getEntity().level.isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                if (mod.onDeath()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!event.getEntity().level.isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onSetAttackTarget(event.getTarget());
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
        if (event.getSource().getEntity() != event.getEntity()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                event.setAmount(mod.onHurt(event.getEntity(), event.getSource(), event.getAmount()));
            }

            /*
             * We use the Hook two-sided, both with the Mob as possible target
             * and attacker
             */
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof LivingEntity) {
                mod = InfernalMobsCore.getMobModifiers((LivingEntity) attacker);
                if (mod != null) {
                    event.setAmount(mod.onAttack(event.getEntity(), event.getSource(), event.getAmount()));
                }
            }

            if (InfernalMobsCore.instance().config.isAntiMobFarm()) {
                /*
                 * check for an environmental/automated damage type, aka mob
                 * farms
                 */
                if (event.getSource() == DamageSource.CACTUS || event.getSource() == DamageSource.DROWN || event.getSource() == DamageSource.FALL || event.getSource() == DamageSource.IN_WALL
                        || event.getSource() == DamageSource.LAVA || event.getSource().getEntity() instanceof FakePlayer) {
                    Tuple<Integer, Integer> cpair = new Tuple<Integer, Integer>((int) event.getEntity().getX(), (int) event.getEntity().getZ());
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

    @SubscribeEvent
    public void onEntityLivingFall(LivingFallEvent event) {
        if (!event.getEntity().level.isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                event.setCanceled(mod.onFall(event.getDistance()));
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!event.getEntity().level.isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onJump(event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level.isClientSide) {

            // workaround to get save-loaded infernal entities working, init them on their first living tick
            if (event.getEntity().tickCount == 1) {
                String savedMods = event.getEntity().getPersistentData().getString(InfernalMobsCore.instance().getNBTTag());
                if (!savedMods.isEmpty() && !savedMods.equals(InfernalMobsCore.instance().getNBTMarkerForNonInfernalEntities())) {
                    InfernalMobsCore.instance().addEntityModifiersByString(event.getEntity(), savedMods);
                }
            }

            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onUpdate(event.getEntity());
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
                            MinecraftForge.EVENT_BUS
                                    .post(new MobFarmDetectedEvent(event.getEntity().level.getChunk(maxC.getA(), maxC.getB()), InfernalMobsCore.instance().config.getMobFarmCheckIntervals(), maxDamage));
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
        if (!event.getEntity().level.isClientSide) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onDropItems(event.getEntity(), event.getSource(), event.getDrops(), event.getLootingLevel(), event.isRecentlyHit(), event.getLootingLevel());
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
