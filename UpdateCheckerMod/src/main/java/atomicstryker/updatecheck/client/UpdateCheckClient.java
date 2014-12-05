package atomicstryker.updatecheck.client;

import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.FMLClientHandler;
import atomicstryker.updatecheck.common.IProxy;

public class UpdateCheckClient implements IProxy
{

    @Override
    public void announce(String announcement)
    {
        /* getChatGUI, printChatMessage */
        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(announcement));
    }

}
