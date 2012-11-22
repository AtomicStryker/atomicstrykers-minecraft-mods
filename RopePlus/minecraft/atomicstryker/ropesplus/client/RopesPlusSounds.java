package atomicstryker.ropesplus.client;

import java.io.File;
import java.net.URL;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class RopesPlusSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "/atomicstryker/ropesplus/client/sound/";
    
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
        for (String soundFile : soundFiles)
        {
            String s = SOUND_RESOURCE_LOCATION+soundFile;
            try
            {
                URL url = this.getClass().getResource(s);
                event.manager.soundPoolSounds.addSound(soundFile, url);
            }
            
            catch (Exception e)
            {
                System.err.println("Failed loading sound file: " + s);
                e.printStackTrace();
            }
        }
    }
}
