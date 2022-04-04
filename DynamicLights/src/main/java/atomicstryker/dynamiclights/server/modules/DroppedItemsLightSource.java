package atomicstryker.dynamiclights.server.modules;

import atomicstryker.dynamiclights.server.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    private static final HashMap<ItemEntity, EntityItemAdapter> trackedItemMap = new HashMap<>();

    private LightConfig config;

    public DroppedItemsLightSource() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverStartEvent(ServerAboutToStartEvent event) {

        LightConfig defaultConfig = new LightConfig();
        defaultConfig.getItemsList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.TORCH), 14));
        defaultConfig.getItemsList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.GLOWSTONE), 15));
        defaultConfig.getNotWaterProofList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.TORCH), 0));

        MinecraftServer server = event.getServer();
        File configFile = new File(server.getFile(""), File.separatorChar + "config" + File.separatorChar + "dynamiclights_droppeditems.cfg");
        try {
            config = GsonConfig.loadConfigWithDefault(LightConfig.class, configFile, defaultConfig);
            if (config == null) {
                throw new UnsupportedOperationException("DroppedItemsLightSource failed parsing config file somehow...");
            }
            itemsMap = new ItemConfigHelper(config.getItemsList(), LOGGER);
            notWaterProofItems = new ItemConfigHelper(config.getNotWaterProofList(), LOGGER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void entityJoinsWorld(EntityJoinWorldEvent event) {

        if (event.getWorld().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof ItemEntity) {
            ItemEntity itemEntity = ((ItemEntity) event.getEntity());
            int lightLevel = getLightFromItemStack(itemEntity.getItem());
            if (lightLevel > 0) {
                EntityItemAdapter entityItemAdapter = new EntityItemAdapter(itemEntity);
                trackedItemMap.put(itemEntity, entityItemAdapter);
            }
        }
    }

    @SubscribeEvent
    public void serverWorldTick(TickEvent.WorldTickEvent event) {

        if (event.side != LogicalSide.SERVER) {
            return;
        }

        Iterator<Map.Entry<ItemEntity, EntityItemAdapter>> iterator = trackedItemMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ItemEntity, EntityItemAdapter> mapEntry = iterator.next();
            if (mapEntry.getKey().isAlive()) {
                mapEntry.getValue().onTick();
            } else {
                iterator.remove();
            }
        }
    }

    private int getLightFromItemStack(ItemStack stack) {
        // First check whether the item has a tag that makes it emit light
        int level = ItemLightLevels.getLightFromItemStack(stack, "dropped");
        if (level > 0 && level <= 15) {
            return level;
        }

        // Then use our config file
        return itemsMap.getLightLevel(stack);
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
            notWaterProof = notWaterProofItems.getLightLevel(eI.getItem()) > 0 || eI.getItem().getTags().anyMatch(rl -> rl.location().equals(DynamicLights.NOT_WATERPROOF_TAG));
        }

        /**
         * Since they are IDynamicLightSource instances, they will already receive
         * updates! Why do we need to do this? Because seperate Thread!
         */
        public void onTick() {
            if (entity.isOnFire()) {
                lightLevel = 15;
            } else {
                lightLevel = getLightFromItemStack(entity.getItem());
                if (notWaterProof && entity.isInWater()) {
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