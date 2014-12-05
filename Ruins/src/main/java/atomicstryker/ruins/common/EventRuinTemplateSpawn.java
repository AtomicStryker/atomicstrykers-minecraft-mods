package atomicstryker.ruins.common;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Executed before and after a Ruins template is created in the world by the Worldgen Hook.
 * Does also fire before manually spawning a template with /testruin.
 * Note: Only the isPrior=false event has the final y value for the boundaries after embedding
 */
@Cancelable
public class EventRuinTemplateSpawn extends WorldEvent
{
    
    public final RuinTemplate template;
    public final int x, y, z, rotation;
    public final boolean testingRuin;
    public final boolean isPrePhase;
    
    /**
     * @param world World template is about to spawn in
     * @param templ template about to spawn, contains RuinData with bounding box
     * @param a x Coordinate
     * @param b y Coordinate
     * @param c z Coordinate
     * @param r Rotation value
     * @param testing whether or not the template was manually spawned
     * @param isPrior true before a Ruins spawned, can be cancelled only then, false otherwise
     */
    public EventRuinTemplateSpawn(World world, RuinTemplate templ, int a, int b, int c, int r, boolean testing, boolean isPrior)
    {
        super(world);
        template = templ;
        x = a;
        y = b;
        z = c;
        rotation = r;
        testingRuin = testing;
        isPrePhase = isPrior;
    }

}
