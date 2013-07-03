package atomicstryker.battletowers.common;

import java.util.EnumSet;
import java.util.Iterator;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ServerTickHandler implements ITickHandler
{
    private final EnumSet<TickType> tickTypes;
    private long time;
    
    public ServerTickHandler()
    {
        tickTypes = EnumSet.of(TickType.WORLD);
        time = System.currentTimeMillis();
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (System.currentTimeMillis() > time + 1000L) // its a one second timer OMFG
        {
        	time = System.currentTimeMillis();
        	Iterator<AS_TowerDestroyer> iter =  AS_BattleTowersCore.getTowerDestroyers().iterator();
            while(iter.hasNext())
            {
				AS_TowerDestroyer td = iter.next();
                if (td.isFinished())
                {
                    iter.remove();
                }
                else
                {
                    td.update();
                }
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
        return "BattleTowers";
    }

}
