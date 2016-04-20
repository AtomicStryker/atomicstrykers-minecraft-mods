package atomicstryker.findercompass.common;

import atomicstryker.findercompass.client.CompassSetting;
import atomicstryker.findercompass.client.FinderCompassClientTicker;
import atomicstryker.findercompass.common.network.HandshakePacket;
import atomicstryker.findercompass.common.network.NetworkHelper;
import atomicstryker.findercompass.common.network.StrongholdPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

@Mod(modid = "FinderCompass", name = "Finder Compass", version = "1.9")
public class FinderCompassMod
{

    @Instance(value = "FinderCompass")
    public static FinderCompassMod instance;
    
    public ArrayList<CompassSetting> settingList;
    
    public ItemFinderCompass compass;
    public boolean itemEnabled;

    public File compassConfig;
    
    public NetworkHelper networkHelper;

    @NetworkCheckHandler
    public boolean checkModLists(Map<String,String> modList, Side side)
    {
        return true;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        settingList = new ArrayList<>();

        compassConfig = evt.getSuggestedConfigurationFile();
        Configuration itemConfig = new Configuration(new File(compassConfig.getAbsolutePath().replace("FinderCompass", "FinderCompassItemConfig")));
        itemConfig.load();
        itemEnabled = itemConfig.get(Configuration.CATEGORY_GENERAL, "isFinderCompassNewItem", false).getBoolean(false);
        itemConfig.save();
        
        if (itemEnabled)
        {
            compass = (ItemFinderCompass) new ItemFinderCompass().setUnlocalizedName("finder_compass");
            GameRegistry.register(compass);
        }
        
        MinecraftForge.EVENT_BUS.register(this);
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            FinderCompassClientTicker.instance = new FinderCompassClientTicker();
        }
        
        networkHelper = new NetworkHelper("AS_FC", HandshakePacket.class, StrongholdPacket.class);
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        networkHelper.sendPacketToPlayer(new HandshakePacket("server"), (EntityPlayerMP) event.player);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        if (itemEnabled)
        {
            GameRegistry.addRecipe(new ItemStack(compass),
                    " # ", "#X#", " # ", '#', Items.DIAMOND, 'X', Items.COMPASS);
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
