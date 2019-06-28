package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.GsonConfig;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author AtomicStryker
 * <p>
 * Offers Dynamic Light functionality to EntityItem instances. Dropped
 * Torches and such can give off Light through this Module.
 */
public class DroppedItemsLightSource {

    private static final Logger LOGGER = LogManager.getLogger();
    private static ItemConfigHelper itemsMap;
    private static ItemConfigHelper notWaterProofItems;
    private Minecraft mcinstance;
    private long nextUpdate;
    private long updateInterval;
    private ArrayList<EntityItemAdapter> trackedItems;
    private boolean threadRunning;
    private LightConfig config;

    public DroppedItemsLightSource() {
        trackedItems = new ArrayList<>();
        threadRunning = false;
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::afterVanillaBootstrap);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void afterVanillaBootstrap(FMLClientSetupEvent event) {
        mcinstance = Minecraft.getInstance();
        nextUpdate = System.currentTimeMillis();

        LightConfig defaultConfig = new LightConfig();
        String torchString = ItemConfigHelper.fromItemStack(new ItemStack(Blocks.TORCH));
        defaultConfig.getItemsList().add(torchString);
        defaultConfig.getItemsList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.GLOWSTONE)));
        defaultConfig.getNotWaterProofList().add(torchString);

        File configFile = new File(Minecraft.getInstance().gameDir, "\\config\\dynamiclights_droppeditems.cfg");
        try {
            config = GsonConfig.loadConfigWithDefault(LightConfig.class, configFile, defaultConfig);
            if (config == null) {
                throw new UnsupportedOperationException("DroppedItemsLightSource failed parsing config file somehow...");
            }
            itemsMap = new ItemConfigHelper(config.getItemsList(), LOGGER);
            notWaterProofItems = new ItemConfigHelper(config.getNotWaterProofList(), LOGGER);
            updateInterval = config.getUpdateInterval();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.PlayerTickEvent event) {

        if (event.player == null || event.player.world == null) {
            return;
        }

        if (mcinstance.world != null && System.currentTimeMillis() > nextUpdate && !DynamicLights.globalLightsOff()) {
            nextUpdate = System.currentTimeMillis() + updateInterval;

            if (!threadRunning) {
                Thread thread = new EntityListChecker(mcinstance.world.func_217416_b());
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
                threadRunning = true;
            }
        }
    }

    private int getLightFromItemStack(ItemStack stack) {
        return itemsMap.contains(stack) ? 15 : 0;
    }

    private class EntityListChecker extends Thread {
        private final ArrayList<Entity> list;

        public EntityListChecker(Iterable<Entity> input) {
            list = new ArrayList<>();
            for (Entity entity : input) {
                list.add(entity);
            }
        }

        @Override
        public void run() {
            ArrayList<EntityItemAdapter> newList = new ArrayList<>();

            Entity ent;
            for (Object o : list) {
                ent = (Entity) o;
                // Loop all loaded Entities, find alive and valid ItemEntities
                if (ent instanceof ItemEntity && ent.isAlive()) {
                    // now find them in the already tracked item adapters
                    boolean found = false;
                    Iterator<EntityItemAdapter> iter = trackedItems.iterator();
                    EntityItemAdapter adapter;
                    while (iter.hasNext()) {
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
                        adapter = new EntityItemAdapter((ItemEntity) ent);
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

    private class EntityItemAdapter implements IDynamicLightSource {

        private ItemEntity entity;
        private int lightLevel;
        private boolean enabled;
        private boolean notWaterProof;

        public EntityItemAdapter(ItemEntity eI) {
            lightLevel = 0;
            enabled = false;
            entity = eI;
            notWaterProof = notWaterProofItems.contains(eI.getItem());
        }

        /**
         * Since they are IDynamicLightSource instances, they will already receive
         * updates! Why do we need to do this? Because seperate Thread!
         */
        public void onTick() {
            if (entity.isBurning()) {
                lightLevel = 15;
            } else {
                lightLevel = getLightFromItemStack(entity.getItem());

                BlockPos pos = new BlockPos(MathHelper.floor(entity.posX), MathHelper.floor(entity.posY), MathHelper.floor(entity.posZ));
                BlockState is = entity.world.getBlockState(pos);
                if (notWaterProof && is.getMaterial().isLiquid()) {
                    lightLevel = 0;
                }
            }

            if (!enabled && lightLevel > 0) {
                enableLight();
            } else if (enabled && lightLevel < 1) {
                disableLight();
            }
        }

        private void enableLight() {
            DynamicLights.addLightSource(this);
            enabled = true;
        }

        private void disableLight() {
            DynamicLights.removeLightSource(this);
            enabled = false;
        }

        @Override
        public Entity getAttachmentEntity() {
            return entity;
        }

        @Override
        public int getLightLevel() {
            return (notWaterProof && entity.isInWater()) ? 0 : lightLevel;
        }
    }

}
