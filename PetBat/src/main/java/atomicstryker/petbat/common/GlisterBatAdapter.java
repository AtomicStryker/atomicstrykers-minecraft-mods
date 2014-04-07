package atomicstryker.petbat.common;

import net.minecraft.entity.Entity;
import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;

public class GlisterBatAdapter implements IDynamicLightSource
{
    private final EntityPetBat bat;
    private long lastsUntil;
    
    public GlisterBatAdapter(EntityPetBat attachmentBat)
    {
        bat = attachmentBat;
        lastsUntil = System.currentTimeMillis() + PetBatMod.instance().glisterBatEffectDuration;
        attachmentBat.setGlistering(true);
        DynamicLights.addLightSource(this);
    }

    @Override
    public Entity getAttachmentEntity()
    {
        return bat;
    }

    @Override
    public int getLightLevel()
    {
        if (System.currentTimeMillis() > lastsUntil)
        {
            bat.setGlistering(false);
            return 0;
        }
        
        return 15;
    }

}
