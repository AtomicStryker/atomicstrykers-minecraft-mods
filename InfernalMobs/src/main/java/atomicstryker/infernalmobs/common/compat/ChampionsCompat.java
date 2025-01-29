package atomicstryker.infernalmobs.common.compat;

import net.minecraft.world.entity.Entity;
import top.theillusivec4.champions.common.capability.ChampionCapability;
import top.theillusivec4.champions.common.util.ChampionHelper;

public class ChampionsCompat {

    public static boolean isChampionEntity(Entity entity) {
        return ChampionCapability.getCapability(entity).map(champion -> ChampionHelper.isValidChampion(champion.getServer())).orElse(false);
    }
}
