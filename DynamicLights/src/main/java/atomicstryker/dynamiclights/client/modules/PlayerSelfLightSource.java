package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.GsonConfig;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
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

/**
 * @author AtomicStryker
 * <p>
 * Offers Dynamic Light functionality to the Player Entity itself.
 * Handheld Items and Armor can give off Light through this Module.
 */
public class PlayerSelfLightSource implements IDynamicLightSource {

    private static final Logger LOGGER = LogManager.getLogger();
    private static ItemConfigHelper itemsMap;
    private static ItemConfigHelper notWaterProofItems;
    public boolean fmlOverrideEnable;
    private PlayerEntity thePlayer;
    private World lastWorld;
    private int lightLevel;
    private boolean enabled;
    private LightConfig config;

    public PlayerSelfLightSource() {
        lightLevel = 0;
        enabled = false;
        lastWorld = null;
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::afterVanillaBootstrap);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void afterVanillaBootstrap(FMLClientSetupEvent event) {

        LightConfig defaultConfig = new LightConfig();
        String torchString = ItemConfigHelper.fromItemStack(new ItemStack(Blocks.TORCH));
        defaultConfig.getItemsList().add(torchString);
        defaultConfig.getItemsList().add(ItemConfigHelper.fromItemStack(new ItemStack(Blocks.GLOWSTONE)));
        defaultConfig.getNotWaterProofList().add(torchString);

        File configFile = new File(Minecraft.getInstance().gameDir, "\\config\\dynamiclights_selflight.cfg");
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
    public void clientTick(TickEvent.PlayerTickEvent event) {

        if (event.player == null || event.player.world == null) {
            return;
        }

        if (thePlayer != event.player || lastWorld != thePlayer.world) {
            thePlayer = event.player;
            lastWorld = thePlayer.world;
        }

        if (thePlayer.isAlive() && !DynamicLights.globalLightsOff()) {
            if (fmlOverrideEnable) {
                return;
            }
            int prevLight = lightLevel;

            ItemStack item = ItemStack.EMPTY;
            LOGGER.trace("checking for light from main hand item {}", thePlayer.getHeldItemMainhand());
            int main = getLightFromItemStack(thePlayer.getHeldItemMainhand());
            int off = getLightFromItemStack(thePlayer.getHeldItemOffhand());
            if (main >= off && main > 0) {
                item = thePlayer.getHeldItemMainhand();
                lightLevel = main;
            } else if (off >= main && off > 0) {
                item = thePlayer.getHeldItemOffhand();
                lightLevel = off;
            } else {
                lightLevel = 0;
            }
            LOGGER.trace("Self light tick, main:{}, off:{}, light:{}, chosen itemstack:{}", main, off, lightLevel, item);

            for (ItemStack armor : thePlayer.inventory.armorInventory) {
                lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
            }

            if (prevLight != 0 && lightLevel != prevLight) {
                lightLevel = 0;
            } else {
                if (thePlayer.isBurning()) {
                    lightLevel = 15;
                } else {
                    if (checkPlayerWater(thePlayer) && notWaterProofItems.contains(item)) {
                        lightLevel = 0;
                        LOGGER.trace("Self light tick, water blocked light!");
                        for (ItemStack armor : thePlayer.inventory.armorInventory) {
                            if (!notWaterProofItems.contains(armor)) {
                                lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
                            }
                        }
                    }
                }
            }

            if (!enabled && lightLevel > 0) {
                enableLight();
            } else if (enabled && lightLevel < 1) {
                disableLight();
            }
        }

    }

    private boolean checkPlayerWater(PlayerEntity thePlayer) {
        if (thePlayer.isInWater()) {
            int x = MathHelper.floor(thePlayer.posX + 0.5D);
            int y = MathHelper.floor(thePlayer.posY + thePlayer.getEyeHeight());
            int z = MathHelper.floor(thePlayer.posZ + 0.5D);
            BlockState is = thePlayer.world.getBlockState(new BlockPos(x, y, z));
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

    private void enableLight() {
        DynamicLights.addLightSource(this);
        enabled = true;
    }

    private void disableLight() {
        if (!fmlOverrideEnable) {
            DynamicLights.removeLightSource(this);
            enabled = false;
        }
    }

    @Override
    public Entity getAttachmentEntity() {
        return thePlayer;
    }

    @Override
    public int getLightLevel() {
        return lightLevel;
    }

}
