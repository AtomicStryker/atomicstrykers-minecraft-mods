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
import net.minecraftforge.event.world.WorldEvent;
import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.DefaultConfigFilePrinter;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.HandshakePacket;
import cpw.mods.fml.client.FMLClientHandler;
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
        instance = this;
        mc = FMLClientHandler.instance().getClient();
        repeat = false;
        settingList = FinderCompassMod.instance.settingList;
        currentSetting = settingList.get(0);

        COMPASS_ITEM_ID = Items.compass;
        MinecraftForgeClient.registerItemRenderer(FinderCompassMod.instance.compass, new CompassCustomRenderer());
        if (!FinderCompassMod.instance.itemEnabled)
        {
            MinecraftForgeClient.registerItemRenderer(COMPASS_ITEM_ID, new CompassCustomRenderer());
        }

        compassLogic = new FinderCompassLogic(mc);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        FinderCompassLogic.serverHasFinderCompass = false;
        FinderCompassMod.instance.networkHelper.sendPacketToServer(new HandshakePacket());
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            if (!FinderCompassMod.instance.itemEnabled && mc.theWorld != null && mc.thePlayer != null)
            {
                if (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() == COMPASS_ITEM_ID)
                {
                    if (mc.gameSettings.keyBindAttack.func_151470_d())
                    {
                        if (!repeat)
                        {
                            repeat = true;
                            switchSetting();
                            mc.theWorld.playSound(mc.thePlayer.posX + 0.5D, mc.thePlayer.posY + 0.5D, mc.thePlayer.posZ + 0.5D, "random.click", 0.3F,
                                    0.6F, false);
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
        currentSetting.onDisableThisConfig();

        int nextIndex = settingList.indexOf(currentSetting) + 1;
        if (nextIndex >= settingList.size())
        {
            nextIndex = 0;
        }

        currentSetting = settingList.get(nextIndex);

        if (mc.theWorld != null)
        {
            mc.theWorld.playSound(mc.thePlayer.posX + 0.5D, mc.thePlayer.posY + 0.5D, mc.thePlayer.posZ + 0.5D, "random.click", 0.3F, 0.6F, false);
            mc.ingameGUI.func_146158_b().func_146227_a(new ChatComponentText("Finder Compass Mode: " + currentSetting.getName()));
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
        mc.ingameGUI.func_146158_b().func_146227_a(
                new ChatComponentText("Finder Compass server config loaded; " + settingList.size() + " custom Setting-Sets loaded"));
    }

    public void onFoundChunkCoordinates(ChunkCoordinates input, Block b, int meta)
    {
        //System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(b, meta);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
