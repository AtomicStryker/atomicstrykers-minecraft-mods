package atomicstryker.magicyarn.common;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import atomicstryker.magicyarn.client.ClientPacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "MagicYarn", name = "Magic Yarn", version = "1.0.1")
@NetworkMod(clientSideRequired = false, serverSideRequired = false, connectionHandler = ConnectionHandler.class,
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "MagicYarn" }, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "MagicYarn" }, packetHandler = ServerPacketHandler.class))
public class MagicYarn implements IProxy
{
    public static MagicYarn instance;
    
    @SidedProxy(clientSide = "atomicstryker.magicyarn.client.MagicYarnClient", serverSide = "atomicstryker.magicyarn.common.MagicYarn")
    public static IProxy proxy;
    
	public static Item magicYarn;
	
    @PreInit
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        File cfile = evt.getSuggestedConfigurationFile();
        Configuration config = new Configuration(cfile);
        config.load();
        int itemID = config.getItem("magicyarnitem", 4734, "Item ID of Magic Yarn item").getInt();
        config.save();
        magicYarn = (new ItemMagicYarn(itemID)).setItemName("Magic Yarn");
        proxy.preInit(cfile);
    }
    
    @Override
    public void preInit(File configFile)
    {
        // NOOP
    }
	
    @Init
    public void load(FMLInitializationEvent evt)
    {	    
	    LanguageRegistry.instance().addName(magicYarn, "Magic Yarn");
	    
        GameRegistry.addRecipe(new ItemStack(magicYarn, 1), new Object[]{
            "###", "#X#", "###", Character.valueOf('X'), Item.compass, Character.valueOf('#'), Block.cloth
        });
    }

    @Override
    public void onPlayerUsedYarn(World world, EntityPlayer player, float timeButtonHeld)
    {
        // NOOP
    }

    @Override
    public void onConnectedToNewServer()
    {
        // NOOP
    }
    
}
