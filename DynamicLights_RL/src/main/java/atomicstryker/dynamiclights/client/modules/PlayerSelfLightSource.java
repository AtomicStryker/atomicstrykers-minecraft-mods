package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.GsonConfig;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.rift.listener.BootstrapListener;
import org.dimdev.rift.listener.client.ClientTickable;

import java.io.File;
import java.io.IOException;

/**
 * @author AtomicStryker
 * <p>
 * Offers Dynamic Light functionality to the Player Entity itself.
 * Handheld Items and Armor can give off Light through this Module.
 */
public class PlayerSelfLightSource implements IDynamicLightSource, BootstrapListener, ClientTickable {

    private static final Logger LOGGER = LogManager.getLogger();

    private EntityPlayer thePlayer;
    private World lastWorld;
    private int lightLevel;
    private boolean enabled;
    private static ItemConfigHelper itemsMap;
    private static ItemConfigHelper notWaterProofItems;

    public boolean fmlOverrideEnable;

    public PlayerSelfLightSource() {
        lightLevel = 0;
        enabled = false;
        lastWorld = null;
    }

    private LightConfig config;

    @Override
    public void afterVanillaBootstrap() {

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

    @Override
    public void clientTick(Minecraft client) {

        if (client == null || client.player == null || client.player.world == null) {
            return;
        }

        if (thePlayer != client.player || lastWorld != thePlayer.world) {
            thePlayer = client.player;
            lastWorld = thePlayer.world;
        }

        if (thePlayer.isAlive() && !DynamicLights.globalLightsOff()) {
            if (fmlOverrideEnable) {
                return;
            }
            int prevLight = lightLevel;

            ItemStack item = ItemStack.EMPTY;
            LOGGER.debug("checking for light from main hand item {}", thePlayer.getHeldItemMainhand());
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
            LOGGER.debug("Self light tick, main:{}, off:{}, light:{}, chosen itemstack:{}", main, off, lightLevel, item);

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
                        LOGGER.debug("Self light tick, water blocked light!");
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

    private boolean checkPlayerWater(EntityPlayer thePlayer) {
        if (thePlayer.isInWater()) {
            int x = MathHelper.floor(thePlayer.posX + 0.5D);
            int y = MathHelper.floor(thePlayer.posY + thePlayer.getEyeHeight());
            int z = MathHelper.floor(thePlayer.posZ + 0.5D);
            IBlockState is = thePlayer.world.getBlockState(new BlockPos(x, y, z));
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
