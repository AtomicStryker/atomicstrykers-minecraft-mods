package atomicstryker.updatecheck.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "AS_UpdateCheck", name = "AtomicStryker Update Check Mod", version = "1.1.7")
public class UpdateCheckMod
{
    private final String updateURL = "http://atomicstryker.net/updatemanager/modversions.txt";
    private final long worldLoadDelay = 10000L;
    private final HashSet<String> announcements = new HashSet<String>();
    private boolean announced = false;

    @SidedProxy(clientSide = "atomicstryker.updatecheck.client.UpdateCheckClient", serverSide = "atomicstryker.updatecheck.common.UpdateCheckServer")
    public static IProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        new UpdateCheckThread().start();
    }

    private class UpdateCheckThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                Thread.sleep(worldLoadDelay);
                
                Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
                ModContainer curMod;

                URL versionDataFile = new URL(updateURL);
                BufferedReader reader = new BufferedReader(new InputStreamReader(versionDataFile.openStream()));
                String curLine;
                boolean inCorrectMCVersionArea = false;
                while ((curLine = reader.readLine()) != null)
                {
                    // System.out.println("Retrieved line from version file: "+curLine);
                    String[] tokens = curLine.split("=");
                    // System.out.println("Now checking mod: "+tokens[0].trim());
                    if (tokens[0].trim().equals("mcversion"))
                    {
                        if (!Loader.instance().getMCVersionString().equals(tokens[1].trim()))
                        {
                            System.out.println("Now reading data segment for mismatching mcversion: " + tokens[1].trim());
                            inCorrectMCVersionArea = false;
                        }
                        else
                        {
                            System.out.println("Your mcversion is: " + tokens[1].trim() + " and matches the next Update Checker data segment");
                            inCorrectMCVersionArea = true;
                        }
                    }
                    else if (inCorrectMCVersionArea && (curMod = modMap.get(tokens[0].trim())) != null)
                    {
                        if (!isLocalVersionUpToDate(curMod.getVersion(), tokens[1].trim()))
                        {
                            announcements.add("A newer version of " + EnumChatFormatting.RED + curMod.getName() + EnumChatFormatting.RESET
                                    + " is available: " + tokens[1].trim() + ", visit " + EnumChatFormatting.GOLD + "atomicstryker.net"
                                    + EnumChatFormatting.RESET + " to get it.");
                        }
                        else
                        {
                            // System.out.println(curMod.getName() + " was found up to date by Update Checker");
                        }
                    }
                }
                
                if (!announced)
                {
                    announced = true;
                    for (String s : announcements)
                    {
                        proxy.announce(s);
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("UpdateCheckThread encountered an Exception, see following stacktrace:");
                e.printStackTrace();
            }
        }

        private boolean isLocalVersionUpToDate(String localVersion, String webVersion)
        {
            boolean newer = false;

            for (int i = 0; i < Math.min(localVersion.length(), webVersion.length()); i++)
            {
                int comparedchar = webVersion.substring(i, i + 1).compareTo(localVersion.substring(i, i + 1));

                // case: local version is not only equal but higher in a digit
                if (comparedchar < 0)
                    newer = true;

                // case: web version is higher, return false immediatly
                if (!newer && comparedchar > 0)
                    return false;
            }

            // if a web version is LONGER and the local version was equal up to
            // it's
            // end, the web version must be newer
            if (webVersion.length() > localVersion.length() && !newer)
                return false;

            return true;
        }
    }

}
