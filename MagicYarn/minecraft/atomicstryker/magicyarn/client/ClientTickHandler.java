package atomicstryker.magicyarn.client;

import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import atomicstryker.magicyarn.common.pathfinding.AStarNode;
import atomicstryker.magicyarn.common.pathfinding.AStarPathPlanner;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler
{
    
    private final MagicYarnClient clientInstance;
    private final Minecraft mcinstance;
    private World lastWorld;
    public AStarPathPlanner plannerInstance;
    
    public ArrayList<AStarNode> path = null;
    public boolean showPath = false;
    
    private final EnumSet<TickType> types = EnumSet.of(TickType.CLIENT);
    
    public ClientTickHandler(MagicYarnClient client, Minecraft mc)
    {
        clientInstance = client;
        mcinstance = mc;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (mcinstance.thePlayer == null || mcinstance.theWorld == null) return;
        
        if (lastWorld != mcinstance.theWorld)
        {
            lastWorld = mcinstance.theWorld;
            if (plannerInstance != null)
            {
                plannerInstance.stopPathSearch();
            }
            plannerInstance = new AStarPathPlanner(lastWorld, clientInstance);
        }
        
        if (showPath && path != null)
        {
            for (AStarNode temp : path)
            {
                if (temp.parent != null)
                {
                    mcinstance.renderGlobal.spawnParticle("magicCrit", temp.x+0.5D, temp.y+0.5D, temp.z+0.5D,
                            (temp.parent.x - temp.x)*0.75, ((temp.parent.y - temp.y)*0.5)+0.2, (temp.parent.z - temp.z)*0.75);
                }
            }
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return types;
    }

    @Override
    public String getLabel()
    {
        return "MagicYarn";
    }
    
}
