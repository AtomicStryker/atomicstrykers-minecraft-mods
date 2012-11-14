package atomicstryker.dynamiclights.client.modules;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityCreeper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.event.FMLInitializationEvent;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to charging Creepers about to explode.
 * Those can give off Light through this Module.
 *
 */
@Mod(modid = "DynamicLights_creepers", name = "Dynamic Lights on Creepers", version = "1.0.2", dependencies = "after:DynamicLights")
public class ChargingCreeperLightSource
{
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @ForgeSubscribe
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
