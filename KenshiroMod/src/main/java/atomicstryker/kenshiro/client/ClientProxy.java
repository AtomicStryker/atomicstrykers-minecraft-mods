package atomicstryker.kenshiro.client;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;
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
        MinecraftForge.EVENT_BUS.register(new KenshiroSounds());
    }
}
