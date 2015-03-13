package atomicstryker.kenshiro.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import atomicstryker.kenshiro.common.CommonProxy;

public class ClientProxy extends CommonProxy
{
    
    public void preInit()
    {
        FMLCommonHandler.instance().bus().register(new KenshiroClient());
        MinecraftForge.EVENT_BUS.register(new RenderHookKenshiro());
    }
    
    @Override
    public void load()
    {
    }
}
