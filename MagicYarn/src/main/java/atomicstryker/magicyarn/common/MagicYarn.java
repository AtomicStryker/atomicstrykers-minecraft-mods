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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import atomicstryker.magicyarn.common.network.HandshakePacket;
import atomicstryker.magicyarn.common.network.NetworkHelper;
import atomicstryker.magicyarn.common.network.PathPacket;

@Mod(modid = "magicyarn", name = "Magic Yarn", version = "1.1.6")
public class MagicYarn implements IProxy
{
    
    @Instance("magicyarn")
    public static MagicYarn instance;
    
    public NetworkHelper networkHelper;
    
    @SidedProxy(clientSide = "atomicstryker.magicyarn.client.MagicYarnClient", serverSide = "atomicstryker.magicyarn.common.MagicYarn")
    public static IProxy proxy;
    
	public Item magicYarn;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        networkHelper = new NetworkHelper("AS_MY", HandshakePacket.class, PathPacket.class);
        
        magicYarn = new ItemMagicYarn().setUnlocalizedName("magicyarn");
        GameRegistry.registerItem(magicYarn, "magicyarn");
        
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
        proxy.init();
        
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

    @Override
    public void init()
    {
        // NOOP
    }
    
}
