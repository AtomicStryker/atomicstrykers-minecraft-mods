package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author AtomicStryker
 * 
 *         Offers Dynamic Light functionality to charging Creepers about to
 *         explode. Those can give off Light through this Module.
 * 
 */
@Mod(modid = "dynamiclights_creepers", name = "Dynamic Lights on Creepers", version = "1.0.6", dependencies = "required-after:dynamiclights")
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
        if (event.getSound() != null && event.getSound().getSoundName().getResourcePath().equals("random.fuse") && event.getEntity() != null && event.getEntity() instanceof EntityCreeper)
        {
            if (event.getEntity().isEntityAlive())
            {
                DynamicLights.addLightSource(new EntityLightAdapter((EntityCreeper) event.getEntity()));
            }
        }
    }

    private class EntityLightAdapter implements IDynamicLightSource
    {
        private EntityCreeper entity;

        public EntityLightAdapter(EntityCreeper eC)
        {
            entity = eC;
        }

        @Override
        public Entity getAttachmentEntity()
        {
            return entity;
        }

        @Override
        public int getLightLevel()
        {
            return entity.getCreeperState() == 1 ? 15 : 0;
        }
    }

}
