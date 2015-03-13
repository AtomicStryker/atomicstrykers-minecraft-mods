package atomicstryker.ropesplus.common;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ServerTickHandler
{
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick)
    {
        if (tick.phase == Phase.END)
        {
            for(int x = 0; x < RopesPlusCore.instance.ropeEntArray.size(); x++)
            {
                Object temp = RopesPlusCore.instance.ropeEntArray.get(x);
                if (temp instanceof BlockRopePseudoEnt)
                {
                    if (((BlockRopePseudoEnt) temp).OnUpdate())
                    {
                        RopesPlusCore.instance.ropeEntArray.remove(x);
                    }
                }
                else if (temp instanceof TileEntityRope)
                {
                    if (((TileEntityRope) temp).OnUpdate())
                    {
                        RopesPlusCore.instance.ropeEntArray.remove(x);
                    }
                }
            }
        }
    }
}
