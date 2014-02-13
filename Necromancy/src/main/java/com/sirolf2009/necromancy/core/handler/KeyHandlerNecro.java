package com.sirolf2009.necromancy.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import atomicstryker.necromancy.network.TearShotPacket;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.entity.EntityTear;
import com.sirolf2009.necromancy.entity.EntityTearBlood;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class KeyHandlerNecro
{

    private final Minecraft mc;
    public static KeyBinding tearNormal = new KeyBinding("Shoot Normal Tear", Keyboard.KEY_F, "key.categories.gameplay");
    public static KeyBinding tearBlood = new KeyBinding("Shoot Blood Tear", Keyboard.KEY_G, "key.categories.gameplay");
    public long lastShotNormal = 0;
    public long lastShotBlood = 0;

    public KeyHandlerNecro()
    {
        ClientRegistry.registerKeyBinding(tearNormal);
        ClientRegistry.registerKeyBinding(tearBlood);
        mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event)
    {
        EntityPlayer player = mc.thePlayer;
        if (player != null && player.isEntityAlive() && event.phase == Phase.END)
        {
            if (mc.currentScreen == null && player.inventory.armorInventory[3] != null
                    && player.inventory.armorInventory[3].getItem() == RegistryNecromancyItems.isaacsHead)
            {
                if (tearNormal.getIsKeyPressed() && lastShotNormal + 1200 < System.currentTimeMillis())
                {
                    EntityTear tear = new EntityTear(player.worldObj, player, 2);
                    Necromancy.instance.networkHelper.sendPacketToServer(new TearShotPacket(player.getCommandSenderName(), false, tear.posX,
                            tear.posX, tear.posZ, tear.motionX, tear.motionY, tear.motionZ));
                    // player.worldObj.spawnEntityInWorld(tearNormal);
                    lastShotNormal = System.currentTimeMillis();
                }
                if (tearBlood.getIsKeyPressed() && lastShotBlood + 1900 < System.currentTimeMillis())
                {
                    EntityTearBlood tear = new EntityTearBlood(player.worldObj, player, 2);
                    Necromancy.instance.networkHelper.sendPacketToServer(new TearShotPacket(player.getCommandSenderName(), true, tear.posX,
                            tear.posX, tear.posZ, tear.motionX, tear.motionY, tear.motionZ));
                    // player.worldObj.spawnEntityInWorld(tearBlood);
                    lastShotBlood = System.currentTimeMillis();
                }
            }
        }
    }

}
