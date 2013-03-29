package atomicstryker.findercompass.common;

import java.io.File;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import atomicstryker.findercompass.client.ClientPacketHandler;
import atomicstryker.findercompass.client.FinderCompassClientTicker;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "FinderCompass", name = "Finder Compass", version = "1.5.1B")
@NetworkMod(
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "FindrCmps" }, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "FindrCmps" }, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class FinderCompassMod
{
    private static File config;
    private int itemID;
    private ItemFinderCompass compass;
    public static boolean itemEnabled;
    
    public static File getConfigFile()
    {
        return config;
    }
    
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        config = evt.getSuggestedConfigurationFile();
        String target = config.getAbsolutePath();
        target = target.replace("FinderCompass", "FinderCompassItemConfig");
        Configuration c = new Configuration(new File(target));
        c.load();
        itemID = c.getItem("finderCompassID", 4356).getInt();
        itemEnabled = c.get(c.CATEGORY_ITEM, "isFinderCompassNewItem", false).getBoolean(false);
        c.save();
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            TickRegistry.registerTickHandler(new FinderCompassClientTicker(), Side.CLIENT);
        }
        
        // i need the Item even if it isn't craftable so MC sets up and updates the texture for it
        compass = (ItemFinderCompass) new ItemFinderCompass(itemID).setUnlocalizedName("Finder Compass");
        LanguageRegistry.addName(compass, "Finder Compass");
        
        if (itemEnabled)
        {
            compass.setCreativeTab(CreativeTabs.tabTools);
            GameRegistry.addRecipe(new ItemStack(compass), new Object[] {" # ", "#X#", " # ", Character.valueOf('#'), Item.diamond, Character.valueOf('X'), Item.compass});
        }
    }
}
