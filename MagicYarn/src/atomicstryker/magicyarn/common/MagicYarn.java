package atomicstryker.magicyarn.common;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import atomicstryker.magicyarn.client.ClientPacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "MagicYarn", name = "Magic Yarn", version = "1.0.7")
@NetworkMod(clientSideRequired = false, serverSideRequired = false, connectionHandler = ConnectionHandler.class,
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "MagicYarn" }, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "MagicYarn" }, packetHandler = ServerPacketHandler.class))
public class MagicYarn implements IProxy
{
    public static MagicYarn instance;
    
    @SidedProxy(clientSide = "atomicstryker.magicyarn.client.MagicYarnClient", serverSide = "atomicstryker.magicyarn.common.MagicYarn")
    public static IProxy proxy;
    
	public static Item magicYarn;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        File cfile = evt.getSuggestedConfigurationFile();
        Configuration config = new Configuration(cfile);
        config.load();
        int itemID = config.getItem("magicyarnitem", 4734, "Item ID of Magic Yarn item").getInt();
        config.save();
        magicYarn = (new ItemMagicYarn(itemID)).setUnlocalizedName("Magic Yarn");
        magicYarn.setCreativeTab(CreativeTabs.tabTools);
        proxy.preInit(cfile);
    }
    
    @Override
    public void preInit(File configFile)
    {
        // NOOP
    }
	
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        LanguageRegistry.addName(magicYarn, "Magic Yarn");
	    
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