package atomicstryker.updatecheck.client;

import atomicstryker.updatecheck.common.IProxy;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class UpdateCheckClient implements IProxy
{

    @Override
    public void announce(String announcement)
    {
        /* getChatGUI, printChatMessage */
        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation(announcement));
    }

}
