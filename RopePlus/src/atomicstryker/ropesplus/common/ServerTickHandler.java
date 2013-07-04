package atomicstryker.ropesplus.common;

import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ServerTickHandler implements ITickHandler
{
    private final EnumSet tickTypes;
    
    public ServerTickHandler()
    {
        tickTypes = EnumSet.of(TickType.WORLD);
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
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

    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }

    @Override
    public String getLabel()
    {
        return "RopesPlusServer";
    }

}
