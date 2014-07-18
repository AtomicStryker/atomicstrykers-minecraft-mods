package atomicstryker.simplyhax;

import java.util.Map.Entry;

import net.minecraft.entity.EntityList;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "SimplyHaxEntityDump", name = "Simply Hax Entity Dump", version = "1.7.10")
public class SimplyHaxEntityDump
{
	
    private boolean dumped = false;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onWorldStart(WorldEvent.Load event)
    {
        if (!dumped)
        {
            dumped = true;
            System.out.println("Simply Hax Entity Dump following, these are the exact Mobspawner names");
            Entry<String, Class<?>> e;
            for (Object o : EntityList.stringToClassMapping.entrySet())
            {
                e = (Entry<String, Class<?>>) o;
                System.out.printf("[%s] maps to [%s]\n", e.getKey(), e.getValue().getName());
                
            }
        }
    }

}
