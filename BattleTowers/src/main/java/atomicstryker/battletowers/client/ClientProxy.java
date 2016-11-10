package atomicstryker.battletowers.client;

import atomicstryker.battletowers.common.AS_EntityGolem;
import atomicstryker.battletowers.common.AS_EntityGolemFireball;
import atomicstryker.battletowers.common.CommonProxy;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolem.class, new IRenderFactory<AS_EntityGolem>()
        {
            @Override
            public Render<? super AS_EntityGolem> createRenderFor(RenderManager manager)
            {
                return new AS_RenderGolem(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(AS_EntityGolemFireball.class, new IRenderFactory<AS_EntityGolemFireball>()
        {
            @Override
            public Render<? super AS_EntityGolemFireball> createRenderFor(RenderManager manager)
            {
                return new AS_RenderFireball(manager, 0.5f);
            }
        });
    }

    @Override
    public void load()
    {

    }
}
