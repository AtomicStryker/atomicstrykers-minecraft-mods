package atomicstryker.findercompass.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.findercompass.client.CompassSetting;
import atomicstryker.findercompass.client.FinderCompassClientTicker;
import atomicstryker.findercompass.common.network.HandshakePacket;
import atomicstryker.findercompass.common.network.NetworkHelper;
import atomicstryker.findercompass.common.network.StrongholdPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "FinderCompass", name = "Finder Compass", version = "1.7.10a")
public class FinderCompassMod
{

    @Instance(value = "FinderCompass")
    public static FinderCompassMod instance;
    
    public ArrayList<CompassSetting> settingList;
    
    public ItemFinderCompass compass;
    public boolean itemEnabled;

    public File compassConfig;
    
    public NetworkHelper networkHelper;
    public SimpleNetworkWrapper networkWrapper;
    
    @NetworkCheckHandler
    public boolean checkModLists(Map<String,String> modList, Side side)
    {
        return true;
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        settingList = new ArrayList<CompassSetting>();

        compassConfig = evt.getSuggestedConfigurationFile();
        Configuration itemConfig = new Configuration(new File(compassConfig.getAbsolutePath().replace("FinderCompass", "FinderCompassItemConfig")));
        itemConfig.load();
        itemEnabled = itemConfig.get(Configuration.CATEGORY_GENERAL, "isFinderCompassNewItem", false).getBoolean(false);
        itemConfig.save();
        
        if (itemEnabled)
        {
            compass = (ItemFinderCompass) new ItemFinderCompass().setUnlocalizedName("finder_compass");
            GameRegistry.registerItem(compass, "finder_compass");
        }
        
        FMLCommonHandler.instance().bus().register(this);
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            FinderCompassClientTicker.instance = new FinderCompassClientTicker();
        }
        
        networkHelper = new NetworkHelper("AS_FC", HandshakePacket.class, StrongholdPacket.class);
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        networkHelper.sendPacketToPlayer(new HandshakePacket(), (EntityPlayerMP) event.player);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        if (itemEnabled)
        {
            GameRegistry.addRecipe(new ItemStack(compass),
                    new Object[] { " # ", "#X#", " # ", Character.valueOf('#'), Items.diamond, Character.valueOf('X'), Items.compass });
        }
        
        if (FinderCompassClientTicker.instance != null)
        {
            FinderCompassClientTicker.instance.onLoad();
        }
    }
    
    @EventHandler
    public void onModsLoaded(FMLPostInitializationEvent event)
    {
        DefaultConfigFilePrinter configurator = new DefaultConfigFilePrinter();
        File needleConfig = new File(compassConfig.getAbsolutePath());
        if (!needleConfig.exists())
        {
            configurator.writeDefaultFile(needleConfig);
        }
        try
        {
            configurator.parseConfig(new BufferedReader(new FileReader(needleConfig)), settingList);
            System.out.println("Finder compass config fully parsed, loaded "+settingList.size()+" settings");
            
            if (FinderCompassClientTicker.instance != null)
            {
                FinderCompassClientTicker.instance.switchSetting();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
}
