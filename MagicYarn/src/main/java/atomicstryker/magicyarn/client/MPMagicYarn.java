package atomicstryker.magicyarn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import org.lwjgl.input.Keyboard;

import atomicstryker.magicyarn.common.MagicYarn;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

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
            if (clientKey.func_151470_d())
            {
                if (serverDoesNotHaveMod)
                {
                    if (mcinstance.currentScreen == null)
                    {
                        timeStartedHoldingButton = System.currentTimeMillis();
                    }
                    else
                    {
                        timeStartedHoldingButton = 0;
                    }
                }
                else if (mcinstance.currentScreen == null && !messageShown)
                {
                    messageShown = true;
                    mcinstance.thePlayer.func_145747_a(new ChatComponentText("This server has Magic Yarn installed. Craft the Item!"));
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
            
            if (mcinstance.currentScreen == null && playerKey.func_151470_d())
            {
                if (!serverDoesNotHaveMod)
                {
                    ItemStack curItem = mcinstance.thePlayer.getCurrentEquippedItem();
                    if (curItem != null && curItem.getItem() == MagicYarn.magicYarn)
                    {
                        mcinstance.func_147108_a(new GuiNavigateToPlayer()); 
                    }
                    else if (!messageShown)
                    {
                        messageShown = true;
                        mcinstance.thePlayer.func_145747_a(new ChatComponentText("This server has Magic Yarn installed. Craft the Item!"));
                    }
                }
                else if (mcinstance.currentScreen == null)
                {
                    mcinstance.func_147108_a(new GuiNavigateToPlayer());
                }
            }
        }
    }

    public void onServerHasMod()
    {
        serverDoesNotHaveMod = false;
        mcinstance.thePlayer.func_145747_a(new ChatComponentText("Magic Yarn found on server."));
    }
    
    public boolean getHasServerMod()
    {
        return !serverDoesNotHaveMod;
    }
    
}
