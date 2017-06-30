package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.DefaultConfigFilePrinter;
import atomicstryker.findercompass.common.FinderCompassMod;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onLoad()
    {
        COMPASS_ITEM_ID = Items.COMPASS;
        
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mesher.register(COMPASS_ITEM_ID, 0, new ModelResourceLocation("compass", "inventory"));
        
        compassLogic = new FinderCompassLogic(mc);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent tick)
    {
        if (tick.phase == Phase.END && compassLogic != null)
        {
            if (!FinderCompassMod.itemEnabled)
            {
                if (tick.player.getHeldItemMainhand() != null && tick.player.getHeldItemMainhand().getItem() == COMPASS_ITEM_ID)
                {
                    if (mc.gameSettings.keyBindAttack.isKeyDown())
                    {
                        if (!repeat)
                        {
                            repeat = true;
                            switchSetting();
                            tick.player.world.playSound(null, new BlockPos(tick.player), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
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

        if (mc.world != null)
        {
            mc.world.playSound(null, new BlockPos(mc.player), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("Finder Compass Mode: " + currentSetting.getName()));
        }
    }

    /**
     * Used by the server packet to override the clientside config
     * 
     * @param dataIn inputstream to be used by DefaultConfigFilePrinter
     */
    public void inputOverrideConfig(DataInputStream dataIn)
    {
        settingList.clear();
        new DefaultConfigFilePrinter().parseConfig(new BufferedReader(new InputStreamReader(dataIn)), settingList);
        mc.ingameGUI.getChatGUI().printChatMessage(
                new TextComponentTranslation("Finder Compass server config loaded; " + settingList.size() + " custom Setting-Sets loaded"));
    }

    public void onFoundChunkCoordinates(BlockPos input, Block b, int meta)
    {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+b+", damage "+meta);
        CompassTargetData key = new CompassTargetData(b, meta);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
