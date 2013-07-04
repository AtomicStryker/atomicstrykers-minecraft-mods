package atomicstryker.minions.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class MinionsSounds
{
    private static final String SOUND_RESOURCE_LOCATION = "assets/minions/sound";

    @ForgeSubscribe
    public void onSoundLoad(SoundLoadEvent event)
    {
        System.out.println("SoundLoadEvent Minions, trying to load sounds");
        
        for (ModContainer mco : Loader.instance().getModList())
        {
            if (mco.getModId().equals("AS_Minions"))
            {
                System.out.println("Found Minions file base: "+mco.getSource());
                File fileCandidate = new File(mco.getSource().toURI().resolve(SOUND_RESOURCE_LOCATION));
                if (fileCandidate.isDirectory())
                {
                    System.out.println("Directory detected! Iterating...");
                    for (String soundFile : fileCandidate.list())
                    {
                        String s = SOUND_RESOURCE_LOCATION + soundFile;
                        event.manager.soundPoolSounds.addSound(s);
                        System.out.println("loaded soundfile " + s);
                    }
                }
                else if (mco.getSource().isFile() && mco.getSource().getName().endsWith(".zip"))
                {
                    System.out.println("Zip file detected! Opening...");
                    try
                    {
                        FileInputStream input = new FileInputStream(mco.getSource());
                        ZipInputStream zis = new ZipInputStream(input);
                        ZipEntry ze;
                        String s;
                        while ((ze = zis.getNextEntry()) != null)
                        {
                            if (!ze.isDirectory())
                            {
                                s = ze.getName();
                                if (s != null
                                && s.length() > 0
                                && s.startsWith(SOUND_RESOURCE_LOCATION))
                                {
                                    event.manager.soundPoolSounds.addSound(s);
                                    System.out.println("loaded soundfile " + s);
                                }
                            }
                            zis.closeEntry();
                        }
                        zis.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }
}
