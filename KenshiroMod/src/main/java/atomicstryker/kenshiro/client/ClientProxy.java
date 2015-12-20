package atomicstryker.kenshiro.client;

import atomicstryker.kenshiro.common.CommonProxy;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy
{
    
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new KenshiroClient());
        MinecraftForge.EVENT_BUS.register(new RenderHookKenshiro());
    }
    
    @Override
    public void load()
    {
    }
}
