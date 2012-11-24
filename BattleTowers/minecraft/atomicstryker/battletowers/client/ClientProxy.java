package atomicstryker.battletowers.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;
import atomicstryker.battletowers.client.sound.BattleTowerSounds;
import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.AS_EntityGolemFireball;
import atomicstryker.battletowers.common.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new BattleTowerSounds());
    }
    
    @Override
    public void load()
    {
        TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
        MinecraftForgeClient.preloadTexture("/atomicstryker/battletowers/client/golemdormant.png");
        MinecraftForgeClient.preloadTexture("/atomicstryker/battletowers/client/golem.png");
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolem.class, new AS_RenderGolem());
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolemFireball.class, new AS_RenderFireball(0.5f));
    }
}
