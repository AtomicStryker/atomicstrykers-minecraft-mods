package com.sirolf2009.necromancy.core.handler;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.sirolf2009.necromancy.core.proxy.ClientProxy;
import com.sirolf2009.necromancy.entity.EntityTear;
import com.sirolf2009.necromancy.entity.EntityTearBlood;
import com.sirolf2009.necromancy.item.ItemNecromancy;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class KeyHandlerNecro
{

    public static KeyBinding tearNormal = new KeyBinding("Shoot Normal Tear", Keyboard.KEY_F, "key.categories.gameplay");
    public static KeyBinding tearBlood = new KeyBinding("Shoot Blood Tear", Keyboard.KEY_G, "key.categories.gameplay");
    public long lastShotNormal = 0;
    public long lastShotBlood = 0;

    public KeyHandlerNecro()
    {
        ClientRegistry.registerKeyBinding(tearNormal);
        ClientRegistry.registerKeyBinding(tearBlood);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            if (FMLClientHandler.instance().getClient().currentScreen == null && ClientProxy.mc.thePlayer.inventory.armorInventory[3] != null
                    && ClientProxy.mc.thePlayer.inventory.armorInventory[3].getItem() == ItemNecromancy.isaacsHead)
            {
                if (tearNormal.getIsKeyPressed() && lastShotNormal + 1200 < System.currentTimeMillis())
                {
                    EntityTear tearNormal = new EntityTear(ClientProxy.mc.thePlayer.worldObj, ClientProxy.mc.thePlayer, 2);
                    ClientProxy.mc.thePlayer.worldObj.spawnEntityInWorld(tearNormal);
                    lastShotNormal = System.currentTimeMillis();
                }
                if (tearBlood.getIsKeyPressed() && lastShotBlood + 1900 < System.currentTimeMillis())
                {
                    EntityTearBlood tearBlood = new EntityTearBlood(ClientProxy.mc.thePlayer.worldObj, ClientProxy.mc.thePlayer, 2);
                    ClientProxy.mc.thePlayer.worldObj.spawnEntityInWorld(tearBlood);
                    lastShotBlood = System.currentTimeMillis();
                }
            }
        }
    }

}
