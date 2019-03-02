package atomicstryker.dynamiclights.client.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * 
 * @author AtomicStryker
 *
 *         Offers Dynamic Light functionality to EntityLiving instances,
 *         Fireballs and Arrows on Fire. Burning Entites can give off Light
 *         through this Module.
 *
 */
@Mod(BurningEntitiesLightSource.MOD_ID)
@Mod.EventBusSubscriber(modid = BurningEntitiesLightSource.MOD_ID, value = Dist.CLIENT)
public class BurningEntitiesLightSource
{
    static final String MOD_ID = "dynamiclights_burningentities";

    private Minecraft mcinstance;
    private long nextUpdate;
    private ArrayList<EntityLightAdapter> trackedEntities;
    private boolean threadRunning;

    public BurningEntitiesLightSource()
    {
        nextUpdate = System.currentTimeMillis();
        trackedEntities = new ArrayList<>();
        threadRunning = false;
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onConfigLoad);
    }

    public void onClientSetup(FMLClientSetupEvent evt)
    {
        mcinstance = evt.getMinecraftSupplier().get();
    }

    public void onConfigLoad(ModConfig.ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == CLIENT_SPEC)
        {
            loadConfig();
        }
    }

    public static final BurningEntitiesLightSource.ClientConfig CLIENT_CONFIG;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static Integer updateInterval = 0;

    static
    {
        final Pair<BurningEntitiesLightSource.ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(BurningEntitiesLightSource.ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT_CONFIG = specPair.getLeft();
    }

    public static class ClientConfig
    {
        public ForgeConfigSpec.ConfigValue<Integer> updateInterval;

        ClientConfig(ForgeConfigSpec.Builder builder)
        {
            updateInterval = builder.comment("Update Interval time for all burning EntityLiving, Arrows and Fireballs in milliseconds. The lower the better and costlier.").define("updateInterval",
                    1000, i -> i instanceof Integer);
            builder.pop();
        }
    }

    public static void loadConfig()
    {
        updateInterval = CLIENT_CONFIG.updateInterval.get();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (mcinstance.world != null && System.currentTimeMillis() > nextUpdate && !DynamicLights.globalLightsOff())
        {
            nextUpdate = System.currentTimeMillis() + updateInterval;

            if (!threadRunning)
            {
                Thread thread = new EntityListChecker(mcinstance.world.loadedEntityList);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
                threadRunning = true;
            }
        }
    }

    private class EntityListChecker extends Thread
    {
        private final Object[] list;

        public EntityListChecker(List<Entity> input)
        {
            list = input.toArray();
        }

        @Override
        public void run()
        {
            ArrayList<EntityLightAdapter> newList = new ArrayList<>();

            Entity ent;
            for (Object o : list)
            {
                ent = (Entity) o;
                // Loop all loaded Entities, find alive and valid EntityLiving not otherwise
                // handled
                if ((ent instanceof EntityLivingBase || ent instanceof EntityFireball || ent instanceof EntityArrow) && ent.isAlive() && ent.isBurning() && !(ent instanceof EntityPlayer))
                {
                    // now find them in the already tracked adapters
                    boolean found = false;
                    Iterator<EntityLightAdapter> iter = trackedEntities.iterator();
                    EntityLightAdapter adapter;
                    while (iter.hasNext())
                    {
                        adapter = iter.next();
                        if (adapter.getAttachmentEntity().equals(ent)) // already tracked!
                        {
                            adapter.onTick(); // execute a tick
                            newList.add(adapter); // put them in the new list
                            found = true;
                            iter.remove(); // remove them from the old
                            break;
                        }
                    }

                    if (!found) // wasnt already tracked
                    {
                        // make new, tick, put in new list
                        adapter = new EntityLightAdapter(ent);
                        adapter.onTick();
                        newList.add(adapter);
                    }
                }
            }
            // any remaining adapters were not targeted again, which probably means they
            // dont burn anymore. The tick will finish them off.
            trackedEntities.forEach(EntityLightAdapter::onTick);

            trackedEntities = newList;
            threadRunning = false;
        }

    }

    private class EntityLightAdapter implements IDynamicLightSource
    {

        private Entity entity;
        private int lightLevel;
        private boolean enabled;

        public EntityLightAdapter(Entity e)
        {
            lightLevel = 0;
            enabled = false;
            entity = e;
        }

        /**
         * Since they are IDynamicLightSource instances, they will already receive
         * updates! Why do we need to do this? Because seperate Thread!
         */
        public void onTick()
        {
            if (entity.isBurning())
            {
                lightLevel = 15;
            }
            else
            {
                lightLevel = 0;
            }

            if (!enabled && lightLevel > 0)
            {
                enableLight();
            }
            else if (enabled && lightLevel < 1)
            {
                disableLight();
            }
        }

        private void enableLight()
        {
            DynamicLights.addLightSource(this);
            enabled = true;
        }

        private void disableLight()
        {
            DynamicLights.removeLightSource(this);
            enabled = false;
        }

        @Override
        public Entity getAttachmentEntity()
        {
            return entity;
        }

        @Override
        public int getLightLevel()
        {
            return lightLevel;
        }
    }

}
