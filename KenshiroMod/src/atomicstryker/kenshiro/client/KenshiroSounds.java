package atomicstryker.kenshiro.client;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class KenshiroSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "kenshiro:";

    private final String[] soundFiles = { "kenshirocharge.ogg", "kenshiroheartbeat.ogg", "kenshiropunch.ogg", "kenshiroshindeiru.ogg", "kenshirosmash.ogg", "kenshirostyle.ogg" };

    @ForgeSubscribe
    public void onSoundLoad(SoundLoadEvent event)
    {
        System.out.println("SoundLoadEvent Kenshiro, trying to load sounds");
        for (String soundFile : soundFiles)
        {
            String s = SOUND_RESOURCE_LOCATION + soundFile;
            try
            {
                event.manager.addSound(s);
                System.out.println("Successfully loaded soundfile " + s);
            }

            catch (Exception e)
            {
                System.err.println("Failed loading sound file: " + s);
                e.printStackTrace();
            }
        }
    }
}
