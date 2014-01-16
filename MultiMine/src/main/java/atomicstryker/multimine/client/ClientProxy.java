package atomicstryker.multimine.client;

import atomicstryker.multimine.common.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void onLoad()
    {
        new MultiMineClient();
    }

}
