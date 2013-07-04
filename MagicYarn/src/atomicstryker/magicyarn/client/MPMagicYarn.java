package atomicstryker.magicyarn.client;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import atomicstryker.magicyarn.common.MagicYarn;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class MPMagicYarn
{

    private final Minecraft mcinstance;
    private long timeStartedHoldingButton;
    private boolean serverDoesNotHaveMod;
    private boolean messageShown;

    public MPMagicYarn(Minecraft mc, MagicYarnClient client)
    {
        mcinstance = mc;
        timeStartedHoldingButton = 0;
        serverDoesNotHaveMod = false;
        messageShown = false;

        KeyBinding[] ckey = { new KeyBinding("MagicYarn Clientkey", Keyboard.KEY_J) };
        KeyBinding[] pkey = { new KeyBinding("MagicYarn Playerkey", Keyboard.KEY_K) };
        boolean[] repeat = {false};
        KeyBindingRegistry.registerKeyBinding(new TriggerKey(ckey, repeat));
        KeyBindingRegistry.registerKeyBinding(new PlayerKey(pkey, repeat));

    }

    private class TriggerKey extends KeyHandler
    {
        private final EnumSet<TickType> tickTypes;

        public TriggerKey(KeyBinding[] keyBindings, boolean[] repeatings)
        {
            super(keyBindings, repeatings);
            tickTypes = EnumSet.of(TickType.CLIENT);
        }

        @Override
        public String getLabel()
        {
            return "MagicYarn Clientkey";
        }

        @Override
        public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
        {
            if (tickEnd)
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
                    mcinstance.thePlayer.addChatMessage("This server has Magic Yarn installed. Craft the Item!");
                }
            }
        }

        @Override
        public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
        {
            if (timeStartedHoldingButton != 0)
            {
                MagicYarn.proxy.onPlayerUsedYarn(mcinstance.theWorld, mcinstance.thePlayer, (float)((System.currentTimeMillis()-timeStartedHoldingButton)/1000));
                timeStartedHoldingButton = 0L;
            }
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

    }

    private class PlayerKey extends KeyHandler
    {
        private final EnumSet<TickType> tickTypes;

        public PlayerKey(KeyBinding[] keyBindings, boolean[] repeatings)
        {
            super(keyBindings, repeatings);
            tickTypes = EnumSet.of(TickType.CLIENT);
        }

        @Override
        public String getLabel()
        {
            return "MagicYarn Playerkey";
        }

        @Override
        public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
        {
        }

        @Override
        public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
        {
            if (mcinstance.currentScreen == null && tickEnd)
            {
                if (!serverDoesNotHaveMod)
                {
                    ItemStack curItem = mcinstance.thePlayer.getCurrentEquippedItem();
                    if (curItem != null && curItem.itemID == MagicYarn.magicYarn.itemID)
                    {
                        mcinstance.displayGuiScreen(new GuiNavigateToPlayer()); 
                    }
                    else if (!messageShown)
                    {
                        messageShown = true;
                        mcinstance.thePlayer.addChatMessage("This server has Magic Yarn installed. Craft the Item!");
                    }
                }
                else if (mcinstance.currentScreen == null)
                {
                    mcinstance.displayGuiScreen(new GuiNavigateToPlayer());
                }
            }
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return tickTypes;
        }

    }
    
    public void onCheckingHasServerMod()
    {
        serverDoesNotHaveMod = true;
        messageShown = false;
    }

    public void onServerHasMod()
    {
        serverDoesNotHaveMod = false;
        mcinstance.thePlayer.addChatMessage("Magic Yarn found on server.");
    }
    
    public boolean getHasServerMod()
    {
        return !serverDoesNotHaveMod;
    }
    
}
