package atomicstryker.ropesplus.common;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class ServerTickHandler
{
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            for(int x = 0; x < RopesPlusCore.ropeEntArray.size(); x++)
            {
                Object temp = RopesPlusCore.ropeEntArray.get(x);
                if (temp instanceof BlockRopePseudoEnt)
                {
                    if (((BlockRopePseudoEnt) temp).OnUpdate())
                    {
                        RopesPlusCore.ropeEntArray.remove(x);
                    }
                }
                else if (temp instanceof TileEntityRope)
                {
                    if (((TileEntityRope) temp).OnUpdate())
                    {
                        RopesPlusCore.ropeEntArray.remove(x);
                    }
                }
            }
        }
    }
}
