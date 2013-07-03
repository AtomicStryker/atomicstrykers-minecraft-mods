package atomicstryker.updatecheck.client;

import cpw.mods.fml.client.FMLClientHandler;
import atomicstryker.updatecheck.common.IProxy;

public class UpdateCheckClient implements IProxy
{

    @Override
    public void announce(String announcement)
    {
        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(announcement);
    }

}
