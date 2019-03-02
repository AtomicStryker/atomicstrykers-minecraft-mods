package atomicstryker.dynamiclights.client.modules;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 
 * @author AtomicStryker
 * 
 *         Offers Dynamic Light functionality to charging Creepers about to
 *         explode. Those can give off Light through this Module.
 * 
 */
@Mod(ChargingCreeperLightSource.MOD_ID)
@Mod.EventBusSubscriber(modid = ChargingCreeperLightSource.MOD_ID, value = Dist.CLIENT)
public class ChargingCreeperLightSource
{
    static final String MOD_ID = "dynamiclights_chargingcreeper";

    @SubscribeEvent
    public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event)
    {
        if (event.getSound() != null && event.getSound().getName().getPath().equals("random.fuse") && event.getEntity() != null && event.getEntity() instanceof EntityCreeper)
        {
            if (event.getEntity().isAlive())
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
