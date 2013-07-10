package atomicstryker.findercompass.client;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import atomicstryker.findercompass.common.FinderCompassMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class FinderCompassClientTicker implements ITickHandler
{
    private EnumSet<TickType> tickTypes = EnumSet.of(TickType.CLIENT);
    
    private final Minecraft mc;
    private int COMPASS_ITEM_ID;
    private boolean repeat;
    
    public FinderCompassClientTicker()
    {
        mc = FMLClientHandler.instance().getClient();
        repeat = false;
        
        COMPASS_ITEM_ID = Item.compass.itemID;
        MinecraftForgeClient.registerItemRenderer(FinderCompassMod.compass.itemID, new CompassCustomRenderer());
        if (!FinderCompassMod.itemEnabled)
        {
            MinecraftForgeClient.registerItemRenderer(COMPASS_ITEM_ID, new CompassCustomRenderer());
        }
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (!FinderCompassMod.itemEnabled
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
                    AS_FinderCompass.switchSetting();
                    mc.theWorld.playSound(mc.thePlayer.posX+0.5D, mc.thePlayer.posY+0.5D, mc.thePlayer.posZ+0.5D, "random.click", 0.3F, 0.6F, false);
                }
            }
            else
            {
                repeat = false;
            }
        }
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
}
