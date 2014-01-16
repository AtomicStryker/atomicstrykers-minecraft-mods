package atomicstryker.battletowers.client;

import net.minecraftforge.common.MinecraftForge;
import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.AS_EntityGolemFireball;
import atomicstryker.battletowers.common.CommonProxy;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new BattleTowerSounds());
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
    }
    
    @Override
    public void load()
    {
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolem.class, new AS_RenderGolem());
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolemFireball.class, new AS_RenderFireball(0.5f));
    }
}
