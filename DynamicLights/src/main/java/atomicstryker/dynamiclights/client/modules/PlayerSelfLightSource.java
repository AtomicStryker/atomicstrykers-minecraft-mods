package atomicstryker.dynamiclights.client.modules;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameData;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to the Player Entity itself.
 * Handheld Items and Armor can give off Light through this Module.
 * 
 * With version 1.1.3 and later you can also use FMLIntercomms to use this 
 * and have the player shine light. Like so:
 * 
 * FMLInterModComms.sendRuntimeMessage(sourceMod, "DynamicLights_thePlayer", "forceplayerlighton", "");
 * FMLInterModComms.sendRuntimeMessage(sourceMod, "DynamicLights_thePlayer", "forceplayerlightoff", "");
 * 
 * Note you have to track this yourself. Dynamic Lights will accept and obey, but not recover should you
 * get stuck in the on or off state inside your own code. It will not revert to off on its own.
 *
 */
@Mod(modid = "DynamicLights_thePlayer", name = "Dynamic Lights Player Light", version = "1.1.3", dependencies = "required-after:DynamicLights")
public class PlayerSelfLightSource implements IDynamicLightSource
{
    private EntityPlayer thePlayer;
    private World lastWorld;
    private int lightLevel;
    private boolean enabled;
    private ItemConfigHelper itemsMap;
    private ItemConfigHelper notWaterProofItems;
    private Configuration config;
    
    public boolean fmlOverrideEnable;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        config = new Configuration(evt.getSuggestedConfigurationFile());        
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        lightLevel = 0;
        enabled = false;
        lastWorld = null;
    }
    
    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        config.load();
        
        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "LightItems", "torch,glowstone=12,glowstone_dust=10,lit_pumpkin,lava_bucket,redstone_torch=10,redstone=10,golden_helmet=14,golden_horse_armor=15");
        itemsList.comment = "Item IDs that shine light while held. Armor Items also work when worn. [ONLY ON YOURSELF]";
        itemsMap = new ItemConfigHelper(itemsList.getString(), 15);
        
        Property notWaterProofList = config.get(Configuration.CATEGORY_GENERAL, "TurnedOffByWaterItems", "torch,lava_bucket");
        notWaterProofList.comment = "Item IDs that do not shine light when held in water, have to be present in LightItems.";
        notWaterProofItems = new ItemConfigHelper(notWaterProofList.getString(), 1);
        
        config.save();
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (lastWorld != FMLClientHandler.instance().getClient().theWorld || thePlayer != FMLClientHandler.instance().getClient().thePlayer)
        {
            thePlayer = FMLClientHandler.instance().getClient().thePlayer;
            if (thePlayer != null)
            {
                lastWorld = thePlayer.worldObj;
            }
            else
            {
                lastWorld = null;
            }
        }
        
        if (thePlayer != null && thePlayer.isEntityAlive() && !DynamicLights.globalLightsOff())
        {
            List<IMCMessage> messages = FMLInterModComms.fetchRuntimeMessages(this);
            if (messages.size() > 0)
            {
                // just get the last one
                IMCMessage imcMessage = messages.get(messages.size()-1);
                if (imcMessage.key.equalsIgnoreCase("forceplayerlighton"))
                {
                    if (!fmlOverrideEnable)
                    {
                        fmlOverrideEnable = true;
                        if (!enabled)
                        {
                            lightLevel = 15;
                            enableLight();
                        }
                    }
                }
                else if (imcMessage.key.equalsIgnoreCase("forceplayerlightoff"))
                {
                    if (fmlOverrideEnable)
                    {
                        fmlOverrideEnable = false;
                        if (enabled)
                        {
                            disableLight();
                        }
                    }
                }
            }
            
            if (!fmlOverrideEnable)
            {
                int prevLight = lightLevel;
                
                ItemStack item = thePlayer.getCurrentEquippedItem();
                lightLevel = getLightFromItemStack(item);
                
                for (ItemStack armor : thePlayer.inventory.armorInventory)
                {
                    lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
                }
                
                if (prevLight != 0 && lightLevel != prevLight)
                {
                    lightLevel = 0;
                }
                else
                {
                    if (thePlayer.isBurning())
                    {
                        lightLevel = 15;
                    }
                    else
                    {
                        if (checkPlayerWater(thePlayer)
                        && item != null
                        && notWaterProofItems.retrieveValue(GameData.getItemRegistry().getNameForObject(item.getItem()), item.getItemDamage()) == 1)
                        {
                            lightLevel = 0;
                            
                            for (ItemStack armor : thePlayer.inventory.armorInventory)
                            {
                                if (armor != null && notWaterProofItems.retrieveValue(GameData.getItemRegistry().getNameForObject(armor.getItem()), item.getItemDamage()) == 0)
                                {
                                    lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
                                }
                            }
                        }
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
        }
    }
    
    private boolean checkPlayerWater(EntityPlayer thePlayer)
    {
        if (thePlayer.isInWater())
        {
            int x = MathHelper.floor_double(thePlayer.posX + 0.5D);
            int y = MathHelper.floor_double(thePlayer.posY + thePlayer.getEyeHeight());
            int z = MathHelper.floor_double(thePlayer.posZ + 0.5D);
            return thePlayer.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock().getMaterial() == Material.water;
        }
        return false;
    }
    
    private int getLightFromItemStack(ItemStack stack)
    {
        if (stack != null)
        {
            int r = itemsMap.retrieveValue(GameData.getItemRegistry().getNameForObject(stack.getItem()), stack.getItemDamage());
            return r < 0 ? 0 : r;
        }
        return 0;
    }
    
    private void enableLight()
    {
        DynamicLights.addLightSource(this);
        enabled = true;
    }
    
    private void disableLight()
    {
        if (!fmlOverrideEnable)
        {
            DynamicLights.removeLightSource(this);
            enabled = false;
        }
    }

    @Override
    public Entity getAttachmentEntity()
    {
        return thePlayer;
    }

    @Override
    public int getLightLevel()
    {
        return lightLevel;
    }

}
