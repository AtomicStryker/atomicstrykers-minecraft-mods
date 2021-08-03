package atomicstryker.dynamiclights.server.modules;

import atomicstryker.dynamiclights.server.DynamicLights;
import atomicstryker.dynamiclights.server.GsonConfig;
import atomicstryker.dynamiclights.server.IDynamicLightSource;
import atomicstryker.dynamiclights.server.ItemConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;
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
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverStartEvent(FMLServerAboutToStartEvent event) {

        LightConfig defaultConfig = new LightConfig();
        String torchString = ItemConfigHelper.fromItemStack(new ItemStack(Blocks.TORCH));
        defaultConfig.getItemsList().add(torchString);
        defaultConfig.getItemsList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.GLOWSTONE)));
        defaultConfig.getNotWaterProofList().add(torchString);

        MinecraftServer server = event.getServer();
        File configFile = new File(server.getFile(""), File.separatorChar + "config" + File.separatorChar + "dynamiclights_selflight.cfg");
        try {
            config = GsonConfig.loadConfigWithDefault(LightConfig.class, configFile, defaultConfig);
            if (config == null) {
                throw new UnsupportedOperationException("PlayerSelfLightSource failed parsing config file somehow...");
            }
            itemsMap = new ItemConfigHelper(config.getItemsList(), LOGGER);
            notWaterProofItems = new ItemConfigHelper(config.getNotWaterProofList(), LOGGER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {

        if (event.side != LogicalSide.SERVER) {
            return;
        }

        if (event.player.isAlive()) {

            PlayerLightSourceContainer playerLightSourceContainer = playerLightsMap.get(event.player);
            if (playerLightSourceContainer == null) {
                LOGGER.trace("built new PlayerLightSourceContainer for player {}", event.player);
                playerLightSourceContainer = new PlayerLightSourceContainer(event.player);
                playerLightsMap.put(event.player, playerLightSourceContainer);
            }

            int prevLight = playerLightSourceContainer.lightLevel;

            ItemStack item = ItemStack.EMPTY;
            LOGGER.trace("checking for light from main hand item {}", event.player.getItemInHand(InteractionHand.MAIN_HAND));
            int main = getLightFromItemStack(event.player.getItemInHand(InteractionHand.MAIN_HAND));
            int off = getLightFromItemStack(event.player.getItemInHand(InteractionHand.OFF_HAND));
            if (main >= off && main > 0) {
                item = event.player.getItemInHand(InteractionHand.MAIN_HAND);
                playerLightSourceContainer.lightLevel = main;
            } else if (off >= main && off > 0) {
                item = event.player.getItemInHand(InteractionHand.OFF_HAND);
                playerLightSourceContainer.lightLevel = off;
            } else {
                playerLightSourceContainer.lightLevel = 0;
            }
            LOGGER.trace("Self light tick, main:{}, off:{}, light:{}, chosen itemstack:{}", main, off, playerLightSourceContainer.lightLevel, item);

            for (ItemStack armor : event.player.getInventory().armor) {
                playerLightSourceContainer.lightLevel = Math.max(playerLightSourceContainer.lightLevel, getLightFromItemStack(armor));
            }

            if (prevLight != 0 && playerLightSourceContainer.lightLevel != prevLight) {
                playerLightSourceContainer.lightLevel = 0;
            } else {
                if (event.player.isOnFire()) {
                    playerLightSourceContainer.lightLevel = 15;
                } else {
                    if (checkPlayerWater(event.player) && notWaterProofItems.contains(item)) {
                        playerLightSourceContainer.lightLevel = 0;
                        LOGGER.trace("Self light tick, water blocked light!");
                        for (ItemStack armor : event.player.getInventory().armor) {
                            if (!notWaterProofItems.contains(armor)) {
                                playerLightSourceContainer.lightLevel = Math.max(playerLightSourceContainer.lightLevel, getLightFromItemStack(armor));
                            }
                        }
                    }
                }
            }

            if (!playerLightSourceContainer.enabled && playerLightSourceContainer.lightLevel > 0) {
                enableLight(playerLightSourceContainer);
            } else if (playerLightSourceContainer.enabled && playerLightSourceContainer.lightLevel < 1) {
                disableLight(playerLightSourceContainer);
            }
        } else {
            PlayerLightSourceContainer playerLightSourceContainer = playerLightsMap.get(event.player);
            if (playerLightSourceContainer != null) {
                disableLight(playerLightSourceContainer);
                playerLightsMap.remove(playerLightSourceContainer.thePlayer);
            }
        }

    }

    private boolean checkPlayerWater(Player thePlayer) {
        if (thePlayer.isInWater()) {
            int x = Mth.floor(thePlayer.getX() + 0.5D);
            int y = Mth.floor(thePlayer.getY() + thePlayer.getEyeHeight());
            int z = Mth.floor(thePlayer.getZ() + 0.5D);
            BlockState is = thePlayer.level.getBlockState(new BlockPos(x, y, z));
            return is.getMaterial().isLiquid();
        }
        return false;
    }

    private int getLightFromItemStack(ItemStack stack) {
        if (itemsMap.contains(stack)) {
            return 15;
        }
        return 0;
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
