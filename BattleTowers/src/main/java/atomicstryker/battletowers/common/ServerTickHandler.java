package atomicstryker.battletowers.common;

import java.util.Iterator;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerTickHandler
{
    private long time;
    
    public ServerTickHandler()
    {
        time = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
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

}
