package atomicstryker.simplyhax;

import java.util.Map.Entry;

import net.minecraft.entity.EntityList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "SimplyHaxEntityDump", name = "Simply Hax Entity Dump", version = "1.5.2B")
public class SimplyHaxEntityDump
{
	
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SuppressWarnings("unchecked")
    @ForgeSubscribe
    public void onWorldStart(WorldEvent.Load event)
    {
        System.out.println("Simply Hax Entity Dump following, these are the exact Mobspawner names");
        Entry<String, Class<?>> e;
        for (Object o : EntityList.stringToClassMapping.entrySet())
        {
            e = (Entry<String, Class<?>>) o;
            System.out.printf("[%s] maps to [%s]\n", e.getKey(), e.getValue().getName());
        }
    }

}
