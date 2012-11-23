package atomicstryker.dynamiclights.client.modules;

import java.util.EnumSet;
import java.util.HashMap;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to the Player Entity itself.
 * Handheld Items and Armor can give off Light through this Module.
 *
 */
@Mod(modid = "DynamicLights_thePlayer", name = "Dynamic Lights Player Light", version = "1.0.2", dependencies = "required-after:DynamicLights")
public class PlayerSelfLightSource implements IDynamicLightSource
{
    private EntityPlayer thePlayer;
    private World lastWorld;
    private int lightLevel;
    private boolean enabled;
    private HashMap<Integer, Integer> itemsMap;
    
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        itemsMap = new HashMap<Integer, Integer>();
        Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        
        Property itemsList = config.get(config.CATEGORY_GENERAL, "LightItems", "50:15,89:12,348:10,91:15,327:15,76:10,331:10,314:14");
        itemsList.comment = "Item IDs that shine light while held. Armor Items also work when worn. [ONLY ON YOURSELF] Syntax: ItemID:LightValue, seperated by commas";
        
        String[] tokens = itemsList.value.split(",");
        for (String pair : tokens)
        {
            String[] values = pair.split(":");
            int id = Integer.valueOf(values[0]);
            int value = Integer.valueOf(values[1]);
            itemsMap.put(id, value);
        }
        
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
        private final EnumSet ticks;
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
            if (lastWorld != FMLClientHandler.instance().getClient().theWorld)
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
                
                lightLevel = getLightFromItemStack(thePlayer.getCurrentEquippedItem());
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
            Integer i = itemsMap.get(stack.itemID);
            if (i != null)
            {
                return i;
            }
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
