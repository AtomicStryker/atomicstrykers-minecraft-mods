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
        bat.setTexture("/atomicstryker/petbat/client/texture/petbat_glister.png");
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
            bat.setTexture("/atomicstryker/petbat/client/texture/petbat.png");
            return 0;
        }
        
        return 15;
    }

}
