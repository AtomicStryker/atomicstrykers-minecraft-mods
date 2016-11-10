package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import atomicstryker.dynamiclights.client.ItemConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to EntityLiving instances, or rather their respective
 * armor and held Itemstacks. Lights up golden armor and torch Zombies
 *
 */
@Mod(modid = "dynamiclights_mobequipment", name = "Dynamic Lights on Mob Equipment", version = "1.1.0", dependencies = "required-after:dynamiclights")
public class EntityLivingEquipmentLightSource
{
    private Minecraft mcinstance;
    private long nextUpdate;
    private long updateInterval;
    private ArrayList<EntityLightAdapter> trackedEntities;
    private boolean threadRunning;
    private ItemConfigHelper itemsMap;
    private Configuration config;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        config = new Configuration(evt.getSuggestedConfigurationFile());        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        mcinstance = FMLClientHandler.instance().getClient();
        nextUpdate = System.currentTimeMillis();
        trackedEntities = new ArrayList<>();
        threadRunning = false;
    }
    
    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        config.load();
        
        Property updateI = config.get(Configuration.CATEGORY_GENERAL, "update Interval", 1000);
        updateI.setComment("Update Interval time for all EntityLiving in milliseconds. The lower the better and costlier.");
        updateInterval = updateI.getInt();
        
        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "LightItems", "torch,glowstone=12,glowstone_dust=10,lit_pumpkin,lava_bucket,redstone_torch=10,redstone=10,golden_helmet=14");
        itemsList.setComment("Item and Armor IDs that shine light when found on any EntityLiving. Syntax: ItemID:LightValue, seperated by commas");
        itemsMap = new ItemConfigHelper(itemsList.getString(), 15);
        
        config.save();
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (mcinstance.theWorld != null && System.currentTimeMillis() > nextUpdate && !DynamicLights.globalLightsOff())
        {
            nextUpdate = System.currentTimeMillis() + updateInterval;
            
            if (!threadRunning)
            {
                Thread thread = new EntityListChecker(mcinstance.theWorld.loadedEntityList);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
                threadRunning = true;
            }
        }
    }
    
    private int getEquipmentLightLevel(EntityLivingBase ent)
    {
        if (ent instanceof EntityHorse)
        {
            // Horse armor texture is the only thing "visible" on client, inventory is not synced.
            // Armor layer is at index 2 in texture layers
            String horseArmorTexture = ((EntityHorse)ent).getVariantTexturePaths()[2];
            if (horseArmorTexture != null)
            {
                if (horseArmorTexture.equals("textures/entity/horse/armor/horse_armor_gold.png"))
                {
                    return getLightFromItemStack(new ItemStack(Items.GOLDEN_HORSE_ARMOR)); // horsearmorgold
                }
                if (horseArmorTexture.equals("textures/entity/horse/armor/horse_armor_iron.png"))
                {
                    return getLightFromItemStack(new ItemStack(Items.IRON_HORSE_ARMOR)); // horsearmormetal
                }
                if (horseArmorTexture.equals("textures/entity/horse/armor/horse_armor_diamond.png"))
                {
                    return getLightFromItemStack(new ItemStack(Items.DIAMOND_HORSE_ARMOR)); // butt stallion
                }
            }
        }
        
        return getMobEquipMaxLight(ent);
    }
    
    private int getMobEquipMaxLight(EntityLivingBase ent)
    {
        int light = 0;
        for (EntityEquipmentSlot ees : EntityEquipmentSlot.values())
        {
            light = Math.max(light, getLightFromItemStack(ent.getItemStackFromSlot(ees)));
        }
        return light;
    }

    private int getLightFromItemStack(ItemStack stack)
    {
        if (stack != null)
        {
            int r = itemsMap.retrieveValue(stack.getItem().getRegistryName(), stack.getItemDamage());
            return r < 0 ? 0 : r;
        }
        return 0;
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
            ArrayList<EntityLightAdapter> newList = new ArrayList<>();
            
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
                    EntityLightAdapter adapter;
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
            trackedEntities.forEach(EntityLightAdapter::onTick);
            
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
            
            if (!enabled && lightLevel > 0)
            {
                enableLight();
            }
            else if (enabled && lightLevel < 1)
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
