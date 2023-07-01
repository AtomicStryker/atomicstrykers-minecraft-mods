package atomicstryker.stalkercreepers;

import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(StalkerCreepers.MOD_ID)
public final class StalkerCreepers {

    public static final String MOD_ID = "stalkercreepers";

    public static final Logger LOGGER = LogManager.getLogger("Stalker Creepers");

    public StalkerCreepers() {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Hello World! Proceeding with Creeper creepification");
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
            LOGGER.info("modifier Creeper " + creeper);
        }
    }
}