package atomicstryker.findercompass.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.client.MinecraftForgeClient;
import atomicstryker.findercompass.common.CompassIntPair;
import atomicstryker.findercompass.common.DefaultConfigFilePrinter;
import atomicstryker.findercompass.common.FinderCompassMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class FinderCompassClientTicker implements ITickHandler
{
    
    public static FinderCompassClientTicker instance;
    
    private CompassSetting currentSetting;
    public FinderCompassLogic compassLogic;
    
    private EnumSet<TickType> tickTypes = EnumSet.of(TickType.CLIENT);
    private final Minecraft mc;
    private int COMPASS_ITEM_ID;
    private boolean repeat;
    private ArrayList<CompassSetting> settingList;

    
    public FinderCompassClientTicker()
    {
        instance = this;
        mc = FMLClientHandler.instance().getClient();
        repeat = false;
        settingList = FinderCompassMod.instance.settingList;
        currentSetting = settingList.get(0);
        
        COMPASS_ITEM_ID = Item.compass.itemID;
        MinecraftForgeClient.registerItemRenderer(FinderCompassMod.instance.compass.itemID, new CompassCustomRenderer());
        if (!FinderCompassMod.instance.itemEnabled)
        {
            MinecraftForgeClient.registerItemRenderer(COMPASS_ITEM_ID, new CompassCustomRenderer());
        }
        
        compassLogic = new FinderCompassLogic(mc);
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (!FinderCompassMod.instance.itemEnabled
        && mc.theWorld != null
        && mc.thePlayer != null)
        {         
            if (mc.thePlayer.getCurrentEquippedItem() != null
            && mc.thePlayer.getCurrentEquippedItem().itemID == COMPASS_ITEM_ID
            && mc.gameSettings.keyBindUseItem.pressed
            && mc.objectMouseOver == null)
            {
                if (!repeat)
                {
                    repeat = true;
                    switchSetting();
                    mc.theWorld.playSound(mc.thePlayer.posX+0.5D, mc.thePlayer.posY+0.5D, mc.thePlayer.posZ+0.5D, "random.click", 0.3F, 0.6F, false);
                }
            }
            else
            {
                repeat = false;
            }
        }
        
        compassLogic.onTick();
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }

    @Override
    public String getLabel()
    {
        return "FinderCompass";
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
            mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass Mode: " + currentSetting.getName());
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
        mc.ingameGUI.getChatGUI().printChatMessage("Finder Compass server config loaded; " + settingList.size() + " custom Setting-Sets loaded");
    }
    
    public void onFoundChunkCoordinates(ChunkCoordinates input, int[] intArray)
    {
        // System.out.println("onFoundChunkCoordinates ["+input.posX+"|"+input.posZ+"] for ID "+intArray[0]+", damage "+intArray[1]);
        CompassIntPair key = new CompassIntPair(intArray[0], intArray[1]);
        currentSetting.getNewFoundTargets().put(key, input);
    }
}
