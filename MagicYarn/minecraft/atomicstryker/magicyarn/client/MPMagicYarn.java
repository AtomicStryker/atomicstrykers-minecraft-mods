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
import cpw.mods.fml.common.network.FMLNetworkHandler;

public class MPMagicYarn
{

    private final Minecraft mcinstance;
    private final MagicYarnClient clientInstance;

    private long timeStartedHoldingButton;
    private boolean serverDoesNotHaveMod;

    public MPMagicYarn(Minecraft mc, MagicYarnClient client)
    {
        mcinstance = mc;
        clientInstance = client;
        timeStartedHoldingButton = 0;
        serverDoesNotHaveMod = false;

        KeyBinding[] ckey = { new KeyBinding("MagicYarn Clientkey", Keyboard.KEY_J) };
        KeyBinding[] pkey = { new KeyBinding("MagicYarn Playerkey", Keyboard.KEY_K) };
        boolean[] repeat = {false};
        KeyBindingRegistry.registerKeyBinding(new TriggerKey(ckey, repeat));
        KeyBindingRegistry.registerKeyBinding(new PlayerKey(pkey, repeat));

    }

    private class TriggerKey extends KeyHandler
    {
        private final EnumSet tickTypes;

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
                else
                {
                    mcinstance.thePlayer.sendChatToPlayer("This server has Magic Yarn installed. Craft the Item!");
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
        private final EnumSet tickTypes;

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
                    else
                    {
                        mcinstance.thePlayer.sendChatToPlayer("This server has Magic Yarn installed. Craft the Item!");
                    }
                }
                else
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
    }

    public void onServerHasMod()
    {
        serverDoesNotHaveMod = false;
        mcinstance.thePlayer.sendChatToPlayer("Magic Yarn found on server.");
    }
    
}
