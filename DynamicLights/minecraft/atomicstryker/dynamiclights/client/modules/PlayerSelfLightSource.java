package atomicstryker.dynamiclights.client.modules;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to the Player Entity itself.
 * Handheld Items and Armor can give off Light through this Module.
 *
 */
@Mod(modid = "DynamicLights_thePlayer", name = "Dynamic Lights Player Light", version = "1.0.8", dependencies = "required-after:DynamicLights")
public class PlayerSelfLightSource implements IDynamicLightSource
{
    private EntityPlayer thePlayer;
    private World lastWorld;
    private int lightLevel;
    private boolean enabled;
    private ItemConfigHelper itemsMap;
    private ItemConfigHelper notWaterProofItems;
    
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        
        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "LightItems", "50,89=12,348=10,91,327,76=10,331=10,314=14");
        itemsList.comment = "Item IDs that shine light while held. Armor Items also work when worn. [ONLY ON YOURSELF]";
        itemsMap = new ItemConfigHelper(itemsList.getString(), 15);
        
        Property notWaterProofList = config.get(Configuration.CATEGORY_GENERAL, "TurnedOffByWaterItems", "50,327");
        notWaterProofList.comment = "Item IDs that do not shine light when held in water, have to be present in LightItems.";
        notWaterProofItems = new ItemConfigHelper(notWaterProofList.getString(), 1);
        
        config.save();
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        lightLevel = 0;
        enabled = false;
        lastWorld = null;
        
        TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
    }
    
    private class TickHandler implements ITickHandler
    {
        private final EnumSet<TickType> ticks;
        public TickHandler()
        {
            ticks = EnumSet.of(TickType.CLIENT);
        }

        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
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
                        && notWaterProofItems.retrieveValue(item.itemID, item.getItemDamage()) == 1)
                        {
                            lightLevel = 0;
                            
                            for (ItemStack armor : thePlayer.inventory.armorInventory)
                            {
                                if (armor != null && notWaterProofItems.retrieveValue(armor.itemID, item.getItemDamage()) == 0)
                                {
                                    lightLevel = Math.max(lightLevel, getLightFromItemStack(armor));
                                }
                            }
                        }
                    }
                }
                
                if (!enabled && lightLevel > 8)
                {
                    enableLight();
                }
                else if (enabled && lightLevel < 9)
                {
                    disableLight();
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
                return thePlayer.worldObj.getBlockMaterial(x, y, z) == Material.water;
            }
            return false;
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return ticks;
        }

        @Override
        public String getLabel()
        {
            return "DynamicLights_thePlayer";
        }
    }
    
    private int getLightFromItemStack(ItemStack stack)
    {
        if (stack != null)
        {
            int r = itemsMap.retrieveValue(stack.itemID, stack.getItemDamage());
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
        DynamicLights.removeLightSource(this);
        enabled = false;
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
