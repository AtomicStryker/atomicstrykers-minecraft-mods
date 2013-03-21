package atomicstryker.findercompass.common;

import java.io.File;

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
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "FinderCompass", name = "Finder Compass", version = "1.5")
@NetworkMod(
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "FindrCmps" }, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "FindrCmps" }, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class FinderCompassMod
{
    private static File config;
    
    public static File getConfigFile()
    {
        return config;
    }
    
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        config = evt.getSuggestedConfigurationFile();
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            TickRegistry.registerTickHandler(new FinderCompassClientTicker(), Side.CLIENT);
        }
    }
}
