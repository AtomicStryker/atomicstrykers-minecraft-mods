package atomicstryker.battletowers.client;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BattleTowerSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "battletowers";

    private final String[] soundFiles = {
            "golem.ogg",
            "golemawaken.ogg",
            "golemcharge1.ogg",
            "golemcharge2.ogg",
            "golemdeath.ogg",
            "golemhurt1.ogg",
            "golemhurt2.ogg",
            "golemspecial1.ogg",
            "golemspecial2.ogg",
            "towerbreakstart.ogg",
            "towercrumble.ogg"
    };

    @SubscribeEvent
    public void onSoundLoad(SoundLoadEvent event)
    {
        System.out.println("SoundLoadEvent Battletowers, trying to load sounds");
        for (String soundFile : soundFiles)
        {
            try
            {
                /* first float is loudness, last int is distance to entity squared something ... im just registering here */
                event.manager.func_148599_a(new PositionedSoundRecord(new ResourceLocation(SOUND_RESOURCE_LOCATION, soundFile), 1.0f, 0, 0, 0, 0), 0);
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
