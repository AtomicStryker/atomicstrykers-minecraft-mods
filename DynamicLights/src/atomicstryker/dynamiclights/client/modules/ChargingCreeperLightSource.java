package atomicstryker.dynamiclights.client.modules;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to charging Creepers about to explode.
 * Those can give off Light through this Module.
 *
 */
@Mod(modid = "DynamicLights_creepers", name = "Dynamic Lights on Creepers", version = "1.0.3", dependencies = "required-after:DynamicLights")
public class ChargingCreeperLightSource
{
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event)
    {
        if (event.name != null
        && event.name.equals("random.fuse")
        && event.entity != null
        && event.entity instanceof EntityCreeper)
        {
            if (event.entity.isEntityAlive())
            {
                EntityLightAdapter adapter = new EntityLightAdapter((EntityCreeper) event.entity);
                adapter.onTick();
            }
        }
    }
    
    private class EntityLightAdapter implements IDynamicLightSource
    {
        
        private EntityCreeper entity;
        private int lightLevel;
        private boolean enabled;
        
        public EntityLightAdapter(EntityCreeper eC)
        {
            lightLevel = 15;
            enabled = false;
            entity = eC;
        }
        
        public void onTick()
        {
            lightLevel = entity.getCreeperState() == 1 ? 15 : 0;
            
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
