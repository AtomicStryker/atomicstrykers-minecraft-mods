package atomicstryker.findercompass.client;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class FinderCompassClientTicker implements ITickHandler
{
    private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);
    
    private final Minecraft mc;
    private final int COMPASS_ITEM_ID = 345;
    private long time;
    private boolean registered;
    private boolean repeat;
    
    public FinderCompassClientTicker()
    {
        mc = FMLClientHandler.instance().getClient();
        time = System.currentTimeMillis();
        registered = false;
        repeat = false;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (mc.theWorld != null
        && mc.thePlayer != null)
        {
            if (!registered
            && !AS_FinderCompass.isHackedIn
            && System.currentTimeMillis() > time + 5000L)
            {
                AS_FinderCompass replacement = new AS_FinderCompass(mc);
                if (AS_FinderCompass.isHackedIn)
                {
                    mc.renderEngine.registerTextureFX(replacement);
                    registered = true;
                }
            }
            
            if (mc.thePlayer.getCurrentEquippedItem() != null
            && mc.thePlayer.getCurrentEquippedItem().itemID == COMPASS_ITEM_ID
            && mc.gameSettings.keyBindUseItem.pressed)
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
