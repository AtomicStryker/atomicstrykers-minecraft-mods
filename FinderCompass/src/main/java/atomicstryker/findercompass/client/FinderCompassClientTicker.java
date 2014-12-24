package atomicstryker.findercompass.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.DefaultConfigFilePrinter;
import atomicstryker.findercompass.common.FinderCompassMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class FinderCompassClientTicker
{

    public static FinderCompassClientTicker instance;

    private CompassSetting currentSetting;
    public FinderCompassLogic compassLogic;

    private final Minecraft mc;
    private Item COMPASS_ITEM_ID;
    private boolean repeat;
    private ArrayList<CompassSetting> settingList;

    public FinderCompassClientTicker()
    {
        mc = FMLClientHandler.instance().getClient();
        repeat = false;
        settingList = FinderCompassMod.instance.settingList;
        currentSetting = null;

        FMLCommonHandler.instance().bus().register(this);
    }

    public void onLoad()
    {
        COMPASS_ITEM_ID = Items.compass;
        MinecraftForgeClient.registerItemRenderer(FinderCompassMod.instance.compass, new CompassCustomRenderer());
        MinecraftForge.EVENT_BUS.register(this);
        if (!FinderCompassMod.instance.itemEnabled)
        {
            MinecraftForgeClient.registerItemRenderer(COMPASS_ITEM_ID, new CompassCustomRenderer());
        }
        compassLogic = new FinderCompassLogic(mc);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        // System.out.println("Finder Compass onWorldLoad: "+event.world);
        // FinderCompassLogic.serverHasFinderCompass = false;
        // FinderCompassMod.instance.networkHelper.sendPacketToServer(new HandshakePacket(mc.thePlayer.getCommandSenderName()));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent tick)
    {
        if (tick.phase == Phase.END && compassLogic != null)
        {
            if (!FinderCompassMod.instance.itemEnabled)
            {
                if (tick.player.getCurrentEquippedItem() != null && tick.player.getCurrentEquippedItem().getItem() == COMPASS_ITEM_ID)
                {
                    if (mc.gameSettings.keyBindAttack.getIsKeyPressed())
                    {
                        if (!repeat)
                        {
                            repeat = true;
                            switchSetting();
                            tick.player.worldObj.playSound(tick.player.posX + 0.5D, tick.player.posY + 0.5D, tick.player.posZ + 0.5D, "random.click",
                                    0.3F, 0.6F, false);
                        }
                    }
                    else
                    {
                        repeat = false;
                    }
                }
            }
            compassLogic.onTick();
        }
    }

    public CompassSetting getCurrentSetting()
    {
        return currentSetting;
    }

    public void switchSetting()
    {
        if (settingList.isEmpty())
        {
            return;
        }
        
        int nextIndex;
        if (currentSetting == null)
        {
            nextIndex = 0;
        }
        else
        {
            currentSetting.onDisableThisConfig();

            nextIndex = settingList.indexOf(currentSetting) + 1;
            if (nextIndex >= settingList.size())
            {
                nextIndex = 0;
            }
        }
        
        currentSetting = settingList.get(nextIndex);

        if (mc.theWorld != null)
        {
            mc.theWorld.playSound(mc.thePlayer.posX + 0.5D, mc.thePlayer.posY + 0.5D, mc.thePlayer.posZ + 0.5D, "random.click", 0.3F, 0.6F, false);
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Finder Compass Mode: " + currentSetting.getName()));
        }
    }

    /**
     * Used by the server packet to override the clientside config
     * 
     * @param dataIn
     */
    public void inputOverrideConfig(DataInputStream dataIn)
    {
        settingList.clear();
        new DefaultConfigFilePrinter().parseConfig(new BufferedReader(new InputStreamReader(dataIn)), settingList);
        mc.ingameGUI.getChatGUI().printChatMessage(
                new ChatComponentText("Finder Compass server config loaded; " + settingList.size() + " custom Setting-Sets loaded"));
    }

    public void onFoundChunkCoordinates(ChunkCoordinates input, Block b, int meta)
    {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(b, meta);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
