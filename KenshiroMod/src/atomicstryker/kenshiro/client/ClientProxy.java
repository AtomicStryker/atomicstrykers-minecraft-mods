package atomicstryker.kenshiro.client;

import net.minecraftforge.common.MinecraftForge;
import atomicstryker.kenshiro.common.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void load()
    {
        new KenshiroClient();
        MinecraftForge.EVENT_BUS.register(new KenshiroSounds());
    }
}
