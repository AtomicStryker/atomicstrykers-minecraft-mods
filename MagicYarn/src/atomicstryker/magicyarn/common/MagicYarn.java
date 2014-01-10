package atomicstryker.magicyarn.common;

import java.io.File;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import atomicstryker.magicyarn.client.ClientPacketHandler;
import atomicstryker.magicyarn.common.network.PacketDispatcher;
import atomicstryker.magicyarn.common.network.PacketWrapper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "MagicYarn", name = "Magic Yarn", version = "1.1.0")
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
        PacketDispatcher.init("AS_MY", new ClientPacketHandler(), new ServerPacketHandler());
        
        magicYarn = (new ItemMagicYarn()).setUnlocalizedName("Magic Yarn");
        magicYarn.setCreativeTab(CreativeTabs.tabTools);
        
        proxy.preInit(evt.getSuggestedConfigurationFile());
        
        MinecraftForge.EVENT_BUS.register(this);
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
            "###", "#X#", "###", Character.valueOf('X'), Items.compass, Character.valueOf('#'), Blocks.wool
        });
    }
    
    @SubscribeEvent
    public void onEntityJoinsWorld(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            PacketDispatcher.sendPacketToPlayer(PacketWrapper.createPacket(1, null), (EntityPlayer) event.entity);
        }
    }

    @Override
    public void onPlayerUsedYarn(World world, EntityPlayer player, float timeButtonHeld)
    {
        // NOOP
    }
    
}
