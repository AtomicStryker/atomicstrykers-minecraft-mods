package atomicstryker.magicyarn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import org.lwjgl.input.Keyboard;

import atomicstryker.magicyarn.common.MagicYarn;

public class MPMagicYarn
{

    private final Minecraft mcinstance;
    private long timeStartedHoldingButton;
    private boolean serverDoesNotHaveMod;
    private boolean messageShown;
    
    private KeyBinding clientKey;
    private KeyBinding playerKey;

    public MPMagicYarn(Minecraft mc, MagicYarnClient client)
    {
        mcinstance = mc;
        timeStartedHoldingButton = 0;
        serverDoesNotHaveMod = true;
        messageShown = false;
        
        clientKey = new KeyBinding("MagicYarn Clientkey", Keyboard.KEY_J, "key.categories.misc");
        playerKey = new KeyBinding("MagicYarn Playerkey", Keyboard.KEY_K, "key.categories.misc");
        
        ClientRegistry.registerKeyBinding(clientKey);
        ClientRegistry.registerKeyBinding(playerKey);
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            if (clientKey.isKeyDown())
            {
                if (serverDoesNotHaveMod)
                {
                    if (mcinstance.currentScreen == null)
                    {
                        if (timeStartedHoldingButton == 0)
                        {
                            timeStartedHoldingButton = System.currentTimeMillis();
                        }
                    }
                    else
                    {
                        timeStartedHoldingButton = 0;
                    }
                }
                else if (mcinstance.currentScreen == null && !messageShown)
                {
                    messageShown = true;
                    mcinstance.thePlayer.addChatMessage(new ChatComponentText("This server has Magic Yarn installed. Craft the Item!"));
                }
            }
            else
            {
                if (timeStartedHoldingButton != 0)
                {
                    MagicYarn.proxy.onPlayerUsedYarn(mcinstance.theWorld, mcinstance.thePlayer, (float)((System.currentTimeMillis()-timeStartedHoldingButton)/1000));
                    timeStartedHoldingButton = 0L;
                }
            }
            
            if (mcinstance.currentScreen == null && playerKey.isKeyDown())
            {
                if (!serverDoesNotHaveMod)
                {
                    ItemStack curItem = mcinstance.thePlayer.getCurrentEquippedItem();
                    if (curItem != null && curItem.getItem() == MagicYarn.instance.magicYarn)
                    {
                        mcinstance.displayGuiScreen(new GuiNavigateToPlayer()); 
                    }
                    else if (!messageShown)
                    {
                        messageShown = true;
                        mcinstance.thePlayer.addChatMessage(new ChatComponentText("This server has Magic Yarn installed. Craft the Item!"));
                    }
                }
                else if (mcinstance.currentScreen == null)
                {
                    mcinstance.displayGuiScreen(new GuiNavigateToPlayer());
                }
            }
        }
    }

    public void onServerHasMod()
    {
        serverDoesNotHaveMod = false;
        //mcinstance.thePlayer.addChatMessage(new ChatComponentText("Magic Yarn found on server."));
    }
    
    public boolean getHasServerMod()
    {
        return !serverDoesNotHaveMod;
    }
    
}
