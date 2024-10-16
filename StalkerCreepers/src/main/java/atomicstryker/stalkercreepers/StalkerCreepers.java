package atomicstryker.stalkercreepers;

import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(StalkerCreepers.MOD_ID)
public final class StalkerCreepers {

    public static final String MOD_ID = "stalkercreepers";

    public static final Logger LOGGER = LogManager.getLogger("Stalker Creepers");

    public StalkerCreepers() {
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Hello World! Proceeding with Creeper stalkification");
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Creeper creeper && !creeper.level().isClientSide()) {
            SwellGoal vanillaSwell = null;
            int vanillaPriority = 0;
            for (WrappedGoal availableGoal : creeper.goalSelector.getAvailableGoals()) {
                if (availableGoal.getGoal() instanceof SwellGoal swellGoal) {
                    vanillaPriority = availableGoal.getPriority();
                    vanillaSwell = swellGoal;
                    break;
                }
            }

            if (vanillaSwell == null) {
                // what? no vanilla swell? other mod must have removed it or something, exit here
                return;
            }

            creeper.goalSelector.removeGoal(vanillaSwell);
            creeper.goalSelector.addGoal(vanillaPriority, new StalkerCreeperSwellGoal(creeper));
            // LOGGER.debug("modified Creeper " + creeper);
        }
    }
}