package atomicstryker.battletowers.client.sound;

import java.io.File;
import java.net.URL;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class BattleTowerSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "/atomicstryker/battletowers/client/sound/";
    
    private final String[] soundFiles = {
            "golem.ogg",
            "golemawaken.ogg",
            "golemcharge.ogg",
            "golemdeath.ogg",
            "golemhurt1.ogg",
            "golemhurt2.ogg",
            "golemspecial1.ogg",
            "golemspecial2.ogg",
            "towerbreakstart.ogg",
            "towercrumble.ogg"
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
                System.out.println("Successfully loaded soundfile "+soundFile);
            }
            
            catch (Exception e)
            {
                System.err.println("Failed loading sound file: " + s);
                e.printStackTrace();
            }
        }
    }
}
