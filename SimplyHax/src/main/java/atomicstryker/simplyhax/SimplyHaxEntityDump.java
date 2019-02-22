package atomicstryker.simplyhax;

import java.util.Map;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(SimplyHaxEntityDump.MOD_ID)
@Mod.EventBusSubscriber(modid = SimplyHaxEntityDump.MOD_ID, value = Dist.CLIENT)
public class SimplyHaxEntityDump
{

    public static final String MOD_ID = "simplyhaxentitydump";

    private boolean dumped = false;

    @SubscribeEvent
    public void onWorldStart(WorldEvent.Load event)
    {
        if (!dumped)
        {
            dumped = true;
            System.out.println("Simply Hax Entity Dump following, these are the exact Mobspawner names");
            for (Map.Entry<ResourceLocation, EntityType<?>> entityEntry : ForgeRegistries.ENTITIES.getEntries())
            {
                System.out.printf("resource [%s], entity type [%s]%n", entityEntry.getKey(), entityEntry.getValue());
            }
        }
    }

}
