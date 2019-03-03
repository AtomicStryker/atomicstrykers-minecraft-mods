package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.GsonConfig;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.rift.listener.BootstrapListener;
import org.dimdev.rift.listener.client.ClientTickable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author AtomicStryker
 * <p>
 * Offers Dynamic Light functionality to EntityItem instances. Dropped
 * Torches and such can give off Light through this Module.
 */
public class DroppedItemsLightSource implements BootstrapListener, ClientTickable {
    private static final Logger LOGGER = LogManager.getLogger();

    private Minecraft mcinstance;
    private long nextUpdate;
    private long updateInterval;
    private ArrayList<EntityItemAdapter> trackedItems;
    private boolean threadRunning;

    private static ItemConfigHelper itemsMap;
    private static ItemConfigHelper notWaterProofItems;

    public DroppedItemsLightSource() {
        trackedItems = new ArrayList<>();
        threadRunning = false;
    }

    private LightConfig config;

    @Override
    public void afterVanillaBootstrap() {
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

    @Override
    public void clientTick(Minecraft client) {

        if (client == null || client.player == null || client.player.world == null) {
            return;
        }

        if (mcinstance.world != null && System.currentTimeMillis() > nextUpdate && !DynamicLights.globalLightsOff()) {
            nextUpdate = System.currentTimeMillis() + updateInterval;

            if (!threadRunning) {
                Thread thread = new EntityListChecker(mcinstance.world.loadedEntityList);
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
        private final Object[] list;

        public EntityListChecker(List<Entity> input) {
            list = input.toArray();
        }

        @Override
        public void run() {
            ArrayList<EntityItemAdapter> newList = new ArrayList<>();

            Entity ent;
            for (Object o : list) {
                ent = (Entity) o;
                // Loop all loaded Entities, find alive and valid ItemEntities
                if (ent instanceof EntityItem && ent.isAlive()) {
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

    private class EntityItemAdapter implements IDynamicLightSource {

        private EntityItem entity;
        private int lightLevel;
        private boolean enabled;
        private boolean notWaterProof;

        public EntityItemAdapter(EntityItem eI) {
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
                IBlockState is = entity.world.getBlockState(pos);
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
