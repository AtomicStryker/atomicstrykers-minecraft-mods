package atomicstryker.findercompass.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.DefaultConfigFilePrinter;
import atomicstryker.findercompass.common.FinderCompassMod;

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
        
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mesher.register(COMPASS_ITEM_ID, 0, new ModelResourceLocation("compass", "inventory"));
        
        compassLogic = new FinderCompassLogic(mc);
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
                    if (mc.gameSettings.keyBindAttack.isKeyDown())
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

    public void onFoundChunkCoordinates(BlockPos input, Block b, int meta)
    {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(b, meta);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
