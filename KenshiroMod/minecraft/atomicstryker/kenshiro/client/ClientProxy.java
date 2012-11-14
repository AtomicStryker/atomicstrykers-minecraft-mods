package atomicstryker.kenshiro.client;

import atomicstryker.kenshiro.common.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void load()
    {
        new KenshiroClient();
    }
}
