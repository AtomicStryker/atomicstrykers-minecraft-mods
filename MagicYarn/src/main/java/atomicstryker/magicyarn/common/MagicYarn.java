package atomicstryker.magicyarn.common;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import atomicstryker.magicyarn.common.network.HandshakePacket;
import atomicstryker.magicyarn.common.network.NetworkHelper;
import atomicstryker.magicyarn.common.network.PathPacket;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "MagicYarn", name = "Magic Yarn", version = "1.1.5")
public class MagicYarn implements IProxy
{
    
    @Instance("MagicYarn")
    public static MagicYarn instance;
    
    public NetworkHelper networkHelper;
    
    @SidedProxy(clientSide = "atomicstryker.magicyarn.client.MagicYarnClient", serverSide = "atomicstryker.magicyarn.common.MagicYarn")
    public static IProxy proxy;
    
	public static Item magicYarn;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        networkHelper = new NetworkHelper("AS_MY", HandshakePacket.class, PathPacket.class);
        
        magicYarn = (new ItemMagicYarn()).setUnlocalizedName("magic_yarn");
        GameRegistry.registerItem(magicYarn, "magic_yarn");
        
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
        GameRegistry.addRecipe(new ItemStack(magicYarn, 1), new Object[]{
            "###", "#X#", "###", Character.valueOf('X'), Items.compass, Character.valueOf('#'), Blocks.wool
        });
    }
    
    @SubscribeEvent
    public void onEntityJoinsWorld(EntityJoinWorldEvent event)
    {
        if (!event.world.isRemote && event.entity instanceof EntityPlayerMP)
        {
            networkHelper.sendPacketToPlayer(new HandshakePacket(), (EntityPlayerMP) event.entity);
        }
    }

    @Override
    public void onPlayerUsedYarn(World world, EntityPlayer player, float timeButtonHeld)
    {
        // NOOP
    }
    
}
