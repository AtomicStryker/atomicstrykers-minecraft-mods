package atomicstryker.findercompass.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import atomicstryker.findercompass.client.ClientPacketHandler;
import atomicstryker.findercompass.client.CompassSetting;
import atomicstryker.findercompass.client.FinderCompassClientTicker;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "FinderCompass", name = "Finder Compass", version = "1.6.4X")
@NetworkMod(
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "FindrCmps" }, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "FindrCmps" }, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class
)
public class FinderCompassMod
{

    @Instance(value = "FinderCompass")
    public static FinderCompassMod instance;

    private int itemID;
    public ArrayList<CompassSetting> settingList;
    
    public ItemFinderCompass compass;
    public boolean itemEnabled;

    public File compassConfig;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {

        settingList = new ArrayList<CompassSetting>();

        compassConfig = evt.getSuggestedConfigurationFile();
        String target = compassConfig.getAbsolutePath();
        DefaultConfigFilePrinter configurator = new DefaultConfigFilePrinter();
        File needleConfig = new File(target);
        if (!needleConfig.exists())
        {
            configurator.writeDefaultFile(needleConfig);
        }
        try
        {
            configurator.parseConfig(new BufferedReader(new FileReader(needleConfig)), settingList);
            System.out.println("Finder compass config fully parsed, loaded "+settingList.size()+" settings");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        Configuration itemConfig = new Configuration(new File(target.replace("FinderCompass", "FinderCompassItemConfig")));
        itemConfig.load();
        itemID = itemConfig.getItem("finderCompassID", 4356).getInt();
        itemEnabled = itemConfig.get(Configuration.CATEGORY_ITEM, "isFinderCompassNewItem", false).getBoolean(false);
        itemConfig.save();

        compass = (ItemFinderCompass) new ItemFinderCompass(itemID).setUnlocalizedName("Finder Compass");
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        LanguageRegistry.addName(compass, "Finder Compass");
        if (itemEnabled)
        {
            GameRegistry.addRecipe(new ItemStack(compass),
                    new Object[] { " # ", "#X#", " # ", Character.valueOf('#'), Item.diamond, Character.valueOf('X'), Item.compass });
        }

        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            TickRegistry.registerTickHandler(new FinderCompassClientTicker(), Side.CLIENT);
        }
    }
    
}
