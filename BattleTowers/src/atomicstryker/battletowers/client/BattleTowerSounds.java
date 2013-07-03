package atomicstryker.battletowers.client;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class BattleTowerSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "/assets/battletowers/sound/";

    private final String[] soundFiles = { "golem.ogg", "golemawaken.ogg", "golemcharge1.ogg", "golemcharge2.ogg", "golemdeath.ogg", "golemhurt1.ogg", "golemhurt2.ogg", "golemspecial1.ogg",
            "golemspecial2.ogg", "towerbreakstart.ogg", "towercrumble.ogg" };

    @ForgeSubscribe
    public void onSoundLoad(SoundLoadEvent event)
    {
        System.out.println("SoundLoadEvent Battletowers, trying to load sounds");
        for (String soundFile : soundFiles)
        {
            String s = SOUND_RESOURCE_LOCATION + soundFile;
            try
            {
                event.manager.soundPoolSounds.addSound(soundFile);
                System.out.println("Successfully loaded soundfile " + soundFile);
            }

            catch (Exception e)
            {
                System.err.println("Failed loading sound file: " + s);
                e.printStackTrace();
            }
        }
    }
}
