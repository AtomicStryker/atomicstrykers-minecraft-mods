package atomicstryker.dynamiclights.client.modules;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to EntityLiving instances, or rather their respective
 * armor and held Itemstacks. Lights up golden armor and torch Zombies
 *
 */
@Mod(modid = "DynamicLights_mobEquipment", name = "Dynamic Lights on Mob Equipment", version = "1.0.2", dependencies = "required-after:DynamicLights")
public class EntityLivingEquipmentLightSource
{
    private Minecraft mcinstance;
    private long nextUpdate;
    private long updateInterval;
    private ArrayList<EntityLightAdapter> trackedEntities;
    private Thread thread;
    private boolean threadRunning;
    private HashMap<Integer, Integer> itemsMap;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        
        Property updateI = config.get(Configuration.CATEGORY_GENERAL, "update Interval", 1000);
        updateI.comment = "Update Interval time for all EntityLiving in milliseconds. The lower the better and costlier.";
        updateInterval = updateI.getInt();
        
        itemsMap = new HashMap<Integer, Integer>();
        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "LightItems", "50:15,89:12,348:10,91:15,327:15,76:10,331:10,314:14,418:15");
        itemsList.comment = "Item and Armor IDs that shine light when found on any EntityLiving. Syntax: ItemID:LightValue, seperated by commas";
        String[] tokens = itemsList.getString().split(",");
        for (String pair : tokens)
        {
            String[] values = pair.split(":");
            int id = Integer.valueOf(values[0]);
            int value = Integer.valueOf(values[1]);
            itemsMap.put(id, value);
        }
        
        config.save();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        mcinstance = FMLClientHandler.instance().getClient();
        nextUpdate = System.currentTimeMillis();
        trackedEntities = new ArrayList<EntityLightAdapter>();
        threadRunning = false;
        TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
    }
    
    private int getEquipmentLightLevel(EntityLivingBase ent)
    {
        if (ent instanceof EntityHorse)
        {
            // Horse armor texture is the only thing "visible" on client, inventory is not synced.
            // Armor layer is at index 2 in texture layers
            String horseArmorTexture = ((EntityHorse)ent).getVariantTexturePaths()[2];
            if (horseArmorTexture.equals("textures/entity/horse/armor/horse_armor_gold.png"))
            {
                return getLightFromItemStack(new ItemStack(Item.horseArmorGold)); // horsearmorgold
            }
            if (horseArmorTexture.equals("textures/entity/horse/armor/horse_armor_iron.png"))
            {
                return getLightFromItemStack(new ItemStack(Item.horseArmorIron)); // horsearmormetal
            }
            if (horseArmorTexture.equals("textures/entity/horse/armor/horse_armor_diamond.png"))
            {
                return getLightFromItemStack(new ItemStack(Item.horseArmorDiamond)); // butt stallion
            }
        }
        
        return getMobEquipMaxLight(ent);
    }
    
    private int getMobEquipMaxLight(EntityLivingBase ent)
    {
        int light = getLightFromItemStack(ent.getCurrentItemOrArmor(0));
        for (int i = 1; i < ent.getLastActiveItems().length; i++)
        {
            light = Math.max(light, getLightFromItemStack(ent.getLastActiveItems()[i]));
        }
        return light;
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

        @SuppressWarnings("unchecked")
        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            if (mcinstance.theWorld != null && System.currentTimeMillis() > nextUpdate && !DynamicLights.globalLightsOff())
            {
                nextUpdate = System.currentTimeMillis() + updateInterval;
                
                if (!threadRunning)
                {
                    thread = new EntityListChecker(mcinstance.theWorld.loadedEntityList);
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                    threadRunning = true;
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
            return "DynamicLights_onFire";
        }
    }
    
    private class EntityListChecker extends Thread
    {
        private final Object[] list;
        
        public EntityListChecker(List<Entity> input)
        {
            list = input.toArray();
        }
        
        @Override
        public void run()
        {
            ArrayList<EntityLightAdapter> newList = new ArrayList<EntityLightAdapter>();
            
            Entity ent;
            for (Object o : list)
            {
                ent = (Entity) o;
                // Loop all loaded Entities, find alive and valid EntityLiving not otherwise handled
                if ((ent instanceof EntityLivingBase)
                        && ent.isEntityAlive() && !(ent instanceof EntityPlayer) && getEquipmentLightLevel((EntityLivingBase) ent) > 0)
                {
                    // now find them in the already tracked adapters
                    boolean found = false;
                    Iterator<EntityLightAdapter> iter = trackedEntities.iterator();
                    EntityLightAdapter adapter = null;
                    while (iter.hasNext())
                    {
                        adapter = iter.next();
                        if (adapter.getAttachmentEntity().equals(ent)) // already tracked!
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
                        adapter = new EntityLightAdapter((EntityLivingBase) ent);
                        adapter.onTick();
                        newList.add(adapter);
                    }
                }
            }
            // any remaining adapters were not targeted again, which probably means they dont burn anymore. The tick will finish them off.
            for (EntityLightAdapter adapter : trackedEntities)
            {
                adapter.onTick();
            }
            
            trackedEntities = newList;
            threadRunning = false;
        }
        
    }
    
    private class EntityLightAdapter implements IDynamicLightSource
    {
        
        private EntityLivingBase entity;
        private int lightLevel;
        private boolean enabled;
        
        public EntityLightAdapter(EntityLivingBase e)
        {
            lightLevel = 0;
            enabled = false;
            entity = e;
        }
        
        /**
         * Since they are IDynamicLightSource instances, they will already receive updates! Why do we need
         * to do this? Because seperate Thread!
         */
        public void onTick()
        {
            lightLevel = getMobEquipMaxLight(entity);
            
            // infernal mobs yay
            if (entity.getEntityData().getString("InfernalMobsMod") != null)
            {
                lightLevel = 15;
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
            return entity;
        }

        @Override
        public int getLightLevel()
        {
            return lightLevel;
        }
    }

}
