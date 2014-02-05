package mods.nandonalt.coralmod.client;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import mods.nandonalt.coralmod.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class ClientProxy extends CommonProxy
{

    private KeyBinding coralKey;
    private Minecraft game;

    @Override
    public void proxyPreInit()
    {
        coralKey = new KeyBinding("Coral Reef GUI", Keyboard.KEY_C, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(coralKey);
        FMLCommonHandler.instance().bus().register(this);
    }

    @Override
    public void proxyInit()
    {
        game = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent tick)
    {
        if (tick.phase == Phase.END && tick.player != null && !tick.player.isDead)
        {
            if (game.currentScreen == null && coralKey.isPressed())
            {
                game.displayGuiScreen(new GuiCoralReef());
            }
        }
    }

}