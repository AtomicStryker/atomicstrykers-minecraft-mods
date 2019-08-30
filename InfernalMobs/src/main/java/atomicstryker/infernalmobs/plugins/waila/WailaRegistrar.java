package atomicstryker.infernalmobs.plugins.waila;

import com.google.common.eventbus.Subscribe;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class WailaRegistrar {
	
	public static final String wailaModid = "waila";
	
	// Config entries
	public static String CONFIG_INFERNALMOBS_MODS = "infernalmobs.mods";
	public static String CONFIG_INFERNALMOBS_LINEBREAK = "infernalmobs.linebreak";
	
	@Subscribe
	public static void preInit(FMLPreInitializationEvent event) {
		FMLInterModComms.sendMessage(wailaModid, "register", "atomicstryker.infernalmobs.plugins.waila.WailaRegistrar.wailaCallback");
	}
	
	public static void wailaCallback(IWailaRegistrar registrar) {
		registrar.addConfig("infernalmobs", CONFIG_INFERNALMOBS_MODS, true);
		registrar.addConfig("infernalmobs", CONFIG_INFERNALMOBS_LINEBREAK, true);
		
		// Register body provider
		registrar.registerBodyProvider(PluginInfernalMobs.INSTANCE, EntityLiving.class);
	}
}
