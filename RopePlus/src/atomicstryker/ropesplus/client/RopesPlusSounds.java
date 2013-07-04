package atomicstryker.ropesplus.client;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class RopesPlusSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "ropesplus:";
    
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
    
    @ForgeSubscribe
    public void onSoundLoad(SoundLoadEvent event)
    {
        System.out.println("SoundLoadEvent RopesPlus, trying to load sounds");
        for (String soundFile : soundFiles)
        {
            String s = SOUND_RESOURCE_LOCATION+soundFile;
            try
            {
                event.manager.soundPoolSounds.addSound(soundFile);
                System.out.println("Successfully loaded soundfile " + soundFile);
            }
            
            catch (Exception e)
            {
                System.err.println("RopesPlus failed loading sound file: " + s);
                e.printStackTrace();
            }
        }
    }
}
