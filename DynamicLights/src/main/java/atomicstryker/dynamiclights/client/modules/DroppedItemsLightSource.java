package atomicstryker.dynamiclights.client.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
 * Offers Dynamic Light functionality to EntityItem instances.
 * Dropped Torches and such can give off Light through this Module.
 *
 */
@Mod(modid = "DynamicLights_dropItems", name = "Dynamic Lights on ItemEntities", version = "1.0.8", dependencies = "required-after:DynamicLights")
public class DroppedItemsLightSource
{
    private Minecraft mcinstance;
    private long nextUpdate;
    private long updateInterval;
    private ArrayList<EntityItemAdapter> trackedItems;
    private Thread thread;
    private boolean threadRunning;
    private Configuration config;
    
    private ItemConfigHelper itemsMap;
    private ItemConfigHelper notWaterProofItems;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        config = new Configuration(evt.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        mcinstance = FMLClientHandler.instance().getClient();
        nextUpdate = System.currentTimeMillis();
        trackedItems = new ArrayList<EntityItemAdapter>();
        threadRunning = false;
    }
    
    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        config.load();
        
        Property itemsList = config.get(Configuration.CATEGORY_GENERAL, "LightItems", "torch,glowstone=12,glowstone_dust=10,lit_pumpkin,lava_bucket,redstone_torch=10,redstone=10,golden_helmet=14");
        itemsList.comment = "Item IDs that shine light when dropped in the World.";
        itemsMap = new ItemConfigHelper(itemsList.getString(), 15);
        
        Property updateI = config.get(Configuration.CATEGORY_GENERAL, "update Interval", 1000);
        updateI.comment = "Update Interval time for all Item entities in milliseconds. The lower the better and costlier.";
        updateInterval = updateI.getInt();
        
        Property notWaterProofList = config.get(Configuration.CATEGORY_GENERAL, "TurnedOffByWaterItems", "torch,lava_bucket");
        notWaterProofList.comment = "Item IDs that do not shine light when dropped and in water, have to be present in LightItems.";
        notWaterProofItems = new ItemConfigHelper(notWaterProofList.getString(), 1);
        
        config.save();
    }
    
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
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
    
    private int getLightFromItemStack(ItemStack stack)
    {
        if (stack != null)
        {
            int r = itemsMap.retrieveValue(GameData.getItemRegistry().getNameForObject(stack.getItem()), stack.getItemDamage());
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
            ArrayList<EntityItemAdapter> newList = new ArrayList<EntityItemAdapter>();
            
            Entity ent;
            for (Object o : list)
            {
                ent = (Entity) o;
                // Loop all loaded Entities, find alive and valid ItemEntities
                if (ent instanceof EntityItem && ent.isEntityAlive())
                {
                    // now find them in the already tracked item adapters
                    boolean found = false;
                    Iterator<EntityItemAdapter> iter = trackedItems.iterator();
                    EntityItemAdapter adapter = null;
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
                        adapter = new EntityItemAdapter((EntityItem) ent);
                        adapter.onTick();
                        newList.add(adapter);
                    }
                }
            }
            // any remaining adapters were not in the loaded entities. The main Dynamic Lights mod will kill them.
            trackedItems = newList;
            threadRunning = false;
        }
        
    }
    
    private class EntityItemAdapter implements IDynamicLightSource
    {
        
        private EntityItem entity;
        private int lightLevel;
        private boolean enabled;
        private boolean notWaterProof;
        
        public EntityItemAdapter(EntityItem eI)
        {
            lightLevel = 0;
            enabled = false;
            entity = eI;
            notWaterProof = notWaterProofItems.retrieveValue(GameData.getItemRegistry().getNameForObject(eI.getEntityItem()), eI.getEntityItem().getItemDamage()) == 1;
        }
        
        /**
         * Since they are IDynamicLightSource instances, they will already receive updates! Why do we need
         * to do this? Because seperate Thread!
         */
        public void onTick()
        {
            if (entity.isBurning())
            {
                lightLevel = 15;
            }
            else
            {
                lightLevel = getLightFromItemStack(entity.getEntityItem());
                
                if (notWaterProof
                && entity.worldObj.getBlockState(
                		new BlockPos(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)))
                		.getBlock().getMaterial() == Material.water)
                {
                    lightLevel = 0;
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
            return (notWaterProof && entity.isInWater()) ? 0 : lightLevel;
        }
    }

}
