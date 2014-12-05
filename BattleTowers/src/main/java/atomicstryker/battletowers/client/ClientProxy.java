package atomicstryker.battletowers.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.AS_EntityGolemFireball;
import atomicstryker.battletowers.common.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
    }
    
    @Override
    public void load()
    {
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolem.class, new AS_RenderGolem(Minecraft.getMinecraft().getRenderManager()));
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolemFireball.class, new AS_RenderFireball(Minecraft.getMinecraft().getRenderManager(), 0.5f));
    }
}
