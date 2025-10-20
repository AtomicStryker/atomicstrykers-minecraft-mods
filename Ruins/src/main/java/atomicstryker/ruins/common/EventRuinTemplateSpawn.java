package atomicstryker.ruins.common;

import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.bus.CancellableEventBus;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import org.jetbrains.annotations.NotNull;


/**
 * Executed before and after a Ruins template is created in the world by the Worldgen Hook.
 * Does also fire before manually spawning a template with /testruin.
 * Note: Only the isPrior=false event has the final y value for the boundaries after embedding
 *
 * @param world    World template is about to spawn in
 * @param templ    template about to spawn, contains RuinData with bounding box
 * @param x        x Coordinate
 * @param y        y Coordinate
 * @param z        z Coordinate
 * @param rotation Rotation value
 * @param testing  whether or not the template was manually spawned
 *                 * @param isPrior true before a Ruins spawned, can be cancelled only then, false otherwise
 */
public record EventRuinTemplateSpawn(Level world, RuinTemplate templ, int x, int y, int z, int rotation,
                                     boolean testing, boolean isPrior) implements Cancellable, RecordEvent {
    public static final CancellableEventBus<@NotNull EventRuinTemplateSpawn> BUS = CancellableEventBus.create(EventRuinTemplateSpawn.class);
}
