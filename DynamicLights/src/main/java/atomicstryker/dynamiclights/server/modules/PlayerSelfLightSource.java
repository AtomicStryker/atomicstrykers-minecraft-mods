package atomicstryker.dynamiclights.server.modules;

import atomicstryker.dynamiclights.server.DynamicLights;
import atomicstryker.dynamiclights.server.GsonConfig;
import atomicstryker.dynamiclights.server.IDynamicLightSource;
import atomicstryker.dynamiclights.server.ItemConfigHelper;
import atomicstryker.dynamiclights.server.ItemLightLevels;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author AtomicStryker
 * <p>
 * Offers Dynamic Light functionality to the Player Entity itself.
 * Handheld Items and Armor can give off Light through this Module.
 */
public class PlayerSelfLightSource {

    private static final Logger LOGGER = LogManager.getLogger();
    private static ItemConfigHelper itemsMap;
    private static ItemConfigHelper notWaterProofItems;
    private LightConfig config;
    private final HashMap<Player, PlayerLightSourceContainer> playerLightsMap = new HashMap<>();

    public PlayerSelfLightSource() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverStartEvent(ServerAboutToStartEvent event) {

        MinecraftServer server = event.getServer();
        RegistryAccess registryAccess = server.registryAccess();

        LightConfig defaultConfig = new LightConfig();
        defaultConfig.getItemsList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.TORCH), 14, registryAccess));
        defaultConfig.getItemsList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.GLOWSTONE), 15, registryAccess));
        defaultConfig.getNotWaterProofList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.TORCH), 0, registryAccess));

        File configFile = new File(server.getFile(""), File.separatorChar + "config" + File.separatorChar + "dynamiclights_selflight.cfg");
        try {
            config = GsonConfig.loadConfigWithDefault(LightConfig.class, configFile, defaultConfig);
            if (config == null) {
                throw new UnsupportedOperationException("PlayerSelfLightSource failed parsing config file somehow...");
            }
            itemsMap = new ItemConfigHelper(config.getItemsList(), LOGGER, registryAccess);
            notWaterProofItems = new ItemConfigHelper(config.getNotWaterProofList(), LOGGER, registryAccess);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        if (player.isAlive()) {

            PlayerLightSourceContainer playerLightSourceContainer = playerLightsMap.get(player);
            if (playerLightSourceContainer == null) {
                LOGGER.trace("built new PlayerLightSourceContainer for player {}", player);
                playerLightSourceContainer = new PlayerLightSourceContainer(player);
                playerLightsMap.put(player, playerLightSourceContainer);
            }

            int prevLight = playerLightSourceContainer.lightLevel;
            boolean isUnderwater = checkPlayerWater(player);

            ItemStack item = ItemStack.EMPTY;
            LOGGER.trace("checking for light from main hand item {}", player.getItemInHand(InteractionHand.MAIN_HAND));
            int main = getLightFromItemStack(player.getItemInHand(InteractionHand.MAIN_HAND), isUnderwater, player.registryAccess());
            int off = getLightFromItemStack(player.getItemInHand(InteractionHand.OFF_HAND), isUnderwater, player.registryAccess());
            if (main >= off && main > 0) {
                item = player.getItemInHand(InteractionHand.MAIN_HAND);
                playerLightSourceContainer.lightLevel = main;
            } else if (off >= main && off > 0) {
                item = player.getItemInHand(InteractionHand.OFF_HAND);
                playerLightSourceContainer.lightLevel = off;
            } else {
                playerLightSourceContainer.lightLevel = 0;
            }
            LOGGER.trace("Self light tick, main:{}, off:{}, light:{}, chosen itemstack:{}", main, off, playerLightSourceContainer.lightLevel, item);

            for (ItemStack armor : player.getInventory().armor) {
                playerLightSourceContainer.lightLevel = Math.max(playerLightSourceContainer.lightLevel, getLightFromItemStack(armor, isUnderwater, player.registryAccess()));
            }

            if (prevLight != 0 && playerLightSourceContainer.lightLevel != prevLight) {
                playerLightSourceContainer.lightLevel = 0;
            } else {
                if (player.isOnFire()) {
                    playerLightSourceContainer.lightLevel = 15;
                }
            }

            if (!playerLightSourceContainer.enabled && playerLightSourceContainer.lightLevel > 0) {
                enableLight(playerLightSourceContainer);
            } else if (playerLightSourceContainer.enabled && playerLightSourceContainer.lightLevel < 1) {
                disableLight(playerLightSourceContainer);
            }
        } else {
            PlayerLightSourceContainer playerLightSourceContainer = playerLightsMap.get(player);
            if (playerLightSourceContainer != null) {
                disableLight(playerLightSourceContainer);
                playerLightsMap.remove(playerLightSourceContainer.thePlayer);
            }
        }

    }

    private boolean checkPlayerWater(Player thePlayer) {
        return thePlayer.isEyeInFluid(FluidTags.WATER);
    }

    private int getLightFromItemStack(ItemStack stack, boolean isUnderwater, RegistryAccess registryAccess) {
        // first check if we are in water and the item is not waterproof
        if (isUnderwater && (notWaterProofItems.getLightLevel(stack, registryAccess) > 0 || stack.getTags().anyMatch(rl -> rl.location().equals(DynamicLights.NOT_WATERPROOF_TAG)))) {
            return 0;
        }
        // check whether the item has a tag that makes it emit light
        int level = ItemLightLevels.getLightFromItemStack(stack, "self");
        if (level > 0 && level <= 15) {
            return level;
        }
        // Then use our config file
        return itemsMap.getLightLevel(stack, registryAccess);
    }

    private void enableLight(PlayerLightSourceContainer container) {
        DynamicLights.addLightSource(container);
        container.enabled = true;
    }

    private void disableLight(PlayerLightSourceContainer container) {
        DynamicLights.removeLightSource(container);
        container.enabled = false;
    }

    class PlayerLightSourceContainer implements IDynamicLightSource {

        PlayerLightSourceContainer(Player player) {
            thePlayer = player;
            lightLevel = 0;
            enabled = false;
        }

        int lightLevel;
        boolean enabled;
        Player thePlayer;

        @Override
        public Entity getAttachmentEntity() {
            return thePlayer;
        }

        @Override
        public int getLightLevel() {
            return lightLevel;
        }
    }

}