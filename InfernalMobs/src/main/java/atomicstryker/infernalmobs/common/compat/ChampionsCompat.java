package atomicstryker.infernalmobs.common.compat;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.common.capability.ChampionCapability;
import top.theillusivec4.champions.common.util.ChampionBuilder;
import top.theillusivec4.champions.common.util.ChampionHelper;

public class ChampionsCompat {

    /**
     * Determines whether the given entity should be handled by the Champions mod
     * instead of Infernal Mobs.
     * <p>
     * This method ensures compatibility between the Champions mod and Infernal Mobs.
     * If champion compatibility is enabled and the entity is recognized as a champion,
     * it checks whether Infernal Mobs should take control. If Infernal Mobs is disabled
     * for champions, it prevents the entity from being handled by Infernal Mobs.
     * Otherwise, it removes the champion's modifiers, rank, and affixes, allowing
     * Infernal Mobs to take over.
     *
     * @param entity The entity being checked for champion handling.
     * @return {@code true} if the entity should be handled by Champions and not
     * Infernal Mobs; {@code false} if Infernal Mobs should proceed with handling.
     */
    public static boolean shouldChampionHandleEntity(Entity entity) {
        var core = InfernalMobsCore.instance();
        var config = core.getConfig();

        // if disabled champion compat, or champion mod not loaded, stop process compat
        if (!config.isEnableChampionCompat() || !core.isChampionLoaded()) {
            return false;
        }

        // Check if entity is champion mob
        if (!ChampionHelper.isChampionEntity(entity)) {
            return false;
        }

        LivingEntity livingEntity = (LivingEntity) entity;

        // if enabled random selection, let selection decide handle by Champions mod or Infernal Mob mod
        if (config.useRandomChampionSelection() && livingEntity.getRandom().nextDouble() > config.getChampionSelectionChance()) {
            return false; // handle by Infernal Mobs
        }

        // if not use infernal mob, then handle by champion
        if (!config.getUseInfernalMobs()) {
            InfernalMobsCore.LOGGER.debug("Stop spawning {} infernal mob at {} uuid: {}, because entity is already handled by Champions mod.",
                    entity.getName().getString(), entity.blockPosition(), entity.getUUID());
            return true;
        }

        // else remove champion's Modifier, Rank and Affixes, let Infernal mobs mod handel
        ChampionCapability.getCapability(entity).ifPresent(ChampionBuilder::resetAndUpdate);

        return false;
    }

}
