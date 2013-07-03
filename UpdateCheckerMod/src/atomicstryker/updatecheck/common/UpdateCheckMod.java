package atomicstryker.updatecheck.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.EnumSet;
import java.util.Map;

import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "AS_UpdateCheck", name = "AtomicStryker Update Check Mod", version = "1.0.8")
public class UpdateCheckMod
{
    private final String updateURL = "http://atomicstryker.net/updatemanager/modversions.txt";
    private final long worldLoadDelay = 10000L;
    private World lastWorld;
    
    private UpdateCheckThread thread;
    
    @SidedProxy(clientSide = "atomicstryker.updatecheck.client.UpdateCheckClient", serverSide = "atomicstryker.updatecheck.common.UpdateCheckServer")
    public static IProxy proxy;
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        lastWorld = null;
        TickRegistry.registerTickHandler(new TickHandler(), Side.SERVER);
    }
    
    private class TickHandler implements ITickHandler
    {
        private final EnumSet<TickType> tickTypes = EnumSet.of(TickType.WORLD);

        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            if (lastWorld != FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0))
            {
                lastWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
                thread = new UpdateCheckThread();
                thread.start();
            }
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

        @Override
        public String getLabel()
        {
            return "AS_UpdateCheck";
        }
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
                while ((curLine = reader.readLine()) != null)
                {
                    //System.out.println("Retrieved line from version file: "+curLine);
                    String[] tokens = curLine.split("=");
                    //System.out.println("Now checking mod: "+tokens[0].trim());
                    if ((curMod = modMap.get(tokens[0].trim())) != null)
                    {
                        if (!isLocalVersionUpToDate(curMod.getVersion(), tokens[1].trim()))
                        {
                            proxy.announce("A newer version of "+curMod.getName()+" is available: "+tokens[1].trim()+", visit atomicstryker.net to get it.");
                        }
                        else
                        {
                            System.out.println(curMod.getName()+" was found up to date by UpdateCheckThread");
                        }
                    }
                    else if (tokens[0].trim().equals("mcversion"))
                    {
                        if (!Loader.instance().getMCVersionString().equals(tokens[1].trim()))
                        {
                            System.out.println("According to website, current mcversion is: "+tokens[1].trim());
                            System.out.println("Since yours doesnt match that, Update Checker is aborting now.");
                            break;
                        }
                        else
                        {
                            System.out.println("Your mcversion is: "+tokens[1].trim()+" and current!");
                        }
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

            // if a web version is LONGER and the local version was equal up to it's
            // end, the web version must be newer
            if (webVersion.length() > localVersion.length() && !newer)
                return false;

            return true;
        }
    }

}
