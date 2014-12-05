package atomicstryker.dynamiclights.client.modules;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;

/**
 * 
 * @author AtomicStryker
 *
 * Offers Dynamic Light functionality to flame enchanted Arrows fired.
 * Those can give off Light through this Module.
 *
 */
@Mod(modid = "DynamicLights_flameArrows", name = "Dynamic Lights on Flame enchanted Arrows", version = "1.0.0", dependencies = "required-after:DynamicLights")
public class FlameEnchantedArrowLightSource
{
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityArrow)
        {
            EntityArrow arrow = (EntityArrow) event.entity;
            if (arrow.shootingEntity != null && arrow.shootingEntity instanceof EntityPlayer)
            {
                EntityPlayer shooter = (EntityPlayer) arrow.shootingEntity;
                if (EnchantmentHelper.getFireAspectModifier(shooter) != 0)
                {
                    DynamicLights.addLightSource(new EntityLightAdapter(arrow));
                }
            }
        }
    }
    
    private class EntityLightAdapter implements IDynamicLightSource
    {
        private EntityArrow entity;
        private int lightLevel;
        
        public EntityLightAdapter(EntityArrow entArrow)
        {
            lightLevel = 15;
            entity = entArrow;
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
