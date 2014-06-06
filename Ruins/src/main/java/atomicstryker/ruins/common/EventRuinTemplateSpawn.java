package atomicstryker.ruins.common;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Executed before a Ruins template is created in the world by the Worldgen Hook.
 * Does also fire before manually spawning a template with /testruin.
 */
@Cancelable
public class EventRuinTemplateSpawn extends WorldEvent
{
    
    final RuinTemplate template;
    final int x, y, z, rotation;
    final boolean testingRuin;
    
    /**
     * @param world World template is about to spawn in
     * @param templ template about to spawn, contains RuinData with bounding box
     * @param a x Coordinate
     * @param b y Coordinate
     * @param c z Coordinate
     * @param r Rotation value
     * @param bool whether or not the template was manually spawned
     */
    public EventRuinTemplateSpawn(World world, RuinTemplate templ, int a, int b, int c, int r, boolean bool)
    {
        super(world);
        template = templ;
        x = a;
        y = b;
        z = c;
        rotation = r;
        testingRuin = bool;
    }

}
