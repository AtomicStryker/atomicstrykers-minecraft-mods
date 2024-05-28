package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.List;

/**
 * @author AtomicStryker
 * <p>
 * Offers Dynamic Light functionality to the Player Entity itself.
 * Handheld Items and Armor can give off Light through this Module.
 * <p>
 * With version 1.1.3 and later you can also use FMLIntercomms to use this
 * and have the player shine light. Like so:
 * <p>
 * FMLInterModComms.sendRuntimeMessage(sourceMod, "DynamicLights_thePlayer", "forceplayerlighton", "");
 * FMLInterModComms.sendRuntimeMessage(sourceMod, "DynamicLights_thePlayer", "forceplayerlightoff", "");
 * <p>
 * Note you have to track this yourself. Dynamic Lights will accept and obey, but not recover should you
 * get stuck in the on or off state inside your own code. It will not revert to off on its own.
 */
@Mod(modid = "DynamicLights_thePlayer", name = "Dynamic Lights Player Light", version = "1.1.5", dependencies = "required-after:DynamicLights")
public class PlayerSelfLightSource implements IDynamicLightSource {
    private EntityPlayer thePlayer;
    private World lastWorld;
    private int lightLevel;
    private boolean enabled;
    private ItemConfigHelper itemsMap;
    private ItemConfigHelper notWaterProofItems;
    private Configuration config;

    public boolean fmlOverrideEnable;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        config = new Configuration(evt.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(this);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt) {
        lightLevel = 0;
        enabled = false;
        lastWorld = null;
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt) {
        config.load();

        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "LightItems", "torch,glowstone=12,glowstone_dust=10,lit_pumpkin,lava_bucket,redstone_torch=10,redstone=10,golden_helmet=14,golden_horse_armor=15,easycoloredlights:easycoloredlightsCLStone=-1");
        itemsList.comment = "Item IDs that shine light while held. Armor Items also work when worn. [ONLY ON YOURSELF]";
        itemsMap = new ItemConfigHelper(itemsList.getString(), 15);

        Property notWaterProofList = config.get(Configuration.CATEGORY_GENERAL, "TurnedOffByWaterItems", "torch,lava_bucket");
        notWaterProofList.comment = "Item IDs that do not shine light when held in water, have to be present in LightItems.";
        notWaterProofItems = new ItemConfigHelper(notWaterProofList.getString(), 1);

        config.save();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (lastWorld != FMLClientHandler.instance().getClient().theWorld || thePlayer != FMLClientHandler.instance().getClient().thePlayer) {
            thePlayer = FMLClientHandler.instance().getClient().thePlayer;
            if (thePlayer != null) {
                lastWorld = thePlayer.worldObj;
            } else {
                lastWorld = null;
            }
        }

        if (thePlayer != null && thePlayer.isEntityAlive() && !DynamicLights.globalLightsOff()) {
            List<IMCMessage> messages = FMLInterModComms.fetchRuntimeMessages(this);
            if (!messages.isEmpty()) {
                // just get the last one
                IMCMessage imcMessage = messages.get(messages.size() - 1);
                if (imcMessage.key.equalsIgnoreCase("forceplayerlighton")) {
                    if (!fmlOverrideEnable) {
                        fmlOverrideEnable = true;
                        if (!enabled) {
                            lightLevel = 15;
                            enableLight();
                        }
                    }
                } else if (imcMessage.key.equalsIgnoreCase("forceplayerlightoff")) {
                    if (fmlOverrideEnable) {
                        fmlOverrideEnable = false;
                        if (enabled) {
                            disableLight();
                        }
                    }
                }
            }

            if (!fmlOverrideEnable) {

                ItemStack item = thePlayer.getCurrentEquippedItem();
                // check light from held item
                lightLevel = itemsMap.getLightFromItemStack(item);
                // turn that off if held item light is not waterproof and in water
                if (item != null && lightLevel > 0 && checkPlayerWater(thePlayer)
                        && notWaterProofItems.retrieveValue(GameData.getItemRegistry().getNameForObject(item.getItem()), item.getItemDamage()) == 1) {
                    lightLevel = 0;
                }

                // check if some armor piece has more light
                for (ItemStack armor : thePlayer.inventory.armorInventory) {
                    lightLevel = DynamicLights.maxLight(lightLevel, itemsMap.getLightFromItemStack(armor));
                }
                // if player is burning, override to max light
                if (thePlayer.isBurning()) {
                    lightLevel = 15;
                }

                if (!enabled && lightLevel > 0) {
                    enableLight();
                } else if (enabled && lightLevel < 1) {
                    disableLight();
                }
            }
        }
    }

    private boolean checkPlayerWater(EntityPlayer thePlayer) {
        if (thePlayer.isInWater()) {
            int x = MathHelper.floor_double(thePlayer.posX + 0.5D);
            int y = MathHelper.floor_double(thePlayer.posY + thePlayer.getEyeHeight());
            int z = MathHelper.floor_double(thePlayer.posZ + 0.5D);
            return thePlayer.worldObj.getBlock(x, y, z).getMaterial() == Material.water;
        }
        return false;
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
