package atomicstryker.kenshiro.client;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class KenshiroSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "kenshiro";

    private final String[] soundFiles = { "kenshirocharge.ogg", "kenshiroheartbeat.ogg", "kenshiropunch.ogg", "kenshiroshindeiru.ogg", "kenshirosmash.ogg", "kenshirostyle.ogg" };

    @SubscribeEvent
    public void onSoundLoad(SoundLoadEvent event)
    {
        System.out.println("SoundLoadEvent Kenshiro, trying to load sounds");
        for (String soundFile : soundFiles)
        {
            try
            {
                event.manager.addDelayedSound(new PositionedSoundRecord(new ResourceLocation(SOUND_RESOURCE_LOCATION, soundFile), 1.0f, 0, 0, 0, 0), 0);
                System.out.println("Successfully loaded soundfile " + soundFile);
            }

            catch (Exception e)
            {
                System.err.println("Failed loading sound file: " + soundFile);
                e.printStackTrace();
            }
        }
    }
}
