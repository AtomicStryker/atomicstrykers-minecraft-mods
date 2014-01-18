package atomicstryker.ropesplus.client;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class RopesPlusSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "ropesplus";
    
    private final String[] soundFiles = {
            "hookshotfire.ogg",
            "hookshotpull.ogg",
            "jungleking.ogg",
            "ropetension1.ogg",
            "ropetension2.ogg",
            "ropetension3.ogg",
            "ropetension0.ogg",
            "zipline.ogg"
    };
    
    @SubscribeEvent
    public void onSoundLoad(SoundLoadEvent event)
    {
        System.out.println("SoundLoadEvent RopesPlus, trying to load sounds");
        for (String soundFile : soundFiles)
        {
            try
            {
                event.manager.func_148599_a(new PositionedSoundRecord(new ResourceLocation(SOUND_RESOURCE_LOCATION, soundFile), 1.0f, 0, 0, 0, 0), 0);
                System.out.println("Successfully loaded soundfile " + soundFile);
            }
            
            catch (Exception e)
            {
                System.err.println("RopesPlus failed loading sound file: " + soundFile);
                e.printStackTrace();
            }
        }
    }
}
