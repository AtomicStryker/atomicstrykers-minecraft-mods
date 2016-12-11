package atomicstryker.simplyhax;

import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "simplyhaxentitydump", name = "Simply Hax Entity Dump", version = "1.11")
public class SimplyHaxEntityDump
{

    private boolean dumped = false;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldStart(WorldEvent.Load event)
    {
        if (!dumped)
        {
            dumped = true;
            System.out.println("Simply Hax Entity Dump following, these are the exact Mobspawner names");
            for (ResourceLocation rsl : EntityList.getEntityNameList())
            {
                System.out.printf("[%s] resource location%n", rsl);

            }
        }
    }

}
