package atomicstryker.dynamiclights.client.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
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
 *         Offers Dynamic Light functionality to EntityItem instances. Dropped
 *         Torches and such can give off Light through this Module.
 *
 */
@Mod(DroppedItemsLightSource.MOD_ID)
@Mod.EventBusSubscriber(modid = DroppedItemsLightSource.MOD_ID, value = Dist.CLIENT)
public class DroppedItemsLightSource
{
    static final String MOD_ID = "dynamiclights_droppeditems";

    private Minecraft mcinstance;
    private long nextUpdate;
    private long updateInterval;
    private ArrayList<EntityItemAdapter> trackedItems;
    private boolean threadRunning;

    private static ItemConfigHelper itemsMap;
    private static ItemConfigHelper notWaterProofItems;

    public DroppedItemsLightSource()
    {
        trackedItems = new ArrayList<>();
        threadRunning = false;

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
    }

    public void onClientSetup(FMLClientSetupEvent evt)
    {
        mcinstance = evt.getMinecraftSupplier().get();
        nextUpdate = System.currentTimeMillis();
    }

    public void onConfigLoad(ModConfig.ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == CLIENT_SPEC)
        {
            loadConfig();
        }
    }

    public static final DroppedItemsLightSource.ClientConfig CLIENT_CONFIG;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static List<? extends String> itemsList = new ArrayList<>();
    public static List<? extends String> notWaterProofList = new ArrayList<>();

    static
    {
        final Pair<DroppedItemsLightSource.ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(DroppedItemsLightSource.ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT_CONFIG = specPair.getLeft();
    }

    public static class ClientConfig
    {
        public ForgeConfigSpec.ConfigValue<List<? extends String>> itemsList;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> notWaterProofList;
        public ForgeConfigSpec.ConfigValue<Integer> updateInterval;

        ClientConfig(ForgeConfigSpec.Builder builder)
        {
            itemsList = builder.comment("Item IDs that shine light when dropped in the World").translation("forge.configgui.itemsList").defineList("itemsList", getDefaultLightItems(),
                    i -> i instanceof String);
            notWaterProofList = builder.comment("Item IDs that do not shine light when dropped and in water, have to be present in LightItems.").translation("forge.configgui.notWaterProofList")
                    .defineList("notWaterProofList", Lists.newArrayList(), i -> i instanceof String);
            updateInterval = builder.comment("Update Interval time for item entities in milliseconds. The lower the better and costlier.").define("updateInterval", 1000, i -> i instanceof Integer);
            builder.pop();
        }
    }

    private static List<String> getDefaultLightItems()
    {
        ItemStack torchStack = new ItemStack(Blocks.TORCH);
        List<String> output = new ArrayList<>();
        output.add(ItemConfigHelper.fromItemStack(torchStack));
        return output;
    }

    public static void loadConfig()
    {
        itemsList = CLIENT_CONFIG.itemsList.get();
        notWaterProofList = CLIENT_CONFIG.notWaterProofList.get();
        itemsMap = new ItemConfigHelper(itemsList);
        notWaterProofItems = new ItemConfigHelper(notWaterProofList);
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

    private int getLightFromItemStack(ItemStack stack)
    {
        return itemsMap.contains(stack) ? 15 : 0;
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
            ArrayList<EntityItemAdapter> newList = new ArrayList<>();

            Entity ent;
            for (Object o : list)
            {
                ent = (Entity) o;
                // Loop all loaded Entities, find alive and valid ItemEntities
                if (ent instanceof EntityItem && ent.isAlive())
                {
                    // now find them in the already tracked item adapters
                    boolean found = false;
                    Iterator<EntityItemAdapter> iter = trackedItems.iterator();
                    EntityItemAdapter adapter;
                    while (iter.hasNext())
                    {
                        adapter = iter.next();
                        if (adapter.getAttachmentEntity().equals(ent)) // already
                                                                       // tracked!
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
                        adapter = new EntityItemAdapter((EntityItem) ent);
                        adapter.onTick();
                        newList.add(adapter);
                    }
                }
            }
            // any remaining adapters were not in the loaded entities. The main
            // Dynamic Lights mod will kill them.
            trackedItems = newList;
            threadRunning = false;
        }

    }

    private class EntityItemAdapter implements IDynamicLightSource
    {

        private EntityItem entity;
        private int lightLevel;
        private boolean enabled;
        private boolean notWaterProof;

        public EntityItemAdapter(EntityItem eI)
        {
            lightLevel = 0;
            enabled = false;
            entity = eI;
            notWaterProof = notWaterProofItems.contains(eI.getItem());
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
                lightLevel = getLightFromItemStack(entity.getItem());

                BlockPos pos = new BlockPos(MathHelper.floor(entity.posX), MathHelper.floor(entity.posY), MathHelper.floor(entity.posZ));
                IBlockState is = entity.world.getBlockState(pos);
                if (notWaterProof && is.getMaterial().isLiquid())
                {
                    lightLevel = 0;
                }
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
            return (notWaterProof && entity.isInWater()) ? 0 : lightLevel;
        }
    }

}
