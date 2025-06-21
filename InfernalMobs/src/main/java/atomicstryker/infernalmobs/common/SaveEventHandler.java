package atomicstryker.infernalmobs.common;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;

import java.util.function.Consumer;


public class SaveEventHandler implements Consumer<LevelEvent.Unload> {

    @Override
    public void accept(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level) {
            Level level = ((Level) event.getLevel());
            InfernalMobsCore.clearAllElitesOfLevel(level);
        }
    }
}
