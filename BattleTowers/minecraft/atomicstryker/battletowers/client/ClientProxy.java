package atomicstryker.battletowers.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;
import atomicstryker.battletowers.common.AS_BattleTowersCore;
import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void load()
    {
        TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
        MinecraftForgeClient.preloadTexture("/atomicstryker/battletowers/client/golemdormant.png");
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolem.class, new AS_RenderGolem());
    }
}
